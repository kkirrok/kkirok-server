package com.kkirok.server.domain.notification.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kkirok.server.domain.member.application.service.NotificationAgreeService;
import com.kkirok.server.domain.member.domain.Member;
import com.kkirok.server.domain.notification.dao.NotificationRepository;
import com.kkirok.server.domain.notification.domain.Notification;
import com.kkirok.server.domain.notification.domain.NotificationType;
import com.kkirok.server.support.fixture.MemberFixture;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class NotificationDispatcherTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationDeliveryService notificationDeliveryService;

    @Mock
    private NotificationAgreeService notificationAgreeService;

    private NotificationDispatcher notificationDispatcher;

    @BeforeEach
    void setUp() {
        notificationDispatcher = new NotificationDispatcher(
                notificationRepository,
                notificationDeliveryService,
                new ObjectMapper(),
                notificationAgreeService
        );
    }

    @AfterEach
    void tearDown() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    @DisplayName("알림을 dispatch하면 알림 row를 저장하고 커밋 후 발송을 위임한다")
    void shouldSaveNotificationsAndDeferDeliveryUntilAfterCommit() {
        // Given
        Member first = createMember(1L, "첫번째");
        Member second = createMember(2L, "두번째");
        AtomicLong sequence = new AtomicLong(10L);

        given(notificationAgreeService.findOptedOutMemberIds(Set.of(1L, 2L), NotificationType.GROUP_JOIN.getAgreeType()))
                .willReturn(Set.of());
        given(notificationRepository.save(any(Notification.class))).willAnswer(invocation -> {
            Notification notification = invocation.getArgument(0);
            ReflectionTestUtils.setField(notification, "id", sequence.getAndIncrement());
            return notification;
        });

        TransactionSynchronizationManager.initSynchronization();

        // When
        notificationDispatcher.dispatchInstant(
                List.of(first, second),
                NotificationType.GROUP_JOIN,
                "제목",
                "본문",
                Map.of("type", "GROUP_JOIN", "groupId", "1")
        );

        // Then
        ArgumentCaptor<Notification> notificationCaptor = ArgumentCaptor.forClass(Notification.class);
        then(notificationRepository).should(times(2)).save(notificationCaptor.capture());
        assertThat(notificationCaptor.getAllValues()).hasSize(2);
        assertThat(notificationCaptor.getAllValues()).allSatisfy(notification -> {
            assertThat(notification.getTitle()).isEqualTo("제목");
            assertThat(notification.getBody()).isEqualTo("본문");
            assertThat(notification.getType()).isEqualTo(NotificationType.GROUP_JOIN);
        });

        List<TransactionSynchronization> synchronizations = TransactionSynchronizationManager.getSynchronizations();
        assertThat(synchronizations).hasSize(1);

        synchronizations.get(0).afterCommit();

        ArgumentCaptor<Map<Long, Long>> targetCaptor = ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<Collection<Notification>> notificationCaptorForDelivery =
                ArgumentCaptor.forClass(Collection.class);
        then(notificationDeliveryService).should().deliver(targetCaptor.capture(), notificationCaptorForDelivery.capture());
        assertThat(targetCaptor.getValue()).containsEntry(10L, 1L).containsEntry(11L, 2L);
        assertThat(notificationCaptorForDelivery.getValue()).hasSize(2);
    }

    @Test
    @DisplayName("배치 트랙은 스케줄러 도입 전까지 저장 후 커밋 시 즉시 발송한다")
    void shouldDispatchBatchedLikeInstantUntilSchedulerIsIntroduced() {
        // Given
        Member member = createMember(1L, "첫번째");
        given(notificationAgreeService.findOptedOutMemberIds(Set.of(1L), NotificationType.GROUP_JOIN.getAgreeType()))
                .willReturn(Set.of());
        given(notificationRepository.save(any(Notification.class))).willAnswer(invocation -> {
            Notification notification = invocation.getArgument(0);
            ReflectionTestUtils.setField(notification, "id", 10L);
            return notification;
        });
        TransactionSynchronizationManager.initSynchronization();

        // When
        notificationDispatcher.dispatchBatched(
                List.of(member),
                NotificationType.GROUP_JOIN,
                "제목",
                "본문",
                Map.of("type", "GROUP_JOIN", "groupId", "1")
        );

        // Then
        then(notificationRepository).should().save(any(Notification.class));
        List<TransactionSynchronization> synchronizations = TransactionSynchronizationManager.getSynchronizations();
        assertThat(synchronizations).hasSize(1);

        synchronizations.get(0).afterCommit();

        then(notificationDeliveryService).should().deliver(eq(Map.of(10L, 1L)), any(Collection.class));
    }

    private Member createMember(Long memberId, String nickname) {
        Member member = MemberFixture.createLocalMember(nickname, nickname + "@test.com");
        ReflectionTestUtils.setField(member, "id", memberId);
        return member;
    }
}
