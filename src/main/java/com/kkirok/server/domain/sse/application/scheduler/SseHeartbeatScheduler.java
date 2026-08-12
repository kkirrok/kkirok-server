package com.kkirok.server.domain.sse.application.scheduler;

import com.kkirok.server.domain.sse.application.service.SseEmitterRegistry;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SseHeartbeatScheduler {

    private final SseEmitterRegistry registry;

    @Scheduled(fixedRate = 20000)
    public void sendHeartbeat() {
        registry.broadcastToAll("ping", Map.of());
    }
}
