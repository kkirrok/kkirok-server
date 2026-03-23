package com.kkirok.server.domain.meal.application.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record MealResponse (
    @Schema(description = "식단기록 아이디", example = "1")
    Long mealId,
    @Schema(description = "식사 유형", example = "아침")
    Long foodCategory,
    @Schema(description = "음식 이름", example = "샐러드")
    String foodName,
    @Schema(description = "음식 칼로리", example = "152")
    Integer kcal,
    @Schema(description = "탄수화물g", example = "50")
    Integer carbohydrateG,
    @Schema(description = "지방g", example = "80")
    Integer fatG,
    @Schema(description = "단백질g", example = "30")
    Integer proteinG,
    @Schema(description = "나트륨mg", example = "30")
    Integer sodiumMg,
    @Schema(description = "당g", example = "30")
    Integer sugarG
) {
}