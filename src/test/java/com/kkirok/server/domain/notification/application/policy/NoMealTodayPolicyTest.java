package com.kkirok.server.domain.notification.application.policy;

import com.kkirok.server.domain.meal.dao.MealRecordRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class NoMealTodayPolicyTest {

    @Mock
    private MealRecordRepository mealRecordRepository;

    @InjectMocks
    private NoMealTodayPolicy noMealTodayPolicy;

    @Test
    @DisplayName("오늘 식사 기록이 없는 멤버를 조회한다")
    void shouldFindMembersWithoutMealToday() {
        // Given
        LocalDateTime now = LocalDateTime.of(2026, 5, 6, 12, 0);
        given(mealRecordRepository.findMemberIdsWithoutMealOn(now.toLocalDate())).willReturn(List.of(1L, 2L));

        // When
        List<MealReminderTarget> targets = noMealTodayPolicy.findTargets(now);

        // Then
        assertThat(targets).containsExactly(
                new MealReminderTarget(1L, "DAY-20260506"),
                new MealReminderTarget(2L, "DAY-20260506")
        );
        then(mealRecordRepository).should().findMemberIdsWithoutMealOn(now.toLocalDate());
    }
}
