package com.kkirok.server.domain.meal.application.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record FoodRecommendation(
        @Schema(description = "음식 이름", example = "샐러드")
        String foodName,

        @Schema(description = "음식 설명", example = "302kcal로 부족한 탄수화물을 채우기 효과적이에요")
        String description,

        @Schema(description = "보충 목적 영양소", example = "탄수화물")
        String targetNutrientType
) {
}
