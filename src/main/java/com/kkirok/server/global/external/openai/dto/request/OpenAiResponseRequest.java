package com.kkirok.server.global.external.openai.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

// /v1/responses 호출에 사용하는 OpenAI 요청 DTO
@JsonInclude(JsonInclude.Include.NON_NULL)
public record OpenAiResponseRequest(
		String model,
		Object input,
		OpenAiResponseText text,
		Map<String, Object> metadata,
		Double temperature,
		@JsonProperty("max_output_tokens")
		Integer maxOutputTokens,
		@JsonIgnore String fallbackModel
) {

	// 가장 단순한 텍스트 입력 요청 생성용 생성자
	public OpenAiResponseRequest(String model, Object input) {
		this(model, input, null, null, null, null, null);
	}

	// 요청에 모델이 비어 있으면 애플리케이션 기본 모델 설정을 주입한다
	public OpenAiResponseRequest withModelDefaults(String defaultModel, String defaultFallback) {
		return new OpenAiResponseRequest(
				model != null ? model : defaultModel,
				input,
				text,
				metadata,
				temperature,
				maxOutputTokens,
				fallbackModel != null ? fallbackModel : defaultFallback
		);
	}
}
