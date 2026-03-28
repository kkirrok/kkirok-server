package com.kkirok.server.domain.member.exception;

import com.kkirok.server.global.common.exception.base.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MemberErrorCode implements BaseErrorCode {
	/*
	400 BadRequest
	*/
	SOCIAL_TYPE_BAD_REQUEST(400, "로그인 요청이 유효하지 않습니다."),
	LOCAL_LOGIN_BAD_REQUEST(400, "로컬 로그인 요청이 유효하지 않습니다."),
	ONBOARDING_HABIT_MAX_COUNT(400, "식습관 유형은 최대 5개까지입니다."),

	/*
	401 Unauthorized
	*/
	LOCAL_LOGIN_FAILED(401, "이메일 또는 비밀번호가 올바르지 않습니다."),

	/*
	403 Forbidden
	*/
	ACCOUNT_RECOVERY_FORBIDDEN(403, "본인 계정만 비밀번호를 재설정할 수 있습니다."),

	/*
	409 Conflict
	*/
	LOCAL_EMAIL_ALREADY_EXISTS(409, "이미 사용 중인 이메일입니다."),

	/*
	404 NotFound
	*/
	ACCOUNT_RECOVERY_INFO_MISMATCH(404, "일치하는 회원 정보가 없습니다."),
	MEMBER_NOT_FOUND(404, "회원이 없습니다"),

	;

	private final int status;
	private final String message;
}
