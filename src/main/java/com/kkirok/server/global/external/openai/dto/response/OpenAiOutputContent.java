package com.kkirok.server.global.external.openai.dto.response;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;

// output item 내부의 실제 텍스트 조각을 받는 응답 DTO
@JsonInclude(JsonInclude.Include.NON_NULL)
public record OpenAiOutputContent(
		String type,
		@JsonAlias({"text", "output_text"})
		String text
) {
}
