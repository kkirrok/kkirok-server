package com.kkirok.server.domain.kkinipop.application.service;

import com.kkirok.server.domain.kkinipop.application.dto.request.KkinipopGroupCreateRequest;
import com.kkirok.server.domain.kkinipop.application.dto.response.KkinipopGroupResponse;
import com.kkirok.server.domain.kkinipop.application.usecase.KkinipopUseCase;
import com.kkirok.server.domain.kkinipop.dao.KkinipopGroupMemberRepository;
import com.kkirok.server.domain.kkinipop.dao.KkinipopGroupRepository;
import com.kkirok.server.domain.kkinipop.domain.KkinipopGroup;
import com.kkirok.server.domain.kkinipop.domain.KkinipopGroupMember;
import com.kkirok.server.domain.kkinipop.exception.KkinipopErrorCode;
import com.kkirok.server.domain.member.application.usecase.MemberUseCase;
import com.kkirok.server.domain.member.domain.Member;
import com.kkirok.server.global.common.exception.ConflictException;
import com.kkirok.server.global.common.util.DateTimeProvider;
import com.kkirok.server.support.fixture.MemberFixture;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class KkinipopGroupServiceTest {

    @Mock
    private KkinipopUseCase kkinipopUseCase;

    @Mock
    private MemberUseCase memberUseCase;

    @Mock
    private KkinipopGroupRepository groupRepository;

    @Mock
    private KkinipopGroupMemberRepository groupMemberRepository;

    @Mock
    private DateTimeProvider dateTimeProvider;

    @InjectMocks
    private KkinipopGroupService kkinipopGroupService;

    @Test
    @DisplayName("유효한 요청으로 그룹을 생성하면 방장 멤버십과 함께 응답을 반환한다")
    void shouldCreateGroup_whenRequestIsValid() {
        // Given
        Member member = createMember(1L, "끼록이");
        KkinipopGroupCreateRequest request = new KkinipopGroupCreateRequest("아침 챌린저스");

        given(memberUseCase.findMemberByMemberId(1L)).willReturn(member);
        given(groupRepository.existsByInviteCode(any())).willReturn(false);
        given(groupRepository.save(any(KkinipopGroup.class))).willAnswer(invocation -> {
            KkinipopGroup group = invocation.getArgument(0);
            ReflectionTestUtils.setField(group, "id", 10L);
            return group;
        });
        given(groupMemberRepository.save(any(KkinipopGroupMember.class))).willAnswer(invocation -> {
            KkinipopGroupMember groupMember = invocation.getArgument(0);
            ReflectionTestUtils.setField(groupMember, "id", 100L);
            return groupMember;
        });

        // When
        KkinipopGroupResponse response = kkinipopGroupService.createGroup(1L, request);

        // Then
        assertThat(response.groupId()).isEqualTo(10L);
        assertThat(response.name()).isEqualTo("아침 챌린저스");
        assertThat(response.memberCount()).isEqualTo(1);
        assertThat(response.isLeader()).isTrue();
    }

    @Test
    @DisplayName("이미 참여 중인 그룹에는 다시 참여할 수 없다")
    void shouldThrowConflictException_whenJoiningAlreadyJoinedGroup() {
        // Given
        Member member = createMember(1L, "끼록이");
        KkinipopGroup group = createGroup(10L, "아침 챌린저스");

        given(memberUseCase.findMemberByMemberId(1L)).willReturn(member);
        given(groupRepository.findByInviteCode("AB12CD")).willReturn(Optional.of(group));
        given(groupMemberRepository.existsActiveMembership(10L, 1L)).willReturn(true);

        // When, Then
        assertThatThrownBy(() -> kkinipopGroupService.joinGroup(1L, "AB12CD"))
                .isInstanceOf(ConflictException.class)
                .extracting("baseErrorCode")
                .isEqualTo(KkinipopErrorCode.GROUP_JOIN_CONFLICT);
    }

    @Test
    @DisplayName("방장이 그룹을 탈퇴하면 그룹이 삭제된다")
    void shouldDeleteGroup_whenLeaderLeavesGroup() {
        // Given
        KkinipopGroup group = createGroup(10L, "아침 챌린저스");
        Member leader = createMember(1L, "방장");
        KkinipopGroupMember leaderGroupMember = createLeaderGroupMember(100L, group, leader);

        given(kkinipopUseCase.findGroupMember(10L, 1L)).willReturn(leaderGroupMember);
        given(groupRepository.findById(10L)).willReturn(Optional.of(group));

        // When
        kkinipopGroupService.leaveGroup(1L, 10L);

        // Then
        then(groupRepository).should().delete(group);
    }

    @Test
    @DisplayName("일반 멤버가 그룹을 탈퇴하면 탈퇴 시간이 기록된다")
    void shouldMarkLeftAt_whenMemberLeavesGroup() {
        // Given
        KkinipopGroup group = createGroup(10L, "아침 챌린저스");
        Member member = createMember(1L, "끼록이");
        KkinipopGroupMember groupMember = createMemberGroupMember(100L, group, member);
        LocalDateTime now = LocalDateTime.of(2026, 4, 24, 10, 0);

        given(kkinipopUseCase.findGroupMember(10L, 1L)).willReturn(groupMember);
        given(dateTimeProvider.now()).willReturn(now);

        // When
        kkinipopGroupService.leaveGroup(1L, 10L);

        // Then
        assertThat(groupMember.getLeftAt()).isEqualTo(now);
    }

    private Member createMember(Long memberId, String nickname) {
        Member member = MemberFixture.createLocalMember(nickname, "kkirok@test.com");
        ReflectionTestUtils.setField(member, "id", memberId);
        return member;
    }

    private KkinipopGroup createGroup(Long groupId, String groupName) {
        KkinipopGroup group = KkinipopGroup.create(new KkinipopGroupCreateRequest(groupName), "AB12CD");
        ReflectionTestUtils.setField(group, "id", groupId);
        return group;
    }

    private KkinipopGroupMember createLeaderGroupMember(Long groupMemberId, KkinipopGroup group, Member member) {
        KkinipopGroupMember groupMember = KkinipopGroupMember.createLeader(group, member);
        ReflectionTestUtils.setField(groupMember, "id", groupMemberId);
        return groupMember;
    }

    private KkinipopGroupMember createMemberGroupMember(Long groupMemberId, KkinipopGroup group, Member member) {
        KkinipopGroupMember groupMember = KkinipopGroupMember.createMember(group, member);
        ReflectionTestUtils.setField(groupMember, "id", groupMemberId);
        return groupMember;
    }
}
