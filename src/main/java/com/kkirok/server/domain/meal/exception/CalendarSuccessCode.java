package com.kkirok.server.domain.meal.exception;

import com.kkirok.server.global.common.exception.base.BaseSuccessCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CalendarSuccessCode implements BaseSuccessCode {

    CALENDAR_SUCCESS_CODE(200, "월간 상태 조회 성공"),
    CALENDAR_DAILY_SUCCESS_CODE(200, "캘린더 개별 조회 성공");

    public final int status;
    public final String message;

}
