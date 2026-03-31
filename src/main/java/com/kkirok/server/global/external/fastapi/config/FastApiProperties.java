package com.kkirok.server.global.external.fastapi.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "fastapi")
public class FastApiProperties {

	private String baseUrl;
	private String apiKey;
	private int connectTimeoutSeconds = 3;
	private int readTimeoutSeconds = 60;
	private int maxRetries = 1;
	private long initialBackoffMillis = 200;
}
