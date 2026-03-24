package com.kkirok.server.domain.meal.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import org.apache.logging.log4j.core.config.plugins.validation.constraints.NotBlank;

public record MealUpdateRequest (
        @NotNull
        @Schema(description = "식사 유형", example = "1")
        Long foodCategory,

        @NotBlank
        @Schema(description = "음식 이름", example = "샐러드")
        String foodName,

        @PositiveOrZero
        @Schema(description = "음식 칼로리", example = "152")
        Integer kcal,

        @PositiveOrZero
        @Schema(description = "탄수화물(g)", example = "50")
        Integer carbohydrateG,

        @PositiveOrZero
        @Schema(description = "지방(g)", example = "80")
        Integer fatG,

        @PositiveOrZero
        @Schema(description = "단백질(g)", example = "30")
        Integer proteinG,

        @PositiveOrZero
        @Schema(description = "나트륨(mg)", example = "30")
        Integer sodiumMg,

        @PositiveOrZero
        @Schema(description = "당(g)", example = "30")
        Integer sugarG
){
}
