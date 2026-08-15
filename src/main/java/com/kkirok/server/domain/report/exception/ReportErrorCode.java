package com.kkirok.server.domain.report.exception;

import com.kkirok.server.global.common.exception.base.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ReportErrorCode implements BaseErrorCode {
    /*
    400 BadRequest
    */
    WEEKLY_REPORT_NOT_ENDED(400, "아직 종료되지 않은 주의 리포트는 조회할 수 없습니다. 지난주 이전의 리포트를 조회해주세요."),

    /*
    404 NotFound
    */
    WEEKLY_REPORT_NOT_FOUND(404, "주간 리포트를 찾을 수 없습니다.");

    private final int status;
    private final String message;
}