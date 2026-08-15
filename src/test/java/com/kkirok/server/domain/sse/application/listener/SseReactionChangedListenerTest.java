package com.kkirok.server.domain.sse.application.listener;

import static org.mockito.BDDMockito.then;

import com.kkirok.server.domain.kkinipop.application.dto.event.KkinipopReactionChangedEvent;
import com.kkirok.server.domain.sse.application.dto.payload.SseReactionUpdatedPayload;
import com.kkirok.server.domain.sse.application.service.SseEventPublisher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SseReactionChangedListenerTest {
    @Mock private SseEventPublisher sseEventPublisher;
    @InjectMocks private SseReactionChangedListener listener;

    @Test
    void handle_publishesReactionPayload() {
        listener.handle(new KkinipopReactionChangedEvent(3L, 1L, "SYSTEM_HEART", 2L, true, 5L));

        then(sseEventPublisher).should().publish(1L, "reaction-updated",
                new SseReactionUpdatedPayload(1L, 3L, "SYSTEM_HEART", 2L, true, 5L));
    }
}
