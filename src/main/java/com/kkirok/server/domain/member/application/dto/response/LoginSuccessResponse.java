package com.kkirok.server.domain.member.application.dto.response;

import com.kkirok.server.domain.terms.application.dto.response.TermsResponse;

import java.util.List;

public record LoginSuccessResponse(
	String accessToken,
	String refreshToken,
	String nickname,
	String role,
	boolean onboardingCompleted,
	List<TermsResponse> pendingTermsAgree
) {
	public static LoginSuccessResponse of(
		final String accessToken,
		final String refreshToken,
		final String nickname,
		final String role,
		final boolean onboardingCompleted,
		final List<TermsResponse> pendingTermsAgree
	) {
		return new LoginSuccessResponse(accessToken, refreshToken, nickname, role, onboardingCompleted, pendingTermsAgree);
	}

	public static LoginSuccessResponse of(
		final String accessToken,
		final String refreshToken,
		final String nickname,
		final String role,
		final boolean onboardingCompleted
	) {
		return of(accessToken, refreshToken, nickname, role, onboardingCompleted, List.of());
	}
}
