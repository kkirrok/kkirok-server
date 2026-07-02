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
	INVALID_PASSWORD_FORMAT(400, "비밀번호는 영문, 숫자, 특수문자를 각각 1개 이상 포함해야 합니다."),
	INVALID_PHONE_FORMAT(400, "휴대전화 번호는 010-1111-1111 형식이어야 합니다."),
	INVALID_PURPOSE_INFO(400, "목표는 필수입니다."),
	INVALID_HABIT_INFO(400, "목표 개수는 1개 이상 5개 이하이어야 합니다."),

	/*
	401 Unauthorized
	*/
	AUTHENTICATION_REQUIRED(401, "인증이 필요합니다."),
	LOCAL_LOGIN_PASSWORD_MISMATCH(401, "비밀번호가 올바르지 않습니다."),

	/*
	403 Forbidden
	*/
	ACCOUNT_RECOVERY_FORBIDDEN(403, "본인 계정만 비밀번호를 재설정할 수 있습니다."),
	SOCIAL_ACCOUNT_LOCAL_LOGIN_FORBIDDEN(403, "소셜 로그인 계정입니다. 소셜 로그인으로 진행해주세요."),
	USER_LOGIN_FOR_ADMIN_ACCOUNT(403, "관리자 계정은 일반 사용자 로그인으로 로그인할 수 없습니다."),
	ADMIN_LOGIN_FOR_USER_ACCOUNT(403, "일반 사용자 계정은 관리자 로그인으로 로그인할 수 없습니다."),
	INVALID_ROLE(403, "해당 기능에 대한 권한이 없습니다."),

	/*
	409 Conflict
	*/
	LOCAL_EMAIL_ALREADY_EXISTS(409, "이미 사용 중인 이메일입니다."),
	DELETED_MEMBER(409, "탈퇴한 회원입니다."),

	/*
	404 NotFound
	*/
	LOCAL_ACCOUNT_NOT_FOUND(404, "로컬 계정을 찾을 수 없습니다."),
	ACCOUNT_RECOVERY_INFO_MISMATCH(404, "일치하는 회원 정보가 없습니다."),
	MEMBER_NOT_FOUND(404, "회원이 없습니다"),
	NOTIFICATION_AGREE_NOT_FOUND(404, "알림 허용 설정을 찾을 수 없습니다."),
	NOTIFICATION_ALL_AGREE_TYPE_MISMATCH(400, "전체 알림 설정 시 모든 유형을 포함해야 합니다."),
	NOTIFICATION_ALL_AGREE_VALUE_MISMATCH(400, "전체 알림 설정 시 모든 유형의 허용 여부가 동일해야 합니다."),
	NOTIFICATION_AGREE_DUPLICATE_TYPE(400, "중복된 알림 유형이 있습니다."),

	;

	private final int status;
	private final String message;
}
