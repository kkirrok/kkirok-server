package com.kkirok.server.global.common.config;

import com.kkirok.server.global.common.logging.MetricFilters;
import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.config.MeterFilter;
import io.micrometer.core.instrument.logging.LoggingMeterRegistry;
import io.micrometer.core.instrument.logging.LoggingRegistryConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.List;

@Configuration
public class MetricsConfig {

    @Bean
    public LoggingMeterRegistry loggingMeterRegistry() {
        LoggingRegistryConfig config = new LoggingRegistryConfig() {
            @Override
            public String get(String key) {
                return null;
            }

            @Override
            public Duration step() {
//                return Duration.ofHours(1);
                return Duration.ofMinutes(5);
            }
        };
        LoggingMeterRegistry registry = new LoggingMeterRegistry(config, Clock.SYSTEM);
        registry.config().meterFilter(
                MeterFilter.denyUnless(id -> ALLOWED_METRICS.contains(id.getName()))
        );
        return registry;
    }

    private List<String> ALLOWED_METRICS = MetricFilters.getActiveFilters();

}
