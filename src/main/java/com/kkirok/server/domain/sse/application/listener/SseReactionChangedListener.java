package com.kkirok.server.domain.sse.application.listener;

import com.kkirok.server.domain.kkinipop.application.dto.event.KkinipopReactionChangedEvent;
import com.kkirok.server.domain.sse.application.dto.payload.SseReactionUpdatedPayload;
import com.kkirok.server.domain.sse.application.service.SseEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class SseReactionChangedListener {

    private final SseEventPublisher sseEventPublisher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(KkinipopReactionChangedEvent event) {
        sseEventPublisher.publish(event.groupId(), "reaction-updated",
                new SseReactionUpdatedPayload(event.groupId(), event.postId(), event.emojiCode(), event.count(), event.reacted()));
    }
}
