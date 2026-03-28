package com.kkirok.server.domain.member.util;

import com.kkirok.server.domain.member.domain.OnboardingHabit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OnboardingHabitListConverterTest {

    private final OnboardingHabitListConverter converter = new OnboardingHabitListConverter();

    @Test
    @DisplayName("온보딩 습관 목록을 DB 컬럼 문자열로 변환한다")
    void shouldConvertHabitListToDatabaseColumn() {
        List<OnboardingHabit> habits = List.of(
                OnboardingHabit.MEAT,
                OnboardingHabit.SNACK,
                OnboardingHabit.DIET
        );

        String result = converter.convertToDatabaseColumn(habits);

        assertThat(result).isEqualTo("MEAT,SNACK,DIET");
    }

    @Test
    @DisplayName("DB 컬럼 문자열을 온보딩 습관 목록으로 변환한다")
    void shouldConvertDatabaseColumnToHabitList() {
        List<OnboardingHabit> result = converter.convertToEntityAttribute("MEAT,SNACK,DIET");

        assertThat(result).containsExactly(
                OnboardingHabit.MEAT,
                OnboardingHabit.SNACK,
                OnboardingHabit.DIET
        );
    }

    @Test
    @DisplayName("비어 있는 온보딩 습관 값은 빈 목록으로 변환한다")
    void shouldReturnEmptyListWhenDatabaseColumnIsBlank() {
        assertThat(converter.convertToDatabaseColumn(List.of())).isEqualTo("");
        assertThat(converter.convertToEntityAttribute(null)).isEmpty();
        assertThat(converter.convertToEntityAttribute("")).isEmpty();
        assertThat(converter.convertToEntityAttribute("   ")).isEmpty();
    }
}
