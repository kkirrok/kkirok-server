package com.kkirok.server.global.common.exception;

import com.kkirok.server.global.common.exception.base.BaseErrorCode;

public class InternalServerException extends KkirokException {
	public InternalServerException(final BaseErrorCode baseErrorCode) {
		super(baseErrorCode);
	}

	public InternalServerException(final BaseErrorCode baseErrorCode, final Throwable cause) {
		super(baseErrorCode, cause);
	}
}
