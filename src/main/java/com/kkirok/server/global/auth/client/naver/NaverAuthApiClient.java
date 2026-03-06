package com.kkirok.server.global.auth.client.naver;

import com.kkirok.server.global.auth.client.naver.response.NaverAccessTokenResponse;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "naverAuthApiClient", url = "https://nid.naver.com/oauth2.0")
public interface NaverAuthApiClient {

	@Operation(summary = "네이버 토큰 발급 API", description = "토큰 발급에 필요한 authorization code를 AccessToken으로 교환합니다.")
	@GetMapping("/token")
	NaverAccessTokenResponse getOAuth2AccessToken(
		@RequestParam("grant_type") String grantType,
		@RequestParam("client_id") String clientId,
		@RequestParam("client_secret") String clientSecret,
		@RequestParam("code") String code,
		@RequestParam("state") String state
	);
}

