package com.kkirok.server.domain.notification.application.scheduler;

import com.kkirok.server.domain.notification.application.service.MealReminderService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MealReminderScheduler {

    private final MealReminderService mealReminderService;

    @Scheduled(cron = "0 0/10 9-21 * * *", zone = "Asia/Seoul")
    public void scheduleMealReminders() {
        // 스케줄러는 실행 시점만 책임지고 실제 정책 판단은 서비스로 넘긴다.
        mealReminderService.runReminderCycle();
    }
}
