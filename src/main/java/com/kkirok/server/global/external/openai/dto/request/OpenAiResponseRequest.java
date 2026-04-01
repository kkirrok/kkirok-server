package com.kkirok.server.global.external.openai.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

// /v1/responses 호출에 사용하는 OpenAI 요청 DTO
@JsonInclude(JsonInclude.Include.NON_NULL)
public record OpenAiResponseRequest(
		String model,
		String instructions,
		Object input,
		OpenAiResponseText text,
		Map<String, Object> metadata,
		Double temperature,
		@JsonProperty("max_output_tokens")
		Integer maxOutputTokens,
		@JsonIgnore String fallbackModel
) {

	public static OpenAiResponseRequest promptWithInput(String instructions, Object input, Map<String, Object> metadata) {
		return new OpenAiResponseRequest(null, instructions, input, null, metadata, null, null, null);
	}

	// 요청에 모델이 비어 있으면 애플리케이션 기본 모델 설정을 주입한다
	public OpenAiResponseRequest withModelDefaults(String defaultModel, String defaultFallback) {
		return new OpenAiResponseRequest(
				model != null ? model : defaultModel,
				instructions,
				input,
				text,
				metadata,
				temperature,
				maxOutputTokens,
				fallbackModel != null ? fallbackModel : defaultFallback
		);
	}
}
