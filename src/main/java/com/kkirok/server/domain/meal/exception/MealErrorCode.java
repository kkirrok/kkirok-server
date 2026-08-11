package com.kkirok.server.domain.meal.exception;

import com.kkirok.server.global.common.exception.base.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MealErrorCode implements BaseErrorCode {

    /*
     * 400 Bad Request
     */
    IMAGE_FILE_REQUIRED(
            400,
            "이미지 파일은 필수입니다."
    ),

    UNRECOGNIZABLE_MEAL_IMAGE(
            400,
            "음식이 선명하게 보이는 사진을 업로드해주세요."
    ),

    /*
     * 409 Conflict
     */
    NOT_FOOD_IMAGE(
            409,
            "음식이 아닌 사진은 기록할 수 없습니다."
    ),

    /*
     * 500 Internal Server Error
     */
    MEAL_ANALYSIS_FAILED(
            500,
            "이미지 분석 중 오류가 발생했습니다."
    ),

    /*
     * 403 Forbidden
     */
    MEAL_FORBIDDEN(
            403,
            "해당 식단에 대한 권한이 없습니다."
    ),

    /*
     * 404 Not Found
     */
    MEAL_NOT_FOUND(
            404,
            "식단 기록이 없습니다."
    );

    private final int status;
    private final String message;
}
