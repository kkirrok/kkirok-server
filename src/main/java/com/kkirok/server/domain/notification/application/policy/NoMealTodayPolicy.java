package com.kkirok.server.domain.notification.application.policy;

import com.kkirok.server.domain.meal.dao.MealRecordRepository;
import com.kkirok.server.domain.notification.domain.NotificationType;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Order(2)
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NoMealTodayPolicy implements MealReminderPolicy {

    private static final String TITLE = "지금 끼록할 시간이에요";
    private static final String BODY = "오늘의 식사를 찍고 간단하게 남겨보세요";
    private static final DateTimeFormatter DAY_SUFFIX_FORMAT = DateTimeFormatter.BASIC_ISO_DATE;

    private final MealRecordRepository mealRecordRepository;

    @Override
    public NotificationType type() {
        return NotificationType.MEAL_REMINDER_NO_TODAY;
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
        // 오늘 식사 기록이 없는 활성 멤버를 모두 조회한 뒤, 하루 키를 붙여 target으로 만든다.
        String daySuffix = "DAY-" + now.toLocalDate().format(DAY_SUFFIX_FORMAT);
        return mealRecordRepository.findMemberIdsWithoutMealOn(now.toLocalDate()).stream()
                .map(memberId -> new MealReminderTarget(memberId, daySuffix))
                .toList();
    }
}
