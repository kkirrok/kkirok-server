package com.kkirok.server.domain.sse.application.service;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.kkirok.server.domain.kkinipop.dao.KkinipopGroupMemberRepository;
import com.kkirok.server.domain.kkinipop.domain.KkinipopGroupMember;
import com.kkirok.server.domain.member.domain.Member;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ImmediateSseEventPublisherTest {

    @Mock private KkinipopGroupMemberRepository groupMemberRepository;
    @Mock private SseEmitterRegistry registry;
    @Mock private KkinipopGroupMember groupMember;
    @Mock private Member member;
    @InjectMocks private ImmediateSseEventPublisher publisher;

    @Test
    void publish_sendsEventToEveryActiveMember() {
        given(groupMemberRepository.findActiveGroupMembers(1L)).willReturn(List.of(groupMember));
        given(groupMember.getMember()).willReturn(member);
        given(member.getId()).willReturn(2L);

        publisher.publish(1L, "event", "payload");

        then(registry).should().sendToMember(2L, "event", "payload");
    }

    @Test
    void publish_doesNotSendWhenThereAreNoMembers() {
        given(groupMemberRepository.findActiveGroupMembers(1L)).willReturn(List.of());

        publisher.publish(1L, "event", "payload");

        then(registry).shouldHaveNoInteractions();
    }
}
