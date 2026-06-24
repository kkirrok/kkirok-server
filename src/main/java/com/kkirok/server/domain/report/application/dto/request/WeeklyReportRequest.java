package com.kkirok.server.domain.report.application.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/**
 * OpenAI 주간 리포트 프롬프트에 넘길 한 주 식단 요약 데이터.
 *
 * @param avgDailyKcal     하루 평균 섭취 칼로리
 * @param avgProteinG      하루 평균 단백질(g)
 * @param avgCarbohydrateG 하루 평균 탄수화물(g)
 * @param avgSugarG        하루 평균 당(g)
 * @param avgFatG          하루 평균 지방(g)
 * @param avgSodiumMg      하루 평균 나트륨(mg)
 * @param recordedDays     기록된 일수 (최대 7)
 * @param slotCounts       시간대별 총 끼니 수 (키: BREAKFAST/LUNCH/DINNER/SNACK/MIDNIGHT_SNACK)
 * @param dailyKcals
 */
public record WeeklyReportRequest(

        @JsonProperty("avgDailyKcal")
        int avgDailyKcal,

        @JsonProperty("avgProteinG")
        double avgProteinG,

        @JsonProperty("avgCarbohydrateG")
        double avgCarbohydrateG,

        @JsonProperty("avgSugarG")
        double avgSugarG,

        @JsonProperty("avgFatG")
        double avgFatG,

        @JsonProperty("avgSodiumMg")
        double avgSodiumMg,

        @JsonProperty("recordedDays")
        int recordedDays,

        @JsonProperty("slotCounts")
        Map<String, Integer> slotCounts,
        Map<String, Integer> dailyKcals) {}
