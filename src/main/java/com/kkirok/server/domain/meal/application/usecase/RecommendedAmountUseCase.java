package com.kkirok.server.domain.meal.application.usecase;

import com.kkirok.server.global.external.openai.dto.response.OpenAiRecommendedAmountResponse;

import java.time.LocalDate;

/**
 * OpenAI에 요청해서 권장섭취량 받아오는 기능 인터페이스
 * 권장 섭취 칼로리, 영양소별 권장 섭취량을 반환합니다.
 *
 */
public interface RecommendedAmountUseCase {

    int getRecommendedKcal(Long memberId); // 오늘 날짜 기준 - 오늘 통합 권장 칼로리
    int getRecommendedKcal(Long memberId, LocalDate date); // 특정 날짜 기준 - 오늘 통합 권장 칼로리
    OpenAiRecommendedAmountResponse getAllRecommendedNutrition(Long memberId); // 오늘 날짜 기준 - 영양소별 추천 섭취량
    OpenAiRecommendedAmountResponse getAllRecommendedNutrition(Long memberId, LocalDate date); // 특정 날짜 기준 - 영양소별 추천 섭취량

}
