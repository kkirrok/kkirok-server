package com.kkirok.server.domain.notification.application.policy;

import com.kkirok.server.domain.meal.dao.MealRecordRepository;
import com.kkirok.server.domain.meal.dao.MealReminderRow;
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

@ExtendWith(MockitoExtension.class)
class LastMealOverDuePolicyTest {

    @Mock
    private MealRecordRepository mealRecordRepository;

    @InjectMocks
    private LastMealOverDuePolicy lastMealOverDuePolicy;

    @Test
    @DisplayName("기준 시각보다 3시간 이상 지난 마지막 식사 멤버를 target으로 변환한다")
    void shouldMapOverdueRowsToTargets() {
        LocalDateTime now = LocalDateTime.of(2026, 5, 6, 12, 0);
        given(mealRecordRepository.findOverdueTargets(LocalDateTime.of(2026, 5, 6, 9, 0)))
                .willReturn(List.of(
                        new MealReminderRow(10L, 100L),
                        new MealReminderRow(11L, 101L)
                ));

        List<MealReminderTarget> targets = lastMealOverDuePolicy.findTargets(now);

        assertThat(targets).containsExactly(
                new MealReminderTarget(10L, "100"),
                new MealReminderTarget(11L, "101")
        );
    }
}
