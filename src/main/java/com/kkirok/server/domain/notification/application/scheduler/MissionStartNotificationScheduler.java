package com.kkirok.server.domain.notification.application.scheduler;

import com.kkirok.server.domain.notification.application.service.MissionStartNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MissionStartNotificationScheduler {

    private final MissionStartNotificationService missionStartNotificationService;

    // 5분에 한 번
    @Scheduled(cron = "0 */5 * * * *", zone = "Asia/Seoul")
    public void scheduleMissionStartNotifications() {
        missionStartNotificationService.dispatchStartingMissions();
    }
}
