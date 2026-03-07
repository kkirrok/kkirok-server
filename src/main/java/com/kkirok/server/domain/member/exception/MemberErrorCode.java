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

	/*
	401 Unauthorized
	*/
	LOCAL_LOGIN_FAILED(401, "이메일 또는 비밀번호가 올바르지 않습니다."),

	/*
	409 Conflict
	*/
	LOCAL_EMAIL_ALREADY_EXISTS(409, "이미 사용 중인 이메일입니다."),

	/*
	404 NotFound
	*/
	MEMBER_NOT_FOUND(404, "회원이 없습니다");

	private final int status;
	private final String message;
}
