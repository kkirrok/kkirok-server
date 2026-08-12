package com.kkirok.server.domain.notification.application.scheduler;

import com.kkirok.server.domain.notification.application.service.NotificationDeliveryService;
import com.kkirok.server.domain.notification.dao.NotificationRepository;
import com.kkirok.server.domain.notification.domain.Notification;
import com.kkirok.server.domain.notification.domain.NotificationType;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 배치 트랙(GROUP_JOIN, MEAL_REMINDER_*)의 미발송/재시도 대상 알림을 5분 주기로 모아서 발송한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationDeliveryScheduler {

    private static final int MAX_RETRY_COUNT = 5;
    private static final List<NotificationType> BATCH_TYPES = List.of(
            NotificationType.GROUP_JOIN,
            NotificationType.MEAL_REMINDER_OVERDUE,
            NotificationType.MEAL_REMINDER_NO_TODAY
    );

    private final NotificationRepository notificationRepository;
    private final NotificationDeliveryService notificationDeliveryService;

    @Scheduled(cron = "0 */5 * * * *", zone = "Asia/Seoul")
    public void deliverPendingNotifications() {
        List<Notification> pending = notificationRepository.findPendingForDelivery(BATCH_TYPES, MAX_RETRY_COUNT);
        if (pending.isEmpty()) {
            return;
        }

        notificationDeliveryService.deliverPendingBatch(pending);
    }
}
