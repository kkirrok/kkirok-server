package com.kkirok.server.global.external.openai.dto.response;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;

// OpenAI 응답의 토큰 사용량 정보를 담는 DTO
@JsonInclude(JsonInclude.Include.NON_NULL)
public record OpenAiUsage(

		@JsonAlias("input_tokens")
		Long inputTokens,

		@JsonAlias("output_tokens")
		Long outputTokens,

		@JsonAlias("total_tokens") Long totalTokens

) {
}
