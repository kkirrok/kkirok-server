package com.kkirok.server.domain.sse.application.listener;

import com.kkirok.server.domain.kkinipop.application.dto.event.KkinipopGroupMemberLeftEvent;
import com.kkirok.server.domain.sse.application.dto.payload.SseMemberLeftPayload;
import com.kkirok.server.domain.sse.application.service.SseEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class SseGroupMemberLeftListener {

    private final SseEventPublisher sseEventPublisher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(KkinipopGroupMemberLeftEvent event) {
        sseEventPublisher.publish(event.groupId(), "member-left",
                new SseMemberLeftPayload(event.groupId(), event.memberId()));
    }
}
