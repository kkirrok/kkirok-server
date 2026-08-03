package com.kkirok.server.domain.meal.application.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "오늘의 영양성분 총합 응답")
public record TodayNutritionSummaryResponse(

        @Schema(description = "총 칼로리", example = "1520")
        Integer totalKcal,

        @Schema(description = "총 탄수화물(g)", example = "210")
        Long totalCarbohydrateG,

        @Schema(description = "총 단백질(g)", example = "75")
        Long totalProteinG,

        @Schema(description = "총 지방(g)", example = "42")
        Long totalFatG,

        @Schema(description = "총 당(g)", example = "28")
        Long totalSugarG,

        @Schema(description = "총 나트륨(mg)", example = "1800")
        Long totalSodiumMg,

        @Schema(description = "권장 칼로리(kcal)", example = "2000")
        Integer recommendedKcal,

        @Schema(description = "권장 탄수화물(g)", example = "250")
        Integer recommendedCarbohydrateG,

        @Schema(description = "권장 단백질(g)", example = "100")
        Integer recommendedProteinG,

        @Schema(description = "권장 지방(g)", example = "67")
        Integer recommendedFatG,

        @Schema(description = "권장 당(g)", example = "50")
        Integer recommendedSugarG,

        @Schema(description = "권장 나트륨(mg)", example = "2000")
        Integer recommendedSodiumMg
) {
}