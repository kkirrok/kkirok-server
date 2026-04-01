package com.kkirok.server.global.external.fastapi.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kkirok.server.global.external.fastapi.FastApiClient;
import com.kkirok.server.global.external.fastapi.FastApiRestClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
@EnableConfigurationProperties(FastApiProperties.class)
public class FastApiClientConfig {

	@Bean
	public RestClient fastApiRestClient(FastApiProperties properties) {
		if (properties.getBaseUrl() == null || properties.getBaseUrl().isBlank()) {
			throw new IllegalStateException("FastAPI base URL이 없습니다. FAST_API_BASE_URL 또는 fastapi.base-url을 설정하세요.");
		}

		SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
		requestFactory.setConnectTimeout(Duration.ofSeconds(properties.getConnectTimeoutSeconds()));
		requestFactory.setReadTimeout(Duration.ofSeconds(properties.getReadTimeoutSeconds()));

		RestClient.Builder builder = RestClient.builder()
				.baseUrl(properties.getBaseUrl())
				.requestFactory(requestFactory)
				.defaultHeader("Content-Type", "application/json");

		if (properties.getApiKey() != null && !properties.getApiKey().isBlank()) {
			builder.defaultHeader("Authorization", "Bearer " + properties.getApiKey());
		}

		return builder.build();
	}

	@Bean
	public FastApiClient fastApiClient(@Qualifier("fastApiRestClient") RestClient fastApiRestClient,
									   ObjectMapper objectMapper,
									   FastApiProperties properties) {
		return new FastApiRestClient(fastApiRestClient, objectMapper, properties);
	}
}
