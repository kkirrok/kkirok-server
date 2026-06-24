package com.kkirok.server.global.external.openai.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * OpenAI 주간 리포트 응답 파싱 DTO.
 * meal-weekly-report-v1.txt 프롬프트의 JSON 응답을 파싱합니다.
 */
public record OpenAiWeeklyReportResult(

        @JsonProperty("kcalFeedback")
        String kcalFeedback,

        @JsonProperty("kcalStatus")
        String kcalStatus,

        @JsonProperty("nutrientFeedbacks")
        List<NutrientFeedback> nutrientFeedbacks,

        @JsonProperty("mealPatternDescription")
        String mealPatternDescription,

        @JsonProperty("nextWeekSuggestions")
        List<Suggestion> nextWeekSuggestions
) {

    public record NutrientFeedback(
            @JsonProperty("nutrient") String nutrient,
            @JsonProperty("avgG")     Double avgG,       // 단백질/탄수화물/당/지방
            @JsonProperty("avgMg")    Double avgMg,      // 나트륨
            @JsonProperty("status")   String status,     // OVER / GOOD / UNDER
            @JsonProperty("feedback") String feedback
    ) {}

    public record Suggestion(
            @JsonProperty("title")   String title,
            @JsonProperty("content") String content
    ) {}
}
