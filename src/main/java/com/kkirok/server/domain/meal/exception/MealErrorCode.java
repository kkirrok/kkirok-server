package com.kkirok.server.domain.meal.exception;

import com.kkirok.server.global.common.exception.base.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MealErrorCode implements BaseErrorCode {
    /*
	400 BadRequest
	*/

    /*
	404 NotFound
	*/
    MEAL_NOT_FOUND(404, "식단 기록이 없습니다.");

    private final int status;
    private final String message;
}
