package com.kkirok.server.global.common.redis.exception;

import com.kkirok.server.global.common.exception.KkirokException;

public class RedisException extends KkirokException {
    public RedisException(final RedisErrorCode baseErrorCode) {
        super(baseErrorCode);
    }
}
