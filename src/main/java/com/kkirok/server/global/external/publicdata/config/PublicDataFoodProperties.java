package com.kkirok.server.global.external.publicdata.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "publicdata")
public class PublicDataFoodProperties {
    private String serviceKey;
    private String baseUrl = "https://apis.data.go.kr";
    private int connectTimeoutSeconds = 5;
    private int readTimeoutSeconds = 10;
}