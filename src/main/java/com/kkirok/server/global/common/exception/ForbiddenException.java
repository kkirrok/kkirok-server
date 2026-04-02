package com.kkirok.server.global.common.exception;

import com.kkirok.server.global.common.exception.base.BaseErrorCode;

public class ForbiddenException extends KkirokException {

	public ForbiddenException(final BaseErrorCode baseErrorCode) {
		super(baseErrorCode);
	}

	public ForbiddenException(final BaseErrorCode baseErrorCode, String role) {
		super(baseErrorCode, role);
	}

}
