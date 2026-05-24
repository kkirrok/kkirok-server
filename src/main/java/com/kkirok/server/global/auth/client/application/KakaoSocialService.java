package com.kkirok.server.global.auth.client.application;

import com.kkirok.server.domain.member.domain.SocialType;
import com.kkirok.server.global.auth.client.dto.MemberInfoResponse;
import com.kkirok.server.global.auth.client.dto.MemberLoginRequest;
import com.kkirok.server.global.auth.client.kakao.KakaoApiClient;
import com.kkirok.server.global.auth.client.kakao.response.KakaoUserResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KakaoSocialService implements SocialService {

	private final KakaoApiClient kakaoApiClient;

	@Transactional
	@Override
	public MemberInfoResponse login(final MemberLoginRequest loginRequest) {
		return getLoginDto(loginRequest.socialType(), getUserInfo(loginRequest.accessToken()));
	}

	private KakaoUserResponse getUserInfo(
		final String accessToken
	) {
		KakaoUserResponse kakaoUserResponse = kakaoApiClient.getUserInformation("Bearer " + accessToken);
		return kakaoUserResponse;
	}

	private MemberInfoResponse getLoginDto(
		final SocialType socialType,
		final KakaoUserResponse kakaoUserResponse
	) {
		Long socialId = kakaoUserResponse.id();
		return MemberInfoResponse.of(
			socialId,
			String.valueOf(socialId),
			kakaoUserResponse.kakaoAccount().profile().nickname(),
			kakaoUserResponse.kakaoAccount().email(),
			socialType
		);
	}

}
