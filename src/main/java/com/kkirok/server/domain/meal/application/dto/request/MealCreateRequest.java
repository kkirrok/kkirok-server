package com.kkirok.server.domain.meal.application.dto.request;

import com.kkirok.server.domain.meal.domain.MealCategory;
import com.kkirok.server.domain.meal.domain.MealTimeSlot;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.LocalDateTime;

public record MealCreateRequest (
        @Schema(description = "식사 기록 날짜 및 시간", example = "2026-05-11T12:30:00")
        LocalDateTime recordedAt,

        @NotNull
        @Schema(description = "식사 시간대", example = "BREAKFAST")
        MealTimeSlot mealTimeSlot,

        @NotNull
        @Schema(description = "식사 카테고리", example = "MEAL")
        MealCategory category,

        @NotBlank
        @Schema(description = "음식 이름", example = "샐러드")
        String foodName,

        @PositiveOrZero
        @Schema(description = "음식 칼로리", example = "152")
        Integer kcal,

        @PositiveOrZero
        @Schema(description = "탄수화물(g)", example = "13")
        Integer carbohydrateG,

        @PositiveOrZero
        @Schema(description = "단백질(g)", example = "20")
        Integer proteinG,

        @PositiveOrZero
        @Schema(description = "지방(g)", example = "3")
        Integer fatG,

        @PositiveOrZero
        @Schema(description = "당(g)", example = "5")
        Integer sugarG,

        @PositiveOrZero
        @Schema(description = "나트륨(mg)", example = "6")
        Integer sodiumMg,

        @Schema(description = "메모", example = "오늘 점심 맛있었다")
        String memo
){
}
