package com.kkirok.server.domain.meal.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

public record MealUpdateRequest (
        @Schema(description = "식사 유형", example = "1")
        Long foodCategory,

        @Schema(description = "음식 이름", example = "샐러드")
        String foodName,

        @Schema(description = "음식 칼로리", example = "152")
        Integer kcal,

        @Schema(description = "탄수화물(g)", example = "50")
        Integer carbohydrateG,

        @Schema(description = "지방(g)", example = "80")
        Integer fatG,

        @Schema(description = "단백질(g)", example = "30")
        Integer proteinG,

        @Schema(description = "나트륨(mg)", example = "30")
        Integer sodiumMg,

        @Schema(description = "당(g)", example = "30")
        Integer sugarG
){
}
