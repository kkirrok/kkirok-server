package com.kkirok.server.domain.notification.application.scheduler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.kkirok.server.domain.notification.application.service.NotificationDeliveryService;
import com.kkirok.server.domain.notification.dao.NotificationRepository;
import com.kkirok.server.domain.notification.domain.Notification;
import com.kkirok.server.domain.notification.domain.NotificationType;
import java.util.Collection;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificationDeliverySchedulerTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationDeliveryService notificationDeliveryService;

    @InjectMocks
    private NotificationDeliveryScheduler notificationDeliveryScheduler;

    @Test
    @DisplayName("대상이 없으면 발송 로직을 호출하지 않는다")
    void deliverPendingNotifications_empty() {
        given(notificationRepository.findPendingForDelivery(anyCollection(), eq(5))).willReturn(List.of());

        notificationDeliveryScheduler.deliverPendingNotifications();

        then(notificationDeliveryService).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("배치 트랙 타입과 최대 재시도 횟수로 조회한 뒤 결과를 그대로 발송 로직에 넘긴다")
    void deliverPendingNotifications_delegates() {
        Notification notification = Mockito.mock(Notification.class);
        given(notificationRepository.findPendingForDelivery(anyCollection(), eq(5))).willReturn(List.of(notification));

        notificationDeliveryScheduler.deliverPendingNotifications();

        ArgumentCaptor<Collection<NotificationType>> typesCaptor = ArgumentCaptor.forClass(Collection.class);
        then(notificationRepository).should().findPendingForDelivery(typesCaptor.capture(), eq(5));
        assertThat(typesCaptor.getValue()).containsExactlyInAnyOrder(
                NotificationType.GROUP_JOIN, NotificationType.MEAL_REMINDER_OVERDUE, NotificationType.MEAL_REMINDER_NO_TODAY
        );

        then(notificationDeliveryService).should().deliverPendingBatch(List.of(notification));
    }
}
