package com.kkirok.server.global.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;

@Configuration
@EnableJpaRepositories(basePackages = "com.kkirok")
@EnableRedisRepositories(
        basePackages = "com.kkirok.server.global.auth.jwt.dao"
)
public class RepositoryConfig {
}
