package com.kkirok.server.domain.notification.application.policy;

import com.kkirok.server.domain.meal.dao.MealRecordRepository;
import com.kkirok.server.domain.meal.dao.MealReminderRow;
import com.kkirok.server.domain.notification.domain.NotificationType;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Order(1)
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LastMealOverDuePolicy implements MealReminderPolicy {

    private static final long OVERDUE_MINUTES = 180;
    private static final String TITLE = "지금 끼록할 시간이에요";
    private static final String BODY = "오늘의 식사를 찍고 간단하게 남겨보세요";

    private final MealRecordRepository mealRecordRepository;

    @Override
    public NotificationType type() {
        return NotificationType.MEAL_REMINDER_OVERDUE;
    }

    @Override
    public String title() {
        return TITLE;
    }

    @Override
    public String body() {
        return BODY;
    }

    @Override
    public List<MealReminderTarget> findTargets(LocalDateTime now) {
        // 마지막 식사 시각이 기준 시각보다 180분 이상 지난 멤버만 조회한다.
        LocalDateTime threshold = now.minusMinutes(OVERDUE_MINUTES);
        return mealRecordRepository.findOverdueTargets(threshold).stream()
                .map(this::toTarget)
                .toList();
    }

    private MealReminderTarget toTarget(MealReminderRow row) {
        // row projection을 알림 디스패치용 target 형식으로 변환한다.
        return new MealReminderTarget(row.memberId(), String.valueOf(row.lastMealId()));
    }
}
