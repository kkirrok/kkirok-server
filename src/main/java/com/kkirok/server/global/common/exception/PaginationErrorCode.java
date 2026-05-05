package com.kkirok.server.global.common.exception;

import com.kkirok.server.global.common.exception.base.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaginationErrorCode implements BaseErrorCode {
    INVALID_PAGING_REQUEST(400, "페이지 번호는 0 이상, 페이지 크기는 1 이상 50 이하이어야 합니다."),
    PAGE_OUT_OF_RANGE(400, "요청한 페이지가 전체 페이지 범위를 초과했습니다."),
    ;

    private final int status;
    private final String message;
}
