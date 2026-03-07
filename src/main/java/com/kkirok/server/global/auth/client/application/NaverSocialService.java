package com.kkirok.server.global.auth.client.application;

import com.kkirok.server.domain.member.domain.SocialType;
import com.kkirok.server.domain.member.exception.MemberErrorCode;
import com.kkirok.server.global.auth.client.dto.MemberInfoResponse;
import com.kkirok.server.global.auth.client.dto.MemberLoginRequest;
import com.kkirok.server.global.auth.client.naver.NaverApiClient;
import com.kkirok.server.global.auth.client.naver.NaverAuthApiClient;
import com.kkirok.server.global.auth.client.naver.response.NaverAccessTokenResponse;
import com.kkirok.server.global.auth.client.naver.response.NaverUserResponse;
import com.kkirok.server.global.auth.jwt.exception.TokenErrorCode;
import com.kkirok.server.global.common.exception.BadRequestException;
import com.kkirok.server.global.common.exception.UnauthorizedException;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class NaverSocialService implements SocialService {

	private static final String AUTH_CODE = "authorization_code";

	@Value("${spring.security.oauth2.client.registration.naver.client-id}")
	private String clientId;

	@Value("${spring.security.oauth2.client.registration.naver.client-secret}")
	private String clientSecret;

	private final NaverApiClient naverApiClient;
	private final NaverAuthApiClient naverAuthApiClient;

	@Override
	public MemberInfoResponse login(String authorizationToken, MemberLoginRequest loginRequest) {
		String state = loginRequest.state();
		if (state == null || state.isBlank()) {
			throw new BadRequestException(MemberErrorCode.SOCIAL_TYPE_BAD_REQUEST);
		}

		String accessToken;
		try {
			accessToken = getOAuth2Authentication(authorizationToken, state);
		} catch (FeignException e) {
			log.error("Naver token exchange failed. status={}, response={}", e.status(), e.contentUTF8());
			throw new UnauthorizedException(TokenErrorCode.AUTHENTICATION_CODE_EXPIRED);
		}

		NaverUserResponse userResponse = naverApiClient.getUserInformation("Bearer " + accessToken);
		return toMemberInfo(userResponse, loginRequest.socialType());
	}

	private String getOAuth2Authentication(String authorizationCode, String state) {
		NaverAccessTokenResponse response = naverAuthApiClient.getOAuth2AccessToken(
			AUTH_CODE,
			clientId,
			clientSecret,
			authorizationCode,
			state
		);
		return response.accessToken();
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
