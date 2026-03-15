package com.kkirok.server.domain.member.exception;

import com.kkirok.server.global.common.exception.base.BaseSuccessCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MemberSuccessCode implements BaseSuccessCode {
	/*
	200 Ok
	*/
	SOCIAL_LOGIN_SUCCESS(200, "소셜 로그인/회원가입 성공"),
	LOCAL_SIGN_UP_SUCCESS(200, "로컬 회원가입 성공"),
	LOCAL_LOGIN_SUCCESS(200, "로컬 로그인 성공"),
	EMAIL_VERIFICATION_CODE_SENT(200, "이메일 인증번호 발송 성공"),
	EMAIL_VERIFIED_SUCCESS(200, "이메일 인증 성공"),
	ISSUE_ACCESS_TOKEN_USING_REFRESH_TOKEN(200, "리프레쉬 토큰으로 액세스 토큰 재발급 성공"),
	SIGN_OUT_SUCCESS(200, "로그아웃 성공"),
	USER_DELETE_SUCCESS(200, "회원 탈퇴 성공");

	private final int status;
	private final String message;
}
