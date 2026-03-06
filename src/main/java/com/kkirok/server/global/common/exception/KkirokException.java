package com.kkirok.server.global.common.exception;


import com.kkirok.server.global.common.exception.base.BaseErrorCode;
import lombok.Getter;

@Getter
public class KkirokException extends RuntimeException {
	private final BaseErrorCode baseErrorCode;

	public KkirokException(BaseErrorCode baseErrorCode) {
		super(baseErrorCode.getMessage());
		this.baseErrorCode = baseErrorCode;
	}
}
