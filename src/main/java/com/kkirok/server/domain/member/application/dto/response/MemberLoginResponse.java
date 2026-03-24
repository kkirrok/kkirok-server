package com.kkirok.server.domain.member.application.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record MemberLoginResponse(
	@Schema(description = "액세스 토큰")
	String accessToken,
	@Schema(description = "유저 닉네임")
	String nickname,
	@Schema(description = "시스템 내 유저 역할( 기능 X )", example = "ROLE_USER")
	String role
) {
	public static MemberLoginResponse of(
		final String accessToken,
		final String nickname,
		final String role
	) {
		return new MemberLoginResponse(accessToken, nickname, role);
	}
}
