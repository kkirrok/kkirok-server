package com.kkirok.server.domain.meal.exception;

import com.kkirok.server.global.common.exception.base.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum MealErrorCode implements BaseErrorCode {
    /*
	400 BadRequest
	*/

    IMAGE_FILE_REQUIRED(400, "이미지 파일은 필수입니다."),
    /*
    403 Forbidden
    */
    MEAL_FORBIDDEN(403, "해당 식단에 대한 권한이 없습니다."),
    /*
	404 NotFound
	*/
    MEAL_NOT_FOUND(404, "식단 기록이 없습니다.");


    private final int status;
    private final String message;
}
