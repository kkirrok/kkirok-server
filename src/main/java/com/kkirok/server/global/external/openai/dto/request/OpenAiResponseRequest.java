package com.kkirok.server.global.external.openai.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
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

	public static OpenAiResponseRequest promptWithInput(
			String instructions,
			Object input,
			Map<String, Object> metadata
	) {
		return new OpenAiResponseRequest(
				null,
				instructions,
				input,
				null,
				metadata,
				null,
				null,
				null
		);
	}

	/**
	 * Vision 요청용 팩토리 메서드.
	 *
	 * OpenAI /v1/responses API의 올바른 이미지 입력 구조:
	 *
	 * [
	 *   {
	 *     "role": "user",
	 *     "content": [
	 *       { "type": "input_text", "text": "..." },
	 *       { "type": "input_image", "image_url": "https://..." }
	 *     ]
	 *   }
	 * ]
	 *
	 * input_image를 input 배열에 바로 넣으면 안 된다.
	 */
	public static OpenAiResponseRequest promptWithImageUrl(
			String instructions,
			String imageUrl,
			String userText,
			Map<String, Object> metadata
	) {
		List<Map<String, Object>> visionInput = List.of(
				Map.of(
						"role", "user",
						"content", List.of(
								Map.of(
										"type", "input_text",
										"text", userText
								),
								Map.of(
										"type", "input_image",
										"image_url", imageUrl
								)
						)
				)
		);

		return new OpenAiResponseRequest(
				null,
				instructions,
				visionInput,
				null,
				metadata,
				null,
				null,
				null
		);
	}

	// 요청에 모델이 비어 있으면 애플리케이션 기본 모델 설정을 주입한다
	public OpenAiResponseRequest withModelDefaults(
			String defaultModel,
			String defaultFallback
	) {
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