package com.kkirok.server.domain.notification.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kkirok.server.domain.member.domain.Member;
import com.kkirok.server.domain.notification.application.sender.PushBatchItemResult;
import com.kkirok.server.domain.notification.application.sender.PushSender;
import com.kkirok.server.domain.notification.dao.DeviceRepository;
import com.kkirok.server.domain.notification.dao.NotificationRepository;
import com.kkirok.server.domain.notification.domain.Device;
import com.kkirok.server.domain.notification.domain.DevicePlatform;
import com.kkirok.server.domain.notification.domain.Notification;
import com.kkirok.server.domain.notification.domain.NotificationType;
import com.kkirok.server.global.external.r2.application.service.PresignedUrlService;
import com.kkirok.server.support.fixture.MemberFixture;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class NotificationDeliveryServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private DeviceRepository deviceRepository;

    @Mock
    private PushSender pushSender;

    @Mock
    private PresignedUrlService presignedUrlService;

    private NotificationDeliveryService notificationDeliveryService;

    @BeforeEach
    void setUp() {
        notificationDeliveryService = new NotificationDeliveryService(
                notificationRepository, deviceRepository, pushSender, presignedUrlService, new ObjectMapper()
        );
    }

    @Test
    @DisplayName("빈 목록이면 아무 것도 하지 않는다")
    void deliverPendingBatch_empty() {
        notificationDeliveryService.deliverPendingBatch(List.of());

        then(notificationRepository).shouldHaveNoInteractions();
        then(pushSender).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("디바이스 토큰이 없으면 managed 엔티티만 NO_DEVICE로 실패 처리하고, 파라미터로 넘긴 detached 엔티티는 건드리지 않는다")
    void deliverPendingBatch_noDevice() {
        Member member = createMember(1L, "첫번째");
        // detached: deliverPendingBatch()에 파라미터로 넘어가는, 스케줄러 트랜잭션에서 조회된 것으로 가정하는 인스턴스
        Notification detached = createNotification(10L, member, NotificationType.GROUP_JOIN);
        // managed: findAllById()가 이 메서드 자신의 새 트랜잭션 안에서 재조회해 리턴하는, detached와는 별개의 인스턴스
        Notification managed = createNotification(10L, member, NotificationType.GROUP_JOIN);

        given(notificationRepository.findAllById(List.of(10L))).willReturn(List.of(managed));
        given(deviceRepository.findAllByMember_IdIn(Set.of(1L))).willReturn(List.of());

        notificationDeliveryService.deliverPendingBatch(List.of(detached));

        assertThat(managed.getFailedAt()).isNotNull();
        assertThat(managed.getFailureReason()).isEqualTo("NO_DEVICE");
        assertThat(detached.getFailedAt()).isNull();
        then(pushSender).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("토큰 1개로 발송 성공하면 managed 엔티티만 markSent 처리한다")
    void deliverPendingBatch_success() {
        Member member = createMember(1L, "첫번째");
        Notification detached = createNotification(10L, member, NotificationType.GROUP_JOIN);
        Notification managed = createNotification(10L, member, NotificationType.GROUP_JOIN);
        Device device = createDevice(member, "token-1");

        given(notificationRepository.findAllById(List.of(10L))).willReturn(List.of(managed));
        given(deviceRepository.findAllByMember_IdIn(Set.of(1L))).willReturn(List.of(device));
        given(pushSender.sendEach(anyList())).willReturn(List.of(new PushBatchItemResult("token-1", true, false)));

        notificationDeliveryService.deliverPendingBatch(List.of(detached));

        assertThat(managed.getSentAt()).isNotNull();
        assertThat(managed.getFailedAt()).isNull();
        assertThat(detached.getSentAt()).isNull();
    }

    @Test
    @DisplayName("한 알림이 디바이스 2개를 가졌을 때 하나라도 성공하면 managed 엔티티를 markSent 처리하고, 무효 토큰만 삭제한다")
    void deliverPendingBatch_partialSuccessAcrossDevices() {
        Member member = createMember(1L, "첫번째");
        Notification detached = createNotification(10L, member, NotificationType.GROUP_JOIN);
        Notification managed = createNotification(10L, member, NotificationType.GROUP_JOIN);
        Device device1 = createDevice(member, "token-1");
        Device device2 = createDevice(member, "token-2");

        given(notificationRepository.findAllById(List.of(10L))).willReturn(List.of(managed));
        given(deviceRepository.findAllByMember_IdIn(Set.of(1L))).willReturn(List.of(device1, device2));
        given(pushSender.sendEach(anyList())).willReturn(List.of(
                new PushBatchItemResult("token-1", false, true),
                new PushBatchItemResult("token-2", true, false)
        ));

        notificationDeliveryService.deliverPendingBatch(List.of(detached));

        assertThat(managed.getSentAt()).isNotNull();
        assertThat(detached.getSentAt()).isNull();
        then(deviceRepository).should().deleteByTokenIn(List.of("token-1"));
    }

    @Test
    @DisplayName("서로 다른 두 알림을 한 번에 처리해도 managed 엔티티끼리 결과가 섞이지 않는다")
    void deliverPendingBatch_multipleNotificationsNoCrossContamination() {
        Member member1 = createMember(1L, "첫번째");
        Member member2 = createMember(2L, "두번째");
        Notification detached1 = createNotification(10L, member1, NotificationType.GROUP_JOIN);
        Notification detached2 = createNotification(11L, member2, NotificationType.MEAL_REMINDER_NO_TODAY);
        Notification managed1 = createNotification(10L, member1, NotificationType.GROUP_JOIN);
        Notification managed2 = createNotification(11L, member2, NotificationType.MEAL_REMINDER_NO_TODAY);
        Device device1 = createDevice(member1, "token-1");
        Device device2 = createDevice(member2, "token-2");

        given(notificationRepository.findAllById(List.of(10L, 11L))).willReturn(List.of(managed1, managed2));
        given(deviceRepository.findAllByMember_IdIn(Set.of(1L, 2L))).willReturn(List.of(device1, device2));
        given(pushSender.sendEach(anyList())).willReturn(List.of(
                new PushBatchItemResult("token-1", true, false),
                new PushBatchItemResult("token-2", false, false)
        ));

        notificationDeliveryService.deliverPendingBatch(List.of(detached1, detached2));

        assertThat(managed1.getSentAt()).isNotNull();
        assertThat(managed2.getFailedAt()).isNotNull();
        assertThat(managed2.getFailureReason()).isEqualTo("FCM_NO_SUCCESS");
        assertThat(detached1.getSentAt()).isNull();
        assertThat(detached2.getFailedAt()).isNull();
    }

    @Test
    @DisplayName("sendEach()가 예외를 던지면 그 배치의 managed 엔티티들을 실패 처리하고 메서드는 예외 없이 끝난다")
    void deliverPendingBatch_sendEachThrows() {
        Member member = createMember(1L, "첫번째");
        Notification detached = createNotification(10L, member, NotificationType.GROUP_JOIN);
        Notification managed = createNotification(10L, member, NotificationType.GROUP_JOIN);
        Device device = createDevice(member, "token-1");

        given(notificationRepository.findAllById(List.of(10L))).willReturn(List.of(managed));
        given(deviceRepository.findAllByMember_IdIn(Set.of(1L))).willReturn(List.of(device));
        given(pushSender.sendEach(anyList())).willThrow(new IllegalStateException("FCM batch send failed"));

        org.junit.jupiter.api.Assertions.assertDoesNotThrow(
                () -> notificationDeliveryService.deliverPendingBatch(List.of(detached))
        );

        assertThat(managed.getFailedAt()).isNotNull();
        assertThat(managed.getSentAt()).isNull();
        then(deviceRepository).should(org.mockito.Mockito.never()).deleteByTokenIn(anyList());
    }

    private Member createMember(Long memberId, String nickname) {
        Member member = MemberFixture.createLocalMember(nickname, nickname + "@test.com");
        ReflectionTestUtils.setField(member, "id", memberId);
        return member;
    }

    private Notification createNotification(Long id, Member member, NotificationType type) {
        Notification notification = Notification.create(member, type, "제목", "본문", null, null);
        ReflectionTestUtils.setField(notification, "id", id);
        return notification;
    }

    private Device createDevice(Member member, String token) {
        return Device.create(member, token, DevicePlatform.ANDROID, LocalDateTime.now());
    }
}
