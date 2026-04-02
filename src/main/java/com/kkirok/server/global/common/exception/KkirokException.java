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

	public KkirokException(BaseErrorCode baseErrorCode, Throwable cause) {
		super(baseErrorCode.getMessage(), cause);
		this.baseErrorCode = baseErrorCode;
	}

	public KkirokException(BaseErrorCode baseErrorCode, String role) {
		super(baseErrorCode.getMessage() + " - 요구 권한 : " + role);
	 	this.baseErrorCode = baseErrorCode;
	}

}
