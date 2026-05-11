package com.kkirok.server.domain.meal.application.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record FoodRecommendation(
        @Schema(description = "음식 이름", example = "닭가슴살")
        String foodName,

        @Schema(description = "음식 설명", example = "300kcal로 단백질 채우기 효과적이에요")
        String description,

        @Schema(description = "보충 목적 영양소", example = "단백질")
        String targetNutrientType,

        @Schema(description = "음식 이모지", example = "🥗")
        String emoji
) {}