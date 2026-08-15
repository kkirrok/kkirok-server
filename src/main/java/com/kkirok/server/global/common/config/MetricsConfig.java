package com.kkirok.server.global.common.config;

import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.config.MeterFilter;
import io.micrometer.core.instrument.logging.LoggingMeterRegistry;
import io.micrometer.core.instrument.logging.LoggingRegistryConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class MetricsConfig {

    private static final String CACHE_LOOKUP_METRIC = "cache.lookup";

    @Bean
    public LoggingMeterRegistry loggingMeterRegistry() {
        LoggingRegistryConfig config = new LoggingRegistryConfig() {
            @Override
            public String get(String key) {
                return null;
            }

            @Override
            public Duration step() {
                return Duration.ofHours(1);
            }
        };
        LoggingMeterRegistry registry = new LoggingMeterRegistry(config, Clock.SYSTEM);
        registry.config().meterFilter(
                MeterFilter.denyUnless(id -> id.getName().equals(CACHE_LOOKUP_METRIC))
        );
        return registry;
    }
}
