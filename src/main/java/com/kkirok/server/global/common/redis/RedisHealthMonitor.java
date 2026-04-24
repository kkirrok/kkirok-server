package com.kkirok.server.global.common.redis;

import com.kkirok.server.global.webhook.sender.RedisWebhookSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.connection.RedisConnectionCommands;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.boot.context.event.ApplicationReadyEvent;

import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisHealthMonitor {

    private static final long REDIS_HEALTH_CHECK_INTERVAL_MILLIS = 60_000L; // 초 단위

    private enum RedisHealthStatus {
        UNKNOWN,
        UP,
        DOWN
    }

    private final RedisTemplate<String, String> redisTemplate;
    private final RedisWebhookSender redisWebhookSender;
    private final AtomicReference<RedisHealthStatus> currentStatus = new AtomicReference<>(RedisHealthStatus.UNKNOWN);

    // 서버 구동 시 체크
    @EventListener(ApplicationReadyEvent.class)
    public void checkOnStartup() {
        checkRedisHealth("startup");
    }

    // 주기적으로 체크
    @Scheduled(fixedDelay = REDIS_HEALTH_CHECK_INTERVAL_MILLIS)
    public void checkPeriodically() {
        checkRedisHealth("scheduled");
    }

    private void checkRedisHealth(String source) {
        try {
            String pong = redisTemplate.execute((RedisCallback<String>) RedisConnectionCommands::ping);
            RedisHealthStatus previousStatus = currentStatus.getAndSet(RedisHealthStatus.UP);

            if (previousStatus == RedisHealthStatus.DOWN) {
                log.info("Redis connection recovered. source={}, response={}", source, pong);
            }
        } catch (RuntimeException exception) {
            RedisHealthStatus previousStatus = currentStatus.getAndSet(RedisHealthStatus.DOWN);
            logRedisFailure(source, exception);

            if (previousStatus != RedisHealthStatus.DOWN) {
                redisWebhookSender.send(exception);
            }
        }
    }

    private void logRedisFailure(final String source, final Exception exception) {
        Throwable rootCause = rootCauseOf(exception);
        log.error(
                "Redis health check failed. source={}, exceptionType={}, message={}, cause={}",
                source,
                exception.getClass().getSimpleName(),
                exception.getMessage(),
                rootCause == null ? null : rootCause.getMessage()
        );
    }

    private Throwable rootCauseOf(final Throwable throwable) {
        Throwable current = throwable;
        while (current.getCause() != null && current.getCause() != current) {
            current = current.getCause();
        }
        return current;
    }
}
