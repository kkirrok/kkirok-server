package com.kkirok.server.global.common.exception;

import com.kkirok.server.global.common.exception.base.BaseErrorCode;

public class BadRequestException extends KkirokException {
	public BadRequestException(final BaseErrorCode baseErrorCode) {
		super(baseErrorCode);
	}
}
