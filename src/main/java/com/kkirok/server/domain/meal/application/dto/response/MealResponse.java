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

        @Schema(description = "메모")
        String memo
) {
    public static MealResponse from(MealRecord meal) {
        MealNutrition nutrition = meal.getMealNutrition();

        return new MealResponse(
                meal.getId(),
                meal.getRecordedAt(),
                meal.getMealTimeSlot(),
                meal.getCategory(),
                meal.getScanType(),
                meal.getName(),
                nutrition != null ? nutrition.getKcal() : null,
                nutrition != null && nutrition.getCarbohydrateG() != null
                        ? nutrition.getCarbohydrateG().intValue()
                        : null,
                nutrition != null && nutrition.getProteinG() != null
                        ? nutrition.getProteinG().intValue()
                        : null,
                nutrition != null && nutrition.getFatG() != null
                        ? nutrition.getFatG().intValue()
                        : null,
                nutrition != null && nutrition.getSugarG() != null
                        ? nutrition.getSugarG().intValue()
                        : null,
                nutrition != null && nutrition.getSodiumMg() != null
                        ? nutrition.getSodiumMg().intValue()
                        : null,
                meal.getMemo()
        );
    }
}