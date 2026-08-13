package com.kkirok.server.domain.terms.exception;

import com.kkirok.server.global.common.exception.base.BaseSuccessCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TermsSuccessCode implements BaseSuccessCode {
	/*
	200 Ok
	*/
	TERMS_LIST_SUCCESS(200, "약관 목록 조회 성공"),
	TERMS_AGREE_SUCCESS(200, "약관 동의 성공"),

	;

	private final int status;
	private final String message;
}
