package com.kkirok.server.global.common.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;

@Configuration
@EnableJpaRepositories(
        basePackages = "com.kkirok",
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.REGEX,
                pattern = "com\\.kkirok\\.server\\.global\\.auth\\.jwt\\.dao\\..*"
        )
)
@EnableRedisRepositories(
        basePackages = "com.kkirok.server.global.auth.jwt.dao"
)
public class RepositoryConfig {
}
