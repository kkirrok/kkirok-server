package com.kkirok.server.domain.member.application.dto.response;

public record LoginSuccessResponse(
	String accessToken,
	String refreshToken,
	String nickname,
	String role,
	boolean onboardingCompleted
) {
	public static LoginSuccessResponse of(
		final String accessToken,
		final String refreshToken,
		final String nickname,
		final String role,
		final boolean onboardingCompleted
	) {
		return new LoginSuccessResponse(accessToken, refreshToken, nickname, role, onboardingCompleted);
	}
}
