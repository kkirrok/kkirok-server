package com.kkirok.server.domain.meal.exception;

import com.kkirok.server.global.common.exception.base.BaseSuccessCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MealSuccessCode implements BaseSuccessCode {
    /*
	200 Ok
	*/
    MEAL_GET_SUCCESS(200, "식단 조회 성공"),
    TODAY_STATUS_GET_SUCCESS(200, "오늘의 상태 조회 성공"),
    RECOMMENDATION_GET_SUCCESS(200, "추천 조회 성공"),
    MEAL_RECORD_SUCCESS(200, "식단 기록 성공"),
    MEAL_SCAN_SUCCESS(200, "음식 스캔 분석 성공"),
    MEAL_UPDATE_SUCCESS(200, "식단 수정 성공"),
    MEAL_DELETE_SUCCESS(200, "식단 삭제 성공"),
    FOOD_SEARCH_SUCCESS(200, "음식 검색 성공"),
    TODAY_NUTRITION_SUMMARY_GET_SUCCESS(200, "오늘의 영양성분 요약 조회 성공"),
    YESTERDAY_PICKS_GET_SUCCESS(200, "어제 이 시간대 픽 조회 성공");

    private final int status;
    private final String message;
}