package com.kkirok.server.domain.meal.application.dto.response;

import com.kkirok.server.domain.meal.domain.MealCategory;
import com.kkirok.server.domain.meal.domain.MealNutrition;
import com.kkirok.server.domain.meal.domain.MealRecord;
import com.kkirok.server.domain.meal.domain.MealTimeSlot;
import com.kkirok.server.domain.meal.domain.ScanType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record MealResponse(
        @Schema(description = "식단기록 아이디", example = "1")
        Long mealId,

        @Schema(description = "식사 기록 날짜 및 시간", example = "2026-05-11T12:30:00")
        LocalDateTime recordedAt,

        @Schema(description = "식사 시간대", example = "BREAKFAST")
        MealTimeSlot mealTimeSlot,

        @Schema(description = "식사 카테고리", example = "MEAL")
        MealCategory category,

        @Schema(description = "기록 방식", example = "DIRECT")
        ScanType scanType,

        @Schema(description = "음식 이름", example = "샐러드")
        String foodName,

        @Schema(description = "음식 칼로리", example = "152")
        Integer kcal,

        @Schema(description = "탄수화물(g)", example = "13")
        Integer carbohydrateG,

        @Schema(description = "단백질(g)", example = "20")
        Integer proteinG,

        @Schema(description = "지방(g)", example = "3")
        Integer fatG,

        @Schema(description = "당(g)", example = "5")
        Integer sugarG,

        @Schema(description = "나트륨(mg)", example = "6")
        Integer sodiumMg,

        @Schema(description = "탄수화물 칼로리 비율(%)", example = "55")
        Integer carbohydratePercent,

        @Schema(description = "단백질 칼로리 비율(%)", example = "20")
        Integer proteinPercent,

        @Schema(description = "지방 칼로리 비율(%)", example = "25")
        Integer fatPercent,

        @Schema(description = "메모")
        String memo
) {
    public static MealResponse from(MealRecord meal) {
        MealNutrition nutrition = meal.getMealNutrition();

        Integer carbG = nutrition != null && nutrition.getCarbohydrateG() != null
                ? nutrition.getCarbohydrateG().intValue() : null;
        Integer proteinG = nutrition != null && nutrition.getProteinG() != null
                ? nutrition.getProteinG().intValue() : null;
        Integer fatG = nutrition != null && nutrition.getFatG() != null
                ? nutrition.getFatG().intValue() : null;

        // 탄수화물: 4 kcal/g, 단백질: 4 kcal/g, 지방: 9 kcal/g
        Integer carbohydratePercent = null;
        Integer proteinPercent = null;
        Integer fatPercent = null;

        if (carbG != null && proteinG != null && fatG != null) {
            long carbKcal = carbG * 4L;
            long proteinKcal = proteinG * 4L;
            long fatKcal = fatG * 9L;
            long macroTotal = carbKcal + proteinKcal + fatKcal;

            if (macroTotal > 0) {
                carbohydratePercent = (int) Math.round(carbKcal * 100.0 / macroTotal);
                proteinPercent = (int) Math.round(proteinKcal * 100.0 / macroTotal);
                fatPercent = 100 - carbohydratePercent - proteinPercent;
            }
        }

        return new MealResponse(
                meal.getId(),
                meal.getRecordedAt(),
                meal.getMealTimeSlot(),
                meal.getCategory(),
                meal.getScanType(),
                meal.getName(),
                nutrition != null ? nutrition.getKcal() : null,
                carbG,
                proteinG,
                fatG,
                nutrition != null && nutrition.getSugarG() != null
                        ? nutrition.getSugarG().intValue() : null,
                nutrition != null && nutrition.getSodiumMg() != null
                        ? nutrition.getSodiumMg().intValue() : null,
                carbohydratePercent,
                proteinPercent,
                fatPercent,
                meal.getMemo()
        );
    }
}