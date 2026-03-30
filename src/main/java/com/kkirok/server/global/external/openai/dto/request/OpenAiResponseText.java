package com.kkirok.server.global.external.openai.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;

// text 응답 옵션을 감싸는 요청 DTO
@JsonInclude(JsonInclude.Include.NON_NULL)
public record OpenAiResponseText(
		OpenAiResponseFormat format
) {

	// 응답 포맷 포함 text 옵션 생성 헬퍼
	public static OpenAiResponseText withFormat(OpenAiResponseFormat format) {
		return new OpenAiResponseText(format);
	}
}
