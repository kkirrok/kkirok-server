package com.kkirok.server.admin.exception;

import com.kkirok.server.global.common.exception.base.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AdminErrorCode implements BaseErrorCode {

	ADMIN_EMAIL_DOMAIN_INVALID(400, "관리자 계정 신청 이메일은 @kkirok.com 이어야 합니다."),
	ADMIN_ACCOUNT_REQUEST_STATUS_INVALID(400, "관리자 계정 요청 처리 상태가 올바르지 않습니다."),
	ADMIN_ACCOUNT_REQUEST_ALREADY_EXISTS(409, "이미 처리 대기 중인 관리자 계정 요청이 있습니다."),
	ADMIN_ACCOUNT_REQUEST_NOT_FOUND(404, "관리자 계정 요청을 찾을 수 없습니다."),
	ADMIN_ACCOUNT_REQUEST_NOT_PENDING(409, "이미 처리된 관리자 계정 요청입니다."),
	ADMIN_ACCOUNT_EMAIL_ALREADY_EXISTS(409, "이미 사용 중인 관리자 이메일입니다.");

	private final int status;
	private final String message;
}
