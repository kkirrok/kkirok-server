package com.kkirok.server.domain.meal.application.dto.response;

import com.kkirok.server.domain.meal.domain.MealRecord;
import com.kkirok.server.domain.meal.domain.MealTimeSlot;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Schema(description = "특정 날짜 식단 상세 응답")
public record DailyMealResponse(

        @Schema(description = "조회 날짜", example = "2026-04-13")
        LocalDate date,

        @Schema(description = "총 칼로리", example = "1600")
        Integer totalKcal,

        @Schema(description = "총 탄수화물(g)", example = "89")
        Long totalCarbohydrateG,

        @Schema(description = "총 단백질(g)", example = "89")
        Long totalProteinG,

        @Schema(description = "총 지방(g)", example = "89")
        Long totalFatG,

        @Schema(description = "총 당(g)", example = "30")
        Long totalSugarG,

        @Schema(description = "총 나트륨(mg)", example = "1200")
        Long totalSodiumMg,

        @Schema(description = "아침 식단 목록")
        List<MealResponse> breakfastMeals,

        @Schema(description = "점심 식단 목록")
        List<MealResponse> lunchMeals,

        @Schema(description = "저녁 식단 목록")
        List<MealResponse> dinnerMeals,

        @Schema(description = "간식 식단 목록")
        List<MealResponse> snackMeals,

        @Schema(description = "야식 식단 목록")
        List<MealResponse> midnightSnackMeals

) {
    public static DailyMealResponse from(LocalDate date, List<MealRecord> records) {
        int totalKcal = 0;
        long totalCarbohydrateG = 0L;
        long totalProteinG = 0L;
        long totalFatG = 0L;
        long totalSugarG = 0L;
        long totalSodiumMg = 0L;

        for (MealRecord record : records) {
            if (record.getMealNutrition() == null) continue;

            totalKcal += record.getMealNutrition().getKcal() == null
                    ? 0 : record.getMealNutrition().getKcal();
            totalCarbohydrateG += record.getMealNutrition().getCarbohydrateG() == null
                    ? 0L : record.getMealNutrition().getCarbohydrateG().longValue();
            totalProteinG += record.getMealNutrition().getProteinG() == null
                    ? 0L : record.getMealNutrition().getProteinG().longValue();
            totalFatG += record.getMealNutrition().getFatG() == null
                    ? 0L : record.getMealNutrition().getFatG().longValue();
            totalSugarG += record.getMealNutrition().getSugarG() == null
                    ? 0L : record.getMealNutrition().getSugarG().longValue();
            totalSodiumMg += record.getMealNutrition().getSodiumMg() == null
                    ? 0L : record.getMealNutrition().getSodiumMg().longValue();
        }

        List<MealResponse> breakfastMeals = filterBySlot(records, MealTimeSlot.BREAKFAST);
        List<MealResponse> lunchMeals = filterBySlot(records, MealTimeSlot.LUNCH);
        List<MealResponse> dinnerMeals = filterBySlot(records, MealTimeSlot.DINNER);
        List<MealResponse> snackMeals = filterBySlot(records, MealTimeSlot.SNACK);
        List<MealResponse> midnightSnackMeals = filterBySlot(records, MealTimeSlot.MIDNIGHT_SNACK);

        return new DailyMealResponse(
                date,
                totalKcal,
                totalCarbohydrateG,
                totalProteinG,
                totalFatG,
                totalSugarG,
                totalSodiumMg,
                breakfastMeals,
                lunchMeals,
                dinnerMeals,
                snackMeals,
                midnightSnackMeals
        );
    }

    private static List<MealResponse> filterBySlot(List<MealRecord> records, MealTimeSlot slot) {
        return records.stream()
                .filter(r -> r.getMealTimeSlot() == slot)
                .map(MealResponse::from)
                .collect(Collectors.toList());
    }
}
