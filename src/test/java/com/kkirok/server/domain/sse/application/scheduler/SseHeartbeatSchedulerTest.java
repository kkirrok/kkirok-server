package com.kkirok.server.domain.sse.application.scheduler;

import static org.mockito.BDDMockito.then;

import com.kkirok.server.domain.sse.application.service.SseEmitterRegistry;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SseHeartbeatSchedulerTest {
    @Mock private SseEmitterRegistry registry;
    @InjectMocks private SseHeartbeatScheduler scheduler;

    @Test
    void sendHeartbeat_broadcastsPing() {
        scheduler.sendHeartbeat();

        then(registry).should().broadcastToAll("ping", Map.of());
    }
}
