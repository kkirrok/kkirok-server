package com.kkirok.server.global.external.openai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kkirok.server.global.external.exception.ExternalErrorCode;
import com.kkirok.server.global.external.exception.OpenAiException;
import com.kkirok.server.global.external.openai.client.OpenAiClient;
import com.kkirok.server.global.external.openai.dto.request.OpenAiResponseRequest;
import com.kkirok.server.global.external.openai.dto.response.OpenAiResponse;
import com.kkirok.server.global.external.openai.prompt.PromptService;
import com.kkirok.server.global.external.openai.prompt.PromptTemplate;
import com.kkirok.server.global.external.openai.prompt.PromptType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/*
	외부 로직에서 OpenAi API 호출 위해 이용하는 클래스이다.
	프롬프트 타입과 P, R를 이용해서 내부적으로 OpenAiResponseRequest 객체를 생성하여,
	OpenAiClient의 createResponse()로 OpenAi API를 호출한다.

	주요 메서드 createObjectResponse()의 역할
	- prompt : 프롬프트 타입으로부터 프롬프트 추출
	- response 생성 : prompt, P로 요청 객체 생성후, client호출
	- response로부터 R타입의 응답값 생성 후 반환

	파라미터
	- promptType  : 어떤 기능인지 의미
	- P(parameter): 호출할 때 이용할 객체
	- R(response) : 호출한 주체가 요구하는 응답값

 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OpenAiService {

	private final OpenAiClient openAiClient;
	private final PromptService promptService;
	private final ObjectMapper objectMapper;

	public <P, R> R createObjectResponse(PromptType promptType, P input, Class<R> responseType) {
		PromptTemplate prompt = promptService.getPrompt(promptType);
		OpenAiResponse response = openAiClient.createResponse(
				OpenAiResponseRequest.promptWithInput(
						prompt.content(),
						serializeInput(input),
						promptMetadata(promptType, prompt)
				)
		);
		return parseResponse(response, responseType);
	}

	private String serializeInput(Object input) {
		if (input == null) {
			return null;
		}
		if (input instanceof String text) {
			return text;
		}

		try {
			return objectMapper.writeValueAsString(input);
		} catch (JsonProcessingException exception) {
			log.warn("Failed to serialize OpenAI input. inputType={}",
					input.getClass().getSimpleName(),
					exception);
			throw new OpenAiException(ExternalErrorCode.OPENAI_CLIENT_ERROR, exception);
		}
	}

	private <T> T parseResponse(OpenAiResponse response, Class<T> responseType) {
		String responseText = response != null ? response.getFirstOutputText() : null;
		System.out.println(responseText);
		if (responseText == null || responseText.isBlank()) {
			throw new OpenAiException(ExternalErrorCode.OPENAI_RESPONSE_PARSE_FAILED, null);
		}

		try {
			return objectMapper.readValue(responseText, responseType);
		} catch (JsonProcessingException exception) {
			log.warn("Failed to parse OpenAI response. responseType={} responseText={}",
					responseType.getSimpleName(),
					responseText,
					exception);
			throw new OpenAiException(ExternalErrorCode.OPENAI_RESPONSE_PARSE_FAILED, exception);
		}
	}

	private Map<String, Object> promptMetadata(PromptType promptType, PromptTemplate prompt) {
		Map<String, Object> metadata = new HashMap<>();
		metadata.put("promptType", promptType.name());
		metadata.put("promptVersion", prompt.version());
		return metadata;
	}

	public <R> R createVisionResponse(PromptType promptType, String imageUrl, String userText, Class<R> responseType) {
		PromptTemplate prompt = promptService.getPrompt(promptType);
		OpenAiResponse response = openAiClient.createResponse(
				OpenAiResponseRequest.promptWithImageUrl(
						prompt.content(), imageUrl, userText, promptMetadata(promptType, prompt)
				)
		);
		return parseResponse(response, responseType);
	}
}
