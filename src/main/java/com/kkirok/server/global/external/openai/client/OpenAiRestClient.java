package com.kkirok.server.global.external.openai.client;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kkirok.server.global.external.exception.ExternalErrorCode;
import com.kkirok.server.global.external.exception.OpenAiException;
import com.kkirok.server.global.external.openai.config.OpenAiProperties;
import com.kkirok.server.global.external.openai.dto.error.OpenAiErrorResponse;
import com.kkirok.server.global.external.openai.dto.request.OpenAiResponseRequest;
import com.kkirok.server.global.external.openai.dto.response.OpenAiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Map;

/*
	OpenAI Responses API를 호출하는 클라이언트
	- OpenAiResponseRequest를 직접 받아 호출
	- 재시도, 에러 로깅, 요청/응답 로깅 처리
 */
@Slf4j
@RequiredArgsConstructor
public class OpenAiRestClient implements OpenAiClient {

	private static final String RESPONSES_PATH = "/v1/responses";

	private final RestClient restClient;
	private final ObjectMapper objectMapper;
	private final OpenAiProperties properties;

	@Override
	public OpenAiResponse createResponse(OpenAiResponseRequest request) {
		OpenAiResponseRequest normalized = request.withModelDefaults(properties.getModel(), properties.getFallbackModel());
		int maxRetries = Math.max(0, properties.getMaxRetries());
		long backoffMillis = Math.max(0, properties.getInitialBackoffMillis());

		RestClientResponseException lastException = null;
		for (int attempt = 0; attempt <= maxRetries; attempt++) {
			try {
				OpenAiResponse response = restClient.post()
						.uri(RESPONSES_PATH)
						.body(normalized)
						.retrieve()
						.body(OpenAiResponse.class);

				logSuccess(normalized, response, true);
				return response;
			} catch (RestClientResponseException ex) {
				lastException = ex;
				if (!isRetryable(ex.getStatusCode()) || attempt == maxRetries) {
					logFailure(normalized, ex, false);
					throw buildException(ex);
				}

				logFailure(normalized, ex, true);
				sleep(backoffMillis);
				backoffMillis = backoffMillis == 0 ? 0 : backoffMillis * 4;
			}
		}

		throw buildException(lastException);
	}

	private boolean isRetryable(HttpStatusCode statusCode) {
		return statusCode != null && (statusCode.value() == 429
				|| statusCode.value() == 503
				|| statusCode.value() == 504);
	}

	private void sleep(long millis) {
		if (millis <= 0) {
			return;
		}
		try {
			Thread.sleep(millis);
		} catch (InterruptedException ignored) {
			Thread.currentThread().interrupt();
		}
	}

	private OpenAiException buildException(RestClientResponseException ex) {
		if (ex == null) {
			return new OpenAiException(ExternalErrorCode.OPENAI_CLIENT_ERROR, null);
		}
		OpenAiErrorResponse errorResponse = tryParseError(ex.getResponseBodyAsString());
		if (errorResponse != null && errorResponse.error() != null) {
			log.warn("OpenAI error response type={} code={} message={}",
					errorResponse.error().type(),
					errorResponse.error().code(),
					errorResponse.error().message());
		}
		return new OpenAiException(ExternalErrorCode.OPENAI_CLIENT_ERROR, ex);
	}

	private OpenAiErrorResponse tryParseError(String responseBody) {
		if (responseBody == null || responseBody.isBlank()) {
			return null;
		}
		try {
			return objectMapper.readValue(responseBody, OpenAiErrorResponse.class);
		} catch (JsonProcessingException ignored) {
			return null;
		}
	}


	private void logSuccess(OpenAiResponseRequest request, OpenAiResponse response, boolean parsed) {

		String requestId = response != null ? response.requestId() : null;
		String model = response != null ? response.model() : request.model();
		Long totalTokens = response != null && response.usage() != null ? response.usage().totalTokens() : null;

		log.info("openai.responses success requestId={} model={} promptVersion={} inputHash={} totalTokens={} parsed={}",
				requestId,
				model,
				extractPromptVersion(request.metadata()),
				hashPayload(request.instructions(), request.input()),
				totalTokens,
				parsed);
	}

	private void logFailure(OpenAiResponseRequest request, RestClientResponseException ex, boolean willRetry) {
		String code = null;
		OpenAiErrorResponse errorResponse = tryParseError(ex.getResponseBodyAsString());
		if (errorResponse != null && errorResponse.error() != null) {
			code = errorResponse.error().code();
		}

		log.warn("openai.responses failed status={} errorCode={} willRetry={} promptVersion={} inputHash={}",
				ex.getStatusCode().value(),
				code,
				willRetry,
				extractPromptVersion(request.metadata()),
				hashPayload(request.instructions(), request.input()));
	}

	private String extractPromptVersion(Map<String, Object> metadata) {
		if (metadata == null) {
			return null;
		}
		Object value = metadata.get("promptVersion");
		if (value == null) {
			value = metadata.get("prompt_version");
		}
		return value != null ? String.valueOf(value) : null;
	}

	private String hashPayload(String instructions, Object input) {
		if (instructions == null && input == null) {
			return null;
		}
		StringBuilder payload = new StringBuilder();
		if (instructions != null) {
			payload.append(instructions);
		}
		if (input != null) {
			if (!payload.isEmpty()) {
				payload.append('\n');
			}
			payload.append(input instanceof String ? (String) input : input.toString());
		}
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hashed = digest.digest(payload.toString().getBytes(StandardCharsets.UTF_8));
			return HexFormat.of().formatHex(hashed);
		} catch (NoSuchAlgorithmException e) {
			return null;
		}
	}
}
