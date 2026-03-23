package com.kkirok.server.domain.report.exception;

import com.kkirok.server.global.common.exception.base.BaseSuccessCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ReportSuccessCode implements BaseSuccessCode {
    /*
        200 ok
    */
    WEEKLY_REPORT_GET_SUCCESS(200, "주간 리포트 조회 성공");

    private final int status;
    private final String message;
}
