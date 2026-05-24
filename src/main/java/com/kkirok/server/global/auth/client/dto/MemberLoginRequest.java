package com.kkirok.server.global.auth.client.dto;

import com.kkirok.server.domain.member.domain.SocialType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record MemberLoginRequest(
	@Schema(description = "소셜 로그인 타입", example = "KAKAO, NAVER", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotNull(message = "소셜 로그인 종류가 입력되지 않았습니다.")
	SocialType socialType,
	@Schema(
		description = "모바일 SDK에서 발급받은 소셜 액세스 토큰",
		example = "sdk_access_token"
	)
	@NotBlank(message = "액세스 토큰이 입력되지 않았습니다.")
	String accessToken
) {
}
