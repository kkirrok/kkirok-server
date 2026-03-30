package com.kkirok.server.global.external.openai.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

// OpenAI structured output 요청 시 전달하는 JSON Schema 정의 DTO
@JsonInclude(JsonInclude.Include.NON_NULL)
public record OpenAiJsonSchema(
		String name,
		Map<String, Object> schema,
		Boolean strict
) {
}
