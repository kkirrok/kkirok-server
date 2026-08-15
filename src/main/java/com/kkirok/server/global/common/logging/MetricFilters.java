package com.kkirok.server.global.common.logging;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

@Getter
@AllArgsConstructor
public enum MetricFilters {

    CACHE_LOOKUP("cache.lookup", true),
    SPRING_DATA_REPOSITORY_INVOCATIONS("spring.data.repository.invocations", true),
    HTTP_SERVER_REQUESTS("http.server.requests", true)
    ;

    public static List<String> getActiveFilters() {
        return Arrays.stream(MetricFilters.values())
                .filter(mf -> mf.isActive)
                .map(MetricFilters::getFilterName)
                .toList();
    }

    private final String filterName;
    private final boolean isActive;

}
