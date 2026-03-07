package com.kkirok.server.global.auth.client.naver.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record NaverAccessTokenResponse(
	String accessToken
) {
}

