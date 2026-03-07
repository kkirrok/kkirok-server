package com.kkirok.server.global.auth.client.dto;

import com.kkirok.server.domain.member.domain.SocialType;

public record MemberInfoResponse(
	Long socialId,
	String providerUserId,
	String nickname,
	String email,
	SocialType socialType
) {
	public static MemberInfoResponse of(
		final Long socialId,
		final String providerUserId,
		final String nickname,
		final String email,
		final SocialType socialType
	) {
		return new MemberInfoResponse(socialId, providerUserId, nickname, email, socialType);
	}
}
