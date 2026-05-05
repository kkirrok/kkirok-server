package com.kkirok.server.domain.notification.application.listener;

import com.kkirok.server.domain.kkinipop.application.dto.event.KkinipopGroupJoinedEvent;
import com.kkirok.server.domain.kkinipop.application.dto.request.KkinipopGroupCreateRequest;
import com.kkirok.server.domain.kkinipop.dao.KkinipopGroupMemberRepository;
import com.kkirok.server.domain.kkinipop.dao.KkinipopGroupRepository;
import com.kkirok.server.domain.kkinipop.domain.KkinipopGroup;
import com.kkirok.server.domain.kkinipop.domain.KkinipopGroupMember;
import com.kkirok.server.domain.member.application.usecase.MemberUseCase;
import com.kkirok.server.domain.member.domain.Member;
import com.kkirok.server.domain.notification.application.service.NotificationDispatcher;
import com.kkirok.server.domain.notification.domain.NotificationType;
import com.kkirok.server.support.fixture.MemberFixture;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class GroupJoinedNotificationListenerTest {

    @Mock
    private KkinipopGroupRepository groupRepository;

    @Mock
    private KkinipopGroupMemberRepository groupMemberRepository;

    @Mock
    private MemberUseCase memberUseCase;

    @Mock
    private NotificationDispatcher notificationDispatcher;

    @InjectMocks
    private GroupJoinedNotificationListener groupJoinedNotificationListener;

    @Test
    @DisplayName("그룹 가입 이벤트는 가입자를 제외한 활성 멤버에게만 발송한다")
    void shouldExcludeJoinedMember() {
        // Given
        KkinipopGroup group = createGroup(10L, "아침 챌린저스");
        Member joinedMember = createMember(1L, "가입자");
        Member first = createMember(2L, "첫번째");
        Member second = createMember(3L, "두번째");

        given(groupRepository.findById(10L)).willReturn(java.util.Optional.of(group));
        given(memberUseCase.findMemberByMemberId(1L)).willReturn(joinedMember);
        given(groupMemberRepository.findActiveGroupMembers(10L)).willReturn(List.of(
                KkinipopGroupMember.createMember(group, joinedMember),
                KkinipopGroupMember.createMember(group, first),
                KkinipopGroupMember.createMember(group, second)
        ));

        // When
        groupJoinedNotificationListener.handle(new KkinipopGroupJoinedEvent(10L, 1L));

        // Then
        then(notificationDispatcher).should().dispatchToMembers(
                List.of(first, second),
                NotificationType.GROUP_JOIN,
                "아침 챌린저스에 새 멤버가 가입했어요",
                "가입자 님이 아침 챌린저스 그룹에 참여했어요.",
                java.util.Map.of(
                        "type", "GROUP_JOIN",
                        "groupId", "10",
                        "joinedMemberId", "1"
                )
        );
    }

    private KkinipopGroup createGroup(Long groupId, String groupName) {
        KkinipopGroup group = KkinipopGroup.create(new KkinipopGroupCreateRequest(groupName), "AB12CD");
        ReflectionTestUtils.setField(group, "id", groupId);
        return group;
    }

    private Member createMember(Long memberId, String nickname) {
        Member member = MemberFixture.createLocalMember(nickname, nickname + "@test.com");
        ReflectionTestUtils.setField(member, "id", memberId);
        return member;
    }
}
