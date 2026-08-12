package com.kkirok.server.domain.sse.application.listener;

import static org.mockito.BDDMockito.then;

import com.kkirok.server.domain.kkinipop.application.dto.event.KkinipopGroupMemberLeftEvent;
import com.kkirok.server.domain.sse.application.dto.payload.SseMemberLeftPayload;
import com.kkirok.server.domain.sse.application.service.SseEventPublisher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SseGroupMemberLeftListenerTest {
    @Mock private SseEventPublisher sseEventPublisher;
    @InjectMocks private SseGroupMemberLeftListener listener;

    @Test
    void handle_publishesLeftMemberPayload() {
        listener.handle(new KkinipopGroupMemberLeftEvent(1L, 2L));

        then(sseEventPublisher).should().publish(1L, "member-left", new SseMemberLeftPayload(1L, 2L));
    }
}
