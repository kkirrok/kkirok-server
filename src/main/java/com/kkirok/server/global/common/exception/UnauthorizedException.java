package com.kkirok.server.global.common.exception;

import com.kkirok.server.global.common.exception.base.BaseErrorCode;

public class UnauthorizedException extends KkirokException {
	public UnauthorizedException(final BaseErrorCode baseErrorCode) {
		super(baseErrorCode);
	}
}
