package com.kkirok.server.domain.sse.application.listener;

import com.kkirok.server.domain.kkinipop.application.dto.event.KkinipopGroupJoinedEvent;
import com.kkirok.server.domain.kkinipop.application.dto.response.KkinipopMemberSummaryResponse;
import com.kkirok.server.domain.kkinipop.dao.KkinipopGroupMemberRepository;
import com.kkirok.server.domain.kkinipop.domain.KkinipopGroupMember;
import com.kkirok.server.domain.sse.application.dto.payload.SseMemberJoinedPayload;
import com.kkirok.server.domain.sse.application.service.SseEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class SseGroupJoinedListener {

    private final KkinipopGroupMemberRepository groupMemberRepository;
    private final SseEventPublisher sseEventPublisher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(KkinipopGroupJoinedEvent event) {
        KkinipopGroupMember groupMember = groupMemberRepository
                .findActiveGroupMembers(event.groupId()).stream()
                .filter(member -> member.getMember().getId().equals(event.joinedMemberId()))
                .findFirst()
                .orElse(null);
        if (groupMember == null) {
            return;
        }

        KkinipopMemberSummaryResponse memberSummary = new KkinipopMemberSummaryResponse(
                groupMember.getMember().getId(),
                groupMember.getMember().getDisplayName(),
                groupMember.getMember().getProfileImage(),
                false,
                groupMember.isLeader()
        );

        sseEventPublisher.publish(event.groupId(), "member-joined",
                new SseMemberJoinedPayload(event.groupId(), memberSummary));
    }
}
