package com.kkirok.server.domain.meal.application.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record RecommendationResponse (
        @Schema(description = "운동 추천 기준 칼로리(섭취 칼로리)", example = "152")
        Integer targetExerciseKcal,

        @Schema(description = "운동 추천 목록",
                example = """
        [
            {
                "exerciseName": "자전거",
                "description": "1시간을 타면 200kcal가 소모되어요!",
                "category": 유산소
            },
            {
                "exerciseName": "테니스",
                "description": "1시간을 타면 200kcal가 소모되어요!",
                "category": 유산소
            }
        ]
        """)
        List<ExerciseRecommendation> exerciseRecommend,

        @Schema(description = "남은 섭취 권장 칼로리", example = "1848")
        Integer remainingFoodKcal,

        @Schema(description = "음식 추천 목록",
                example = """
        [
            {
                "foodName": "샐러드",
                "description": "302kcal로 00을 채우기 효과적이에요",
                "targetNutrientType": "00"
            },
            {
                "foodName": "치즈",
                "description": "부족한 00을 섭취하고 건강을 챙겨요",
                "targetNutrientType": "00"
            }
        ]
        """)
        List<FoodRecommendation> foodRecommend

){
}
