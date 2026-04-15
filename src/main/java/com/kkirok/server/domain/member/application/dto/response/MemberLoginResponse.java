package com.kkirok.server.domain.member.application.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record MemberLoginResponse(
	@Schema(description = "로그인 성공 후 발급된 액세스 토큰", example = "eyJhbGciOiJIUzI1NiJ9...")
	String accessToken,
	@Schema(description = "로그인한 사용자의 닉네임", example = "끼록이")
	String nickname,
	@Schema(description = "시스템 내부 역할 값", example = "ROLE_USER")
	String role,
	@Schema(description = "온보딩 완료 여부", example = "false")
	boolean onboardingCompleted
) {
	public static MemberLoginResponse of(
		final String accessToken,
		final String nickname,
		final String role,
		final boolean onboardingCompleted
	) {
		return new MemberLoginResponse(accessToken, nickname, role, onboardingCompleted);
	}
}
