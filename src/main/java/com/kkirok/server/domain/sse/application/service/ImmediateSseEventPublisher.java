package com.kkirok.server.domain.sse.application.service;

import com.kkirok.server.domain.kkinipop.dao.KkinipopGroupMemberRepository;
import com.kkirok.server.domain.kkinipop.domain.KkinipopGroupMember;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ImmediateSseEventPublisher implements SseEventPublisher {

    private final KkinipopGroupMemberRepository groupMemberRepository;
    private final SseEmitterRegistry registry;

    @Override
    public void publish(Long groupId, String eventName, Object payload) {
        List<KkinipopGroupMember> members = groupMemberRepository.findActiveGroupMembers(groupId);
        for (KkinipopGroupMember member : members) {
            registry.sendToMember(member.getMember().getId(), eventName, payload);
        }
    }
}
