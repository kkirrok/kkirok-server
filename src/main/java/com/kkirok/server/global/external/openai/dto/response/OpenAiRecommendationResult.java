package com.kkirok.server.global.external.openai.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * OpenAI 식단/운동 추천 응답 파싱 DTO.
 * meal-recommendation-v1.txt 프롬프트의 JSON 응답을 파싱합니다.
 */
public record OpenAiRecommendationResult(

        @JsonProperty("exerciseRecommendations")
        List<Exercise> exerciseRecommendations,

        @JsonProperty("foodRecommendations")
        List<Food> foodRecommendations
) {

    public record Exercise(
            @JsonProperty("exerciseName") String exerciseName,
            @JsonProperty("description")  String description,
            @JsonProperty("category")     String category,
            @JsonProperty("emoji")        String emoji
    ) {}

    public record Food(
            @JsonProperty("foodName")           String foodName,
            @JsonProperty("description")        String description,
            @JsonProperty("targetNutrientType") String targetNutrientType,
            @JsonProperty("emoji")              String emoji
    ) {}
}
