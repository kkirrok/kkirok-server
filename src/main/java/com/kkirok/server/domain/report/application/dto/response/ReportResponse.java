package com.kkirok.server.domain.report.application.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record ReportResponse(
        @Schema(description = "리포트 아이디", example = "1")
        Long reportId,
        @Schema(description = "주 평균 섭취 칼로리", example = "1980")
        Integer avgKcal,
        @Schema(description = "총 섭취 칼로리", example = "14700")
        Integer totalKcal,
        @Schema(description = "평균 섭취 단백질", example = "20")
        Integer avgProtein,
        @Schema(description = "평균 섭취 탄수화물", example = "13")
        Integer avgCarbohydrate,
        @Schema(description = "평균 섭취 당", example = "5")
        Integer avgSugar,
        @Schema(description = "평균 섭취 지방", example = "3")
        Integer avgFat,
        @Schema(description = "평균 섭취 나트륨", example = "6")
        Integer avgSodium,
        @Schema(description = "섭취 영양성분 분석 멘트", example = "전체적으로 적게 섭취하는 경향이 있어요. 현재 탐수화물 섭취가 너무 낮고, 당 섭취가 높아요")
        String intakeComment,
        @Schema(description = "섭취 패턴 분석 제목", example = "주로 저녁에 칼로리가 높은 음식을 먹었어요")
        String mainKcalTitle,
        @Schema(description = "섭취 패턴 분석 내용", example = "전체의 40%의 칼로리를 저녁에 섭취하였고, 탄수화물은 30g을 저녁마다 먹었어요")
        String mainKcalComment,

        @Schema(description = "다음에는 이렇게 해보아요")
        List<SuggestionResponse> suggestions
) {
}