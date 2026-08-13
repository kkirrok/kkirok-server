package com.kkirok.server.domain.terms.exception;

import com.kkirok.server.global.common.exception.base.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TermsErrorCode implements BaseErrorCode {
	/*
	400 BadRequest
	*/
	REQUIRED_TERMS_NOT_AGREED(400, "필수 약관은 동의해야 합니다."),
	TERMS_TYPE_DUPLICATE(400, "중복된 약관 유형이 있습니다."),

	/*
	403 Forbidden
	*/
	TERMS_AGREEMENT_REQUIRED(403, "필수 약관에 동의해야 이용할 수 있습니다."),

	/*
	404 NotFound
	*/
	TERMS_NOT_FOUND(404, "약관을 찾을 수 없습니다."),

	;

	private final int status;
	private final String message;
}
