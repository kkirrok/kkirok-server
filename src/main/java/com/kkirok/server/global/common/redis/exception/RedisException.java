package com.kkirok.server.global.common.redis.exception;

import com.kkirok.server.global.common.exception.KkirokException;

public class RedisException extends KkirokException {
    public RedisException(RedisErrorCode baseErrorCode) {
        super(baseErrorCode);
    }

    public RedisException(RedisErrorCode baseErrorCode, Throwable cause) {
        super(baseErrorCode, cause);
    }
}
