package com.kkirok.server.global.external.openai.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

// 응답 텍스트를 어떤 형식으로 생성할지 지정하는 요청 DTO
@JsonInclude(JsonInclude.Include.NON_NULL)
public record OpenAiResponseFormat(
		String type,
		String name,
		Map<String, Object> schema,
		Boolean strict
) {

	// structured output을 위한 json_schema 형식 생성 헬퍼
	public static OpenAiResponseFormat jsonSchema(String name, Map<String, Object> schema, Boolean strict) {
		return new OpenAiResponseFormat("json_schema", name, schema, strict);
	}
}
