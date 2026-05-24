package com.kkirok.server.domain.member.application.service;

import com.kkirok.server.domain.member.domain.MealStyle;
import com.kkirok.server.domain.member.domain.OnboardingHabit;
import com.kkirok.server.global.external.openai.OpenAiService;
import com.kkirok.server.global.external.openai.dto.response.OpenAiMealStyleResult;
import com.kkirok.server.global.external.openai.prompt.PromptType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MealStyleClassificationService {

    private final OpenAiService openAiService;

    public MealStyle classify(List<OnboardingHabit> habits) {
        if (habits == null || habits.isEmpty()) {
            return MealStyle.BALANCED;
        }

        String habitsText = habits.stream()
                .map(OnboardingHabit::getLabel)
                .collect(Collectors.joining(", "));

        OpenAiMealStyleResult result = openAiService.createObjectResponse(
                PromptType.MEAL_STYLE_CLASSIFICATION,
                habitsText,
                OpenAiMealStyleResult.class
        );

        return parseMealStyle(result.mealStyle());
    }

    private MealStyle parseMealStyle(String label) {
        return Arrays.stream(MealStyle.values())
                .filter(mealStyle -> mealStyle.getLabel().equals(label))
                .findFirst()
                .orElse(MealStyle.BALANCED);
    }
}
