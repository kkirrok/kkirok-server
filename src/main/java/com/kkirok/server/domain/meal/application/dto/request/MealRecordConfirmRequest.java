package com.kkirok.server.domain.meal.application.dto.request;

import com.kkirok.server.domain.meal.domain.MealCategory;
import com.kkirok.server.domain.meal.domain.MealTimeSlot;
import com.kkirok.server.domain.meal.domain.ScanType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record MealRecordConfirmRequest(
        @NotBlank
        @Schema(description = "scan 단계에서 발급받은 R2 이미지 키", example = "meal-images/uuid.jpg")
        String imageKey,

        @NotNull
        @Schema(description = "스캔 방식", example = "CAMERA")
        ScanType scanType,

        @NotNull
        @Schema(description = "식사 시간대", example = "DINNER")
        MealTimeSlot mealTimeSlot,

        @NotNull
        @Schema(description = "식사 카테고리", example = "MEAL")
        MealCategory category,

        @NotBlank
        @Schema(description = "음식 이름 (AI 감지값을 사용자가 수정 가능)", example = "부대찌개")
        String foodName,

        @PositiveOrZero
        @Schema(description = "칼로리", example = "500")
        Integer kcal,

        @PositiveOrZero
        @Schema(description = "탄수화물(g)", example = "60")
        Integer carbohydrateG,

        @PositiveOrZero
        @Schema(description = "단백질(g)", example = "20")
        Integer proteinG,

        @PositiveOrZero
        @Schema(description = "지방(g)", example = "15")
        Integer fatG,

        @PositiveOrZero
        @Schema(description = "당(g)", example = "5")
        Integer sugarG,

        @PositiveOrZero
        @Schema(description = "나트륨(mg)", example = "900")
        Integer sodiumMg,

        @Schema(description = "메모", example = "오늘 저녁 맛있었다")
        String memo
) {
}