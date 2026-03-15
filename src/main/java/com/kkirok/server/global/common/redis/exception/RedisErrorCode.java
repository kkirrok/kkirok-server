package com.kkirok.server.global.common.redis.exception;

import com.kkirok.server.global.common.exception.base.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RedisErrorCode implements BaseErrorCode {
    REDIS_SAVE_FAILED(500, "Redis 데이터 저장에 실패했습니다."),
    REDIS_READ_FAILED(500, "Redis 데이터 조회에 실패했습니다."),
    REDIS_DELETE_FAILED(500, "Redis 데이터 삭제에 실패했습니다.");

    private final int status;
    private final String message;
}
