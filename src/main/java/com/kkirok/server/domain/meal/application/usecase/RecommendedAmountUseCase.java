package com.kkirok.server.domain.meal.application.usecase;

import com.kkirok.server.global.external.openai.dto.response.OpenAiRecommendedAmountResponse;

import java.time.LocalDate;

public interface RecommendedAmountUseCase {

    int getRecommendedKcal(Long memberId); // 오늘 날짜 기준 - 오늘 통합 권장 칼로리
    int getRecommendedKcal(Long memberId, LocalDate date); // 특정 날짜 기준 - 오늘 통합 권장 칼로리
    OpenAiRecommendedAmountResponse getAllRecommendedNutrition(Long memberId); // 오늘 날짜 기준 - 영양소별 추천 섭취량
    OpenAiRecommendedAmountResponse getAllRecommendedNutrition(Long memberId, LocalDate date); // 특정 날짜 기준 - 영양소별 추천 섭취량

}
