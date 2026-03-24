package com.kkirok.server.domain.meal.application.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record ExerciseRecommendation (
        @Schema(description = "운동 이름", example = "자전거")
        String exerciseName,

        @Schema(description = "운동 설명", example = "1시간을 타면 200kcal가 소모되어요!")
        String description,

        @Schema(description = "운동 카테고리", example = "유산소")
        String category
){
}
