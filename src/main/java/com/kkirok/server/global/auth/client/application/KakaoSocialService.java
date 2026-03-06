package com.kkirok.server.global.auth.client.application;

import com.kkirok.server.domain.member.domain.SocialType;
import com.kkirok.server.global.auth.client.dto.MemberInfoResponse;
import com.kkirok.server.global.auth.client.dto.MemberLoginRequest;
import com.kkirok.server.global.auth.client.kakao.KakaoApiClient;
import com.kkirok.server.global.auth.client.kakao.KakaoAuthApiClient;
import com.kkirok.server.global.auth.client.kakao.response.KakaoAccessTokenResponse;
import com.kkirok.server.global.auth.client.kakao.response.KakaoUserResponse;
import com.kkirok.server.global.auth.jwt.exception.TokenErrorCode;
import com.kkirok.server.global.common.exception.UnauthorizedException;
import feign.FeignException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class KakaoSocialService implements SocialService {

	private static final String AUTH_CODE = "authorization_code";

	@Value("${spring.security.oauth2.client.registration.kakao.redirect-uri}")
	private String redirectUri;

	@Value("${spring.security.oauth2.client.registration.kakao.client-id}")
	private String clientId;

	@Value("${spring.security.oauth2.client.registration.kakao.client-secret}")
	private String clientSecret;

	private final KakaoApiClient kakaoApiClient;
	private final KakaoAuthApiClient kakaoAuthApiClient;

	@Transactional
	@Override
	public MemberInfoResponse login(
		final String authorizationCode,
		final MemberLoginRequest loginRequest
	) {
		String accessToken;
		try {
			// 인가 코드로 Access Token + Refresh Token 받아오기
			accessToken = getOAuth2Authentication(authorizationCode);
		} catch (FeignException e) {
			log.error("Kakao token exchange failed. status={}, response={}, redirectUri={}",
				e.status(), e.contentUTF8(), redirectUri);
			throw new UnauthorizedException(TokenErrorCode.AUTHENTICATION_CODE_EXPIRED);
		}
		// Access Token으로 유저 정보 불러오기
		return getLoginDto(loginRequest.socialType(), getUserInfo(accessToken));
	}

	private String getOAuth2Authentication(
		final String authorizationCode
	) {
		KakaoAccessTokenResponse response = kakaoAuthApiClient.getOAuth2AccessToken(
			AUTH_CODE,
			clientId,
			clientSecret,
			redirectUri,
			authorizationCode
		);
		log.info("Received OAuth2 authentication response: {}", response);
		return response.accessToken();
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
