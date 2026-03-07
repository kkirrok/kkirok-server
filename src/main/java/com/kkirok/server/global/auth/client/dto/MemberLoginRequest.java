package com.kkirok.server.global.auth.client.dto;

import com.kkirok.server.domain.member.domain.SocialType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record MemberLoginRequest(
	@Schema(description = "소셜 로그인 타입", example = "KAKAO, NAVER", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotNull(message = "소셜 로그인 종류가 입력되지 않았습니다.")
	SocialType socialType,
	@Schema(
		description = "OAuth state 값 (NAVER 로그인 시 필수, 인가 요청 시 사용한 state와 동일해야 함)",
		example = "naver_test_state_123"
	)
	String state
) {
}
