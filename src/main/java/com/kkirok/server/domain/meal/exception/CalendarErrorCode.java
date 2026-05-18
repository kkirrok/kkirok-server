package com.kkirok.server.domain.meal.exception;

import com.kkirok.server.global.common.exception.base.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CalendarErrorCode implements BaseErrorCode {

    /*
     400 BadRequest
     */
    INVALID_YEAR(400, "년도 정보가 올바르지 않습니다."),
    INVALID_MONTH(400, "월 정보는 1부터 12 사이여야 합니다."),
    INVALID_START_DAY_OF_WEEK(400, "시작 요일은 0부터 6 사이여야 합니다."),
    INVALID_DATE(400, "날짜 정보가 올바르지 않습니다."),

    /*
     403 Forbidden
     */
    CALENDAR_FORBIDDEN(403, "캘린더 조회 권한이 없습니다."),

    /*
     404 NotFound
     */
    CALENDAR_NOT_FOUND(404, "캘린더 정보를 찾을 수 없습니다.");

    private final int status;
    private final String message;
}