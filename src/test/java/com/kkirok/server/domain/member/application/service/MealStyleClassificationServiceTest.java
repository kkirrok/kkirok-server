package com.kkirok.server.domain.member.application.service;

import com.kkirok.server.domain.member.domain.MealStyle;
import com.kkirok.server.domain.member.domain.OnboardingHabit;
import com.kkirok.server.global.external.openai.OpenAiService;
import com.kkirok.server.global.external.openai.dto.response.OpenAiMealStyleResult;
import com.kkirok.server.global.external.openai.prompt.PromptType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class MealStyleClassificationServiceTest {

    @Mock
    private OpenAiService openAiService;

    @InjectMocks
    private MealStyleClassificationService mealStyleClassificationService;

    @Test
    @DisplayName("식습관 목록을 OpenAI에 전달하고 응답 label을 MealStyle로 변환한다")
    void shouldClassifyMealStyle_whenHabitsExist() {
        // Given
        List<OnboardingHabit> habits = List.of(OnboardingHabit.MEAT, OnboardingHabit.SNACK);
        given(openAiService.createObjectResponse(
                PromptType.MEAL_STYLE_CLASSIFICATION,
                "고기, 간식",
                OpenAiMealStyleResult.class
        )).willReturn(new OpenAiMealStyleResult("단백질 집중형"));

        // When
        MealStyle result = mealStyleClassificationService.classify(habits);

        // Then
        assertThat(result).isEqualTo(MealStyle.PROTEIN_FOCUSED);
        then(openAiService).should().createObjectResponse(
                PromptType.MEAL_STYLE_CLASSIFICATION,
                "고기, 간식",
                OpenAiMealStyleResult.class
        );
    }

    @Test
    @DisplayName("식습관 목록이 비어 있으면 OpenAI를 호출하지 않고 균형형을 반환한다")
    void shouldReturnBalanced_whenHabitsIsEmpty() {
        // When
        MealStyle result = mealStyleClassificationService.classify(List.of());

        // Then
        assertThat(result).isEqualTo(MealStyle.BALANCED);
        then(openAiService).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("식습관 목록이 null이면 OpenAI를 호출하지 않고 균형형을 반환한다")
    void shouldReturnBalanced_whenHabitsIsNull() {
        // When
        MealStyle result = mealStyleClassificationService.classify(null);

        // Then
        assertThat(result).isEqualTo(MealStyle.BALANCED);
        then(openAiService).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("OpenAI 응답 label이 알 수 없는 값이면 균형형으로 fallback한다")
    void shouldReturnBalanced_whenLabelIsUnknown() {
        // Given
        List<OnboardingHabit> habits = List.of(OnboardingHabit.DESSERT);
        given(openAiService.createObjectResponse(
                PromptType.MEAL_STYLE_CLASSIFICATION,
                "디저트",
                OpenAiMealStyleResult.class
        )).willReturn(new OpenAiMealStyleResult("알 수 없음"));

        // When
        MealStyle result = mealStyleClassificationService.classify(habits);

        // Then
        assertThat(result).isEqualTo(MealStyle.BALANCED);
    }
}
