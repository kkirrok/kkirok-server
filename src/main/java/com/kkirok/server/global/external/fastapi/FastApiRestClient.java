package com.kkirok.server.global.external.fastapi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kkirok.server.global.external.exception.ExternalErrorCode;
import com.kkirok.server.global.external.exception.FastApiException;
import com.kkirok.server.global.external.fastapi.config.FastApiProperties;
import com.kkirok.server.global.external.fastapi.dto.error.FastApiErrorResponse;
import com.kkirok.server.global.external.fastapi.dto.request.FastApiRequest;
import com.kkirok.server.global.external.fastapi.dto.response.FastApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class FastApiRestClient implements FastApiClient {

	private final RestClient restClient;
	private final ObjectMapper objectMapper;
	private final FastApiProperties properties;

	@Override
	public FastApiResponse call(FastApiRequest request) {
		validateRequest(request);

		int maxRetries = Math.max(0, properties.getMaxRetries());
		long backoffMillis = Math.max(0, properties.getInitialBackoffMillis());

		RestClientResponseException lastException = null;
		for (int attempt = 0; attempt <= maxRetries; attempt++) {
			try {
				ResponseEntity<JsonNode> response = restClient.post()
						.uri(request.path())
						.contentType(MediaType.APPLICATION_JSON)
						.headers(httpHeaders -> applyHeaders(httpHeaders, request.headers()))
						.body(request.body())
						.retrieve()
						.toEntity(JsonNode.class);

				return new FastApiResponse(response.getStatusCode().value(), response.getBody());
			} catch (RestClientResponseException ex) {
				lastException = ex;
				if (!isRetryable(ex.getStatusCode()) || attempt == maxRetries) {
					throw buildException(ex);
				}
				sleep(backoffMillis);
				backoffMillis = backoffMillis == 0 ? 0 : backoffMillis * 4;
			}
		}

		throw buildException(lastException);
	}

	private void validateRequest(FastApiRequest request) {
		if (request == null || request.endpoint() == null || request.path() == null || request.path().isBlank()) {
			throw new FastApiException(ExternalErrorCode.FAST_API_INVALID_REQUEST);
		}
	}

	private void applyHeaders(HttpHeaders headers, Map<String, String> requestHeaders) {
		if (requestHeaders == null || requestHeaders.isEmpty()) {
			return;
		}
		requestHeaders.forEach(headers::set);
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

	private FastApiException buildException(RestClientResponseException ex) {
		if (ex == null) {
			return new FastApiException(ExternalErrorCode.FAST_API_CLIENT_ERROR);
		}

		FastApiErrorResponse errorResponse = tryParseError(ex.getResponseBodyAsString());
		if (errorResponse != null) {
			log.warn("FastAPI error status={} detail={}", ex.getStatusCode().value(), errorResponse.detail());
		}

		return new FastApiException(ExternalErrorCode.FAST_API_CLIENT_ERROR, ex);
	}

	private FastApiErrorResponse tryParseError(String responseBody) {
		if (responseBody == null || responseBody.isBlank()) {
			return null;
		}
		try {
			return objectMapper.readValue(responseBody, FastApiErrorResponse.class);
		} catch (JsonProcessingException ignored) {
			return null;
		}
	}
}
