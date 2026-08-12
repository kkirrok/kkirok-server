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
    @DisplayName("디바이스 토큰이 없으면 NO_DEVICE로 실패 처리하고 FCM은 호출하지 않는다")
    void deliverPendingBatch_noDevice() {
        Member member = createMember(1L, "첫번째");
        Notification notification = createNotification(10L, member, NotificationType.GROUP_JOIN);

        given(notificationRepository.findAllById(List.of(10L))).willReturn(List.of(notification));
        given(deviceRepository.findAllByMember_IdIn(Set.of(1L))).willReturn(List.of());

        notificationDeliveryService.deliverPendingBatch(List.of(notification));

        assertThat(notification.getFailedAt()).isNotNull();
        assertThat(notification.getFailureReason()).isEqualTo("NO_DEVICE");
        then(pushSender).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("토큰 1개로 발송 성공하면 markSent 처리한다")
    void deliverPendingBatch_success() {
        Member member = createMember(1L, "첫번째");
        Notification notification = createNotification(10L, member, NotificationType.GROUP_JOIN);
        Device device = createDevice(member, "token-1");

        given(notificationRepository.findAllById(List.of(10L))).willReturn(List.of(notification));
        given(deviceRepository.findAllByMember_IdIn(Set.of(1L))).willReturn(List.of(device));
        given(pushSender.sendEach(anyList())).willReturn(List.of(new PushBatchItemResult("token-1", true, false)));

        notificationDeliveryService.deliverPendingBatch(List.of(notification));

        assertThat(notification.getSentAt()).isNotNull();
        assertThat(notification.getFailedAt()).isNull();
    }

    @Test
    @DisplayName("한 알림이 디바이스 2개를 가졌을 때 하나라도 성공하면 markSent 처리하고, 무효 토큰만 삭제한다")
    void deliverPendingBatch_partialSuccessAcrossDevices() {
        Member member = createMember(1L, "첫번째");
        Notification notification = createNotification(10L, member, NotificationType.GROUP_JOIN);
        Device device1 = createDevice(member, "token-1");
        Device device2 = createDevice(member, "token-2");

        given(notificationRepository.findAllById(List.of(10L))).willReturn(List.of(notification));
        given(deviceRepository.findAllByMember_IdIn(Set.of(1L))).willReturn(List.of(device1, device2));
        given(pushSender.sendEach(anyList())).willReturn(List.of(
                new PushBatchItemResult("token-1", false, true),
                new PushBatchItemResult("token-2", true, false)
        ));

        notificationDeliveryService.deliverPendingBatch(List.of(notification));

        assertThat(notification.getSentAt()).isNotNull();
        then(deviceRepository).should().deleteByTokenIn(List.of("token-1"));
    }

    @Test
    @DisplayName("서로 다른 두 알림을 한 번에 처리해도 결과가 섞이지 않는다")
    void deliverPendingBatch_multipleNotificationsNoCrossContamination() {
        Member member1 = createMember(1L, "첫번째");
        Member member2 = createMember(2L, "두번째");
        Notification notification1 = createNotification(10L, member1, NotificationType.GROUP_JOIN);
        Notification notification2 = createNotification(11L, member2, NotificationType.MEAL_REMINDER_NO_TODAY);
        Device device1 = createDevice(member1, "token-1");
        Device device2 = createDevice(member2, "token-2");

        given(notificationRepository.findAllById(List.of(10L, 11L))).willReturn(List.of(notification1, notification2));
        given(deviceRepository.findAllByMember_IdIn(Set.of(1L, 2L))).willReturn(List.of(device1, device2));
        given(pushSender.sendEach(anyList())).willReturn(List.of(
                new PushBatchItemResult("token-1", true, false),
                new PushBatchItemResult("token-2", false, false)
        ));

        notificationDeliveryService.deliverPendingBatch(List.of(notification1, notification2));

        assertThat(notification1.getSentAt()).isNotNull();
        assertThat(notification2.getFailedAt()).isNotNull();
        assertThat(notification2.getFailureReason()).isEqualTo("FCM_NO_SUCCESS");
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
