package com.kkirok.server.global.common.exception;


import com.kkirok.server.global.common.exception.base.BaseErrorCode;

public class NotFoundException extends KkirokException {
	public NotFoundException(final BaseErrorCode baseErrorCode) {
		super(baseErrorCode);
	}
}