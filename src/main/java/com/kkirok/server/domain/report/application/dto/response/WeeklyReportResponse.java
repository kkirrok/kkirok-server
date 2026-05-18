package com.kkirok.server.domain.report.application.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.Map;

/**
 * 주간 리포트 응답 DTO.
 *
 * 화면 구성:
 * 1. 칼로리 분석     — avgDailyKcal, kcalFeedback, kcalStatus
 * 2. 영양소 분석     — nutrientFeedbacks (단백질/탄수화물/지방/나트륨 평균 + 상태)
 * 3. 식사 패턴       — mealPatternDescription, slotCounts
 * 4. 다음 주 제안    — nextWeekSuggestions (제목 + 내용 2개)
 */
public record WeeklyReportResponse(

        // ── 1. 칼로리 분석 ────────────────────────────────────────────────
        @Schema(description = "하루 평균 섭취 칼로리", example = "1352")
        @JsonProperty("avgDailyKcal")
        int avgDailyKcal,

        @Schema(description = "칼로리 피드백", example = "권장 칼로리보다 조금 적게 드셨어요")
        @JsonProperty("kcalFeedback")
        String kcalFeedback,

        @Schema(description = "칼로리 상태 (OVER/GOOD/UNDER)", example = "UNDER")
        @JsonProperty("kcalStatus")
        String kcalStatus,

        // ── 2. 영양소 분석 ────────────────────────────────────────────────
        @Schema(description = "영양소별 평균/상태/피드백 목록")
        @JsonProperty("nutrientFeedbacks")
        List<NutrientFeedback> nutrientFeedbacks,

        // ── 3. 식사 패턴 ──────────────────────────────────────────────────
        @Schema(description = "시간대별 총 끼니 수", example = "{\"BREAKFAST\":2,\"LUNCH\":6}")
        @JsonProperty("slotCounts")
        Map<String, Integer> slotCounts,

        @Schema(description = "식사 시간대 패턴 설명")
        @JsonProperty("mealPatternDescription")
        String mealPatternDescription,

        // ── 4. 다음 주 제안 ───────────────────────────────────────────────
        @Schema(description = "다음 주 식단 제안 2개")
        @JsonProperty("nextWeekSuggestions")
        List<Suggestion> nextWeekSuggestions
) {

    public record NutrientFeedback(
            @Schema(description = "영양소 이름", example = "단백질")
            @JsonProperty("nutrient") String nutrient,

            @Schema(description = "하루 평균 섭취량(g 또는 mg)", example = "45.0")
            @JsonProperty("avgAmount") double avgAmount,

            @Schema(description = "영양소 단위", example = "g")
            @JsonProperty("unit") String unit,

            @Schema(description = "상태 (OVER/GOOD/UNDER)", example = "UNDER")
            @JsonProperty("status") String status,

            @Schema(description = "피드백 한 줄", example = "단백질이 조금 부족해요")
            @JsonProperty("feedback") String feedback
    ) {}

    public record Suggestion(
            @Schema(description = "제안 제목", example = "아침 챙기기")
            @JsonProperty("title") String title,

            @Schema(description = "제안 내용")
            @JsonProperty("content") String content
    ) {}
}
