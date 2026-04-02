package com.kkirok.server.admin.exception;

import com.kkirok.server.global.common.exception.base.BaseSuccessCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AdminSuccessCode implements BaseSuccessCode {

	ADMIN_ACCOUNT_REQUEST_CREATED(201, "관리자 계정 생성 요청 성공"),
	ADMIN_ACCOUNT_REQUEST_LIST_SUCCESS(200, "관리자 계정 요청 목록 조회 성공"),
	ADMIN_ACCOUNT_REQUEST_APPROVED(200, "관리자 계정 승인 성공"),
	ADMIN_ACCOUNT_REQUEST_STATUS_UPDATED(200, "관리자 계정 요청 상태 변경 성공"),
	ADMIN_LOCAL_LOGIN_SUCCESS(200, "관리자 로그인 성공");

	private final int status;
	private final String message;
}
