package com.kkirok.server.global.common.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "thread-pool")
@RequiredArgsConstructor
@Getter
public class ThreadPoolProperties {
    private final int coreSize;
    private final String threadNamePrefix;
}
