package com.kkirok.server.global.external.publicdata.config;

import com.kkirok.server.global.external.publicdata.PublicDataFoodClient;
import com.kkirok.server.global.external.publicdata.PublicDataFoodRestClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
@EnableConfigurationProperties(PublicDataFoodProperties.class)
public class PublicDataFoodClientConfig {

    // PublicDataFoodClientConfig.java
    @Bean
    public RestClient processedFoodRestClient(PublicDataFoodProperties properties) {
        return RestClient.builder()
                .baseUrl("https://api.data.go.kr")   // 가공식품 도메인
                .build();
    }

    @Bean
    public RestClient standardFoodRestClient(PublicDataFoodProperties properties) {
        return RestClient.builder()
                .baseUrl("https://apis.data.go.kr")   // 일반식품 도메인
                .build();
    }

    @Bean
    public PublicDataFoodClient publicDataFoodClient(
            @Qualifier("processedFoodRestClient") RestClient processedRestClient,
            @Qualifier("standardFoodRestClient") RestClient standardRestClient,
            PublicDataFoodProperties properties) {
        return new PublicDataFoodRestClient(processedRestClient, standardRestClient, properties);
    }
}