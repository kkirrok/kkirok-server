package com.kkirok.server.global.external.r2.exception;

import com.kkirok.server.global.common.exception.KkirokException;

public class R2Exception extends KkirokException {
    public R2Exception(final R2ErrorCode baseErrorCode) {
        super(baseErrorCode);
    }
}
