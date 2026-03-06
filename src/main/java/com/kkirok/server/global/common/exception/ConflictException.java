package com.kkirok.server.global.common.exception;

import com.kkirok.server.global.common.exception.base.BaseErrorCode;

public class ConflictException extends KkirokException {
	public ConflictException(final BaseErrorCode baseErrorCode) {
		super(baseErrorCode);
	}
}