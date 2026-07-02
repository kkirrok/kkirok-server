package com.kkirok.server.domain.meal.application.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kkirok.server.domain.meal.domain.MealTimeSlot;
import com.kkirok.server.domain.meal.domain.ScanType;
import com.kkirok.server.global.external.openai.dto.response.OpenAiFoodAnalysisResult;
import io.swagger.v3.oas.annotations.media.Schema;

public record MealScanResponse(
        @Schema(description = "R2 이미지 키", example = "meal-images/uuid.jpg")
        String imageKey,
        @Schema(description = "감지된 음식명", example = "된장찌개")
        String foodName,
        @Schema(description = "식사 시간대", example = "DINNER")
        MealTimeSlot mealTimeSlot,
        @Schema(description = "스캔 방식", example = "IMAGE")
        @JsonProperty("scan_type")
        ScanType scanType,
        @Schema(description = "칼로리", example = "350")
        Integer kcal,
        @Schema(description = "탄수화물(g)", example = "30")
        Integer carbohydrateG,
        @Schema(description = "단백질(g)", example = "15")
        Integer proteinG,
        @Schema(description = "지방(g)", example = "8")
        Integer fatG,
        @Schema(description = "당(g)", example = "5")
        Integer sugarG,
        @Schema(description = "나트륨(mg)", example = "900")
        Integer sodiumMg
) {
    public static MealScanResponse from(
            String imageKey,
            ScanType scanType,
            OpenAiFoodAnalysisResult result
    ) {
        return new MealScanResponse(
                imageKey,
                result.foodNameOrDefault(),
                result.mealTimeSlotOrDefault(),
                scanType,
                result.kcalOrDefault(),
                (int) result.carbohydrateOrDefault(),
                (int) result.proteinOrDefault(),
                (int) result.fatOrDefault(),
                (int) result.sugarOrDefault(),
                (int) result.sodiumOrDefault()
        );
    }
}