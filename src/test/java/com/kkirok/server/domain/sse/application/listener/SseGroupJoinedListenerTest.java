package com.kkirok.server.domain.sse.application.listener;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.kkirok.server.domain.kkinipop.application.dto.event.KkinipopGroupJoinedEvent;
import com.kkirok.server.domain.kkinipop.application.dto.request.KkinipopGroupCreateRequest;
import com.kkirok.server.domain.kkinipop.application.dto.response.KkinipopMemberSummaryResponse;
import com.kkirok.server.domain.kkinipop.dao.KkinipopGroupMemberRepository;
import com.kkirok.server.domain.kkinipop.domain.KkinipopGroup;
import com.kkirok.server.domain.kkinipop.domain.KkinipopGroupMember;
import com.kkirok.server.domain.member.domain.Member;
import com.kkirok.server.domain.sse.application.dto.payload.SseMemberJoinedPayload;
import com.kkirok.server.domain.sse.application.service.SseEventPublisher;
import com.kkirok.server.support.fixture.MemberFixture;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class SseGroupJoinedListenerTest {

    @Mock private KkinipopGroupMemberRepository groupMemberRepository;
    @Mock private SseEventPublisher sseEventPublisher;
    @InjectMocks private SseGroupJoinedListener listener;

    @Test
    void handle_publishesJoinedMemberPayload() {
        KkinipopGroup group = KkinipopGroup.create(new KkinipopGroupCreateRequest("그룹"), "ABC123");
        Member member = MemberFixture.createLocalMember("가입자", "joined@test.com");
        ReflectionTestUtils.setField(member, "id", 2L);
        KkinipopGroupMember groupMember = KkinipopGroupMember.createMember(group, member);
        given(groupMemberRepository.findActiveGroupMembers(1L)).willReturn(List.of(groupMember));

        listener.handle(new KkinipopGroupJoinedEvent(1L, 2L));

        then(sseEventPublisher).should().publish(1L, "member-joined", new SseMemberJoinedPayload(
                1L, new KkinipopMemberSummaryResponse(2L, "가입자", null, false, false)));
    }
}
