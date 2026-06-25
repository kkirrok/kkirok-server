package com.kkirok.server.domain.meal.application.service;

import com.kkirok.server.domain.meal.application.dto.request.MealRecommendationRequest;
import com.kkirok.server.domain.meal.application.dto.response.ExerciseRecommendation;
import com.kkirok.server.domain.meal.application.dto.response.FoodRecommendation;
import com.kkirok.server.domain.meal.application.dto.response.RecommendationResponse;
import com.kkirok.server.domain.meal.application.usecase.MealRecordUseCase;
import com.kkirok.server.domain.meal.application.usecase.RecommendedAmountUseCase;
import com.kkirok.server.domain.meal.domain.MealNutrition;
import com.kkirok.server.domain.meal.domain.MealRecord;
import com.kkirok.server.global.external.openai.OpenAiService;
import com.kkirok.server.global.external.openai.dto.response.OpenAiRecommendationResult;
import com.kkirok.server.global.external.openai.dto.response.OpenAiRecommendedAmountResponse;
import com.kkirok.server.global.external.openai.prompt.PromptType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
@Service
public class RecommendationService implements RecommendedAmountUseCase {

    private static final int DAILY_RECOMMENDED_KCAL = 2000;

    private final MealRecordUseCase mealRecordUseCase;
    private final OpenAiService openAiService;

    public RecommendationResponse recommend(Long memberId) {
        List<MealRecord> todayMeals = mealRecordUseCase.getTodayRecords(memberId);
        MealRecommendationRequest input = buildInput(todayMeals);

        OpenAiRecommendationResult result = openAiService.createObjectResponse(
                PromptType.MEAL_RECOMMENDATION,
                input,
                OpenAiRecommendationResult.class
        );

        return toResponse(input.totalKcal(), result);
    }

    private MealRecommendationRequest buildInput(List<MealRecord> meals) {
        int    totalKcal          = 0;
        double totalProteinG      = 0.0;
        double totalCarbohydrateG = 0.0;
        double totalFatG          = 0.0;
        double totalSodiumMg      = 0.0;

        for (MealRecord meal : meals) {
            MealNutrition n = meal.getMealNutrition();
            if (n == null) continue;
            totalKcal          += n.getKcal();
            totalProteinG      += n.getProteinG();
            totalCarbohydrateG += n.getCarbohydrateG();
            totalFatG          += n.getFatG();
            totalSodiumMg      += n.getSodiumMg();
        }

        return new MealRecommendationRequest(
                totalKcal, totalProteinG, totalCarbohydrateG, totalFatG, totalSodiumMg, meals.size()
        );
    }

    private RecommendationResponse toResponse(int totalKcal, OpenAiRecommendationResult result) {
        List<ExerciseRecommendation> exercises = result.exerciseRecommendations().stream()
                .filter(Objects::nonNull)
                .map(e -> new ExerciseRecommendation(
                        e.exerciseName(),   // 이름만
                        e.description(),
                        e.category(),
                        e.emoji()           // 이모지 별도 필드
                ))
                .toList();

        List<FoodRecommendation> foods = result.foodRecommendations().stream()
                .filter(Objects::nonNull)
                .map(f -> new FoodRecommendation(
                        f.foodName(),           // 이름만
                        f.description(),
                        f.targetNutrientType(),
                        f.emoji()               // 이모지 별도 필드
                ))
                .toList();

        int remainingKcal = Math.max(0, DAILY_RECOMMENDED_KCAL - totalKcal);
        return new RecommendationResponse(totalKcal, exercises, remainingKcal, foods);
    }

    @Override
    public int getRecommendedKcal(Long memberId) {
        return 0;
    }

    @Override
    public int getRecommendedKcal(Long memberId, LocalDate date) {
        return 0;
    }

    @Override
    public OpenAiRecommendedAmountResponse getAllRecommendedNutrition(Long memberId) {
        return null;
    }

    @Override
    public OpenAiRecommendedAmountResponse getAllRecommendedNutrition(Long memberId, LocalDate date) {
        return null;
    }

}