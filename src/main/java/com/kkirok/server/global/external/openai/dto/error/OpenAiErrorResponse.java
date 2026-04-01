package com.kkirok.server.global.external.openai.dto.error;

import com.fasterxml.jackson.annotation.JsonInclude;

// OpenAI API 실패 응답 body를 파싱하기 위한 DTO
@JsonInclude(JsonInclude.Include.NON_NULL)
public record OpenAiErrorResponse(OpenAiErrorDetail error) {

	// OpenAI error 객체의 상세 필드
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public record OpenAiErrorDetail(
			String message,
			String type,
			String code,
			String param
	) {
	}
}
