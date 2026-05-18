package com.kkirok.server.domain.notification.application.listener;

import com.kkirok.server.domain.kkinipop.application.dto.event.KkinipopReactionAddedEvent;
import com.kkirok.server.domain.kkinipop.application.dto.request.KkinipopGroupCreateRequest;
import com.kkirok.server.domain.kkinipop.dao.KkinipopGroupRepository;
import com.kkirok.server.domain.kkinipop.domain.KkinipopGroup;
import com.kkirok.server.domain.member.application.usecase.MemberUseCase;
import com.kkirok.server.domain.member.domain.Member;
import com.kkirok.server.domain.notification.application.service.NotificationDispatcher;
import com.kkirok.server.domain.notification.domain.NotificationType;
import com.kkirok.server.support.fixture.MemberFixture;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(MockitoExtension.class)
class KkinipopReactionNotificationListenerTest {

    @Mock
    private MemberUseCase memberUseCase;

    @Mock
    private KkinipopGroupRepository groupRepository;

    @Mock
    private NotificationDispatcher notificationDispatcher;

    @InjectMocks
    private KkinipopReactionNotificationListener kkinipopReactionNotificationListener;

    @Test
    @DisplayName("시스템 이모지 반응 알림은 시스템 정보만 data에 포함한다")
    void shouldDispatchSystemReactionNotification() {
        // Given
        KkinipopGroup group = createGroup(10L, "아침 챌린저스");
        Member postAuthor = createMember(1L, "작성자");
        Member reactor = createMember(2L, "반응자");
        KkinipopReactionAddedEvent event = new KkinipopReactionAddedEvent(
                30L,
                10L,
                1L,
                2L,
                "SYSTEM_HEART",
                false,
                null
        );

        given(memberUseCase.findMemberByMemberId(1L)).willReturn(postAuthor);
        given(memberUseCase.findMemberByMemberId(2L)).willReturn(reactor);
        given(groupRepository.findById(10L)).willReturn(java.util.Optional.of(group));

        // When
        kkinipopReactionNotificationListener.handle(event);

        // Then
        then(notificationDispatcher).should().dispatchToMembers(
                eq(java.util.List.of(postAuthor)),
                eq(NotificationType.KKINIPOP_REACTION),
                eq("아침 챌린저스"),
                eq("반응자님이 이모지를 달았습니다"),
                eq(Map.of(
                        "type", "KKINIPOP_REACTION",
                        "postId", "30",
                        "groupId", "10",
                        "reactorMemberId", "2",
                        "emojiCode", "SYSTEM_HEART",
                        "isCustom", "false"
                ))
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
