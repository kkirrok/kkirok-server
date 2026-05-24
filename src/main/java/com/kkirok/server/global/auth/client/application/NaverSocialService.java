package com.kkirok.server.global.auth.client.application;

import com.kkirok.server.domain.member.domain.SocialType;
import com.kkirok.server.global.auth.client.dto.MemberInfoResponse;
import com.kkirok.server.global.auth.client.dto.MemberLoginRequest;
import com.kkirok.server.global.auth.client.naver.NaverApiClient;
import com.kkirok.server.global.auth.client.naver.response.NaverUserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NaverSocialService implements SocialService {

	private final NaverApiClient naverApiClient;

	@Override
	public MemberInfoResponse login(MemberLoginRequest loginRequest) {
		NaverUserResponse userResponse = naverApiClient.getUserInformation("Bearer " + loginRequest.accessToken());
		return toMemberInfo(userResponse, loginRequest.socialType());
	}

	private MemberInfoResponse toMemberInfo(NaverUserResponse userResponse, SocialType socialType) {
		String providerUserId = userResponse.response().id();
		Long syntheticSocialId = toStableLong(providerUserId);
		return MemberInfoResponse.of(
			syntheticSocialId,
			providerUserId,
			userResponse.response().nickname(),
			userResponse.response().email(),
			socialType
		);
	}

	// Naver id is string-based; keep deterministic Long for existing Member schema compatibility.
	private Long toStableLong(String providerUserId) {
		long hash = providerUserId.hashCode();
		return hash < 0 ? -hash : hash;
	}
}
