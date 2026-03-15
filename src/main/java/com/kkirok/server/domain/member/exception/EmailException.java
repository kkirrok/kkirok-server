package com.kkirok.server.domain.member.exception;

import com.kkirok.server.global.common.exception.KkirokException;

public class EmailException extends KkirokException {
    public EmailException(final EmailErrorCode baseErrorCode) {
        super(baseErrorCode);
    }
}
