package com.kkirok.server.domain.report.application.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.Map;

public record WeeklyReportResponse(

        @Schema(description = "하루 평균 섭취 칼로리", example = "1980")
        @JsonProperty("avgDailyKcal")
        int avgDailyKcal,

        @Schema(description = "주간 총 섭취 칼로리", example = "14700")
        @JsonProperty("totalWeeklyKcal")
        int totalWeeklyKcal,

        @Schema(
                description = "요일별 칼로리 (MON~SUN)",
                example = """
                {
                  "MONDAY": 2100,
                  "TUESDAY": 1850,
                  "WEDNESDAY": 2300,
                  "THURSDAY": 1950,
                  "FRIDAY": 2100,
                  "SATURDAY": 2200,
                  "SUNDAY": 2200
                }
                """
        )
        @JsonProperty("dailyKcals")
        Map<String, Integer> dailyKcals,

        @Schema(description = "칼로리 피드백", example = "권장 칼로리보다 조금 적게 드셨어요")
        @JsonProperty("kcalFeedback")
        String kcalFeedback,

        @Schema(description = "칼로리 상태 (OVER/GOOD/UNDER)", example = "UNDER")
        @JsonProperty("kcalStatus")
        String kcalStatus,

        @Schema(description = "영양소별 평균/상태/피드백 목록")
        @JsonProperty("nutrientFeedbacks")
        List<NutrientFeedback> nutrientFeedbacks,

        @Schema(description = "시간대별 총 끼니 수")
        @JsonProperty("slotCounts")
        Map<String, Integer> slotCounts,

        @Schema(description = "식사 시간대 패턴 설명", example = "특정 시간대에 칼로리 섭취가 집중되는 경향이 있어요")
        @JsonProperty("mealPatternDescription")
        String mealPatternDescription,

        @Schema(description = "다음 주 식단 제안 2개")
        @JsonProperty("nextWeekSuggestions")
        List<Suggestion> nextWeekSuggestions

) {
        public record NutrientFeedback(
                @Schema(description = "영양소명", example = "단백질")
                @JsonProperty("nutrient") String nutrient,

                @Schema(description = "일평균 섭취량", example = "45")
                @JsonProperty("avgAmount") double avgAmount,

                @Schema(description = "단위", example = "g")
                @JsonProperty("unit") String unit,

                @Schema(description = "상태 (OVER/GOOD/UNDER)", example = "UNDER")
                @JsonProperty("status") String status,

                @Schema(description = "피드백 문장", example = "단백질이 조금 부족해요")
                @JsonProperty("feedback") String feedback
        ) {}

        public record Suggestion(
                @Schema(description = "제안 제목", example = "영양소를 고르게 섭취해요")
                @JsonProperty("title") String title,

                @Schema(description = "제안 내용", example = "다양한 식품군을 포함하여 영양소의 균형을 맞추는 것이 중요합니다.")
                @JsonProperty("content") String content
        ) {}
}