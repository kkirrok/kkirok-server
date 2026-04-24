package com.kkirok.server.domain.kkinipop.application.service;

import com.kkirok.server.domain.kkinipop.application.dto.response.KkinipopMissionDateResponse;
import com.kkirok.server.domain.kkinipop.application.usecase.KkinipopUseCase;
import com.kkirok.server.domain.kkinipop.dao.KkinipopMissionRepository;
import com.kkirok.server.domain.kkinipop.dao.KkinipopPostRepository;
import com.kkirok.server.domain.kkinipop.domain.KkinipopGroup;
import com.kkirok.server.domain.kkinipop.domain.KkinipopGroupMember;
import com.kkirok.server.domain.kkinipop.domain.KkinipopMission;
import com.kkirok.server.domain.kkinipop.domain.KkinipopPost;
import com.kkirok.server.domain.kkinipop.exception.KkinipopErrorCode;
import com.kkirok.server.domain.member.domain.Member;
import com.kkirok.server.global.common.exception.BadRequestException;
import com.kkirok.server.global.common.util.DateTimeProvider;
import com.kkirok.server.support.fixture.MemberFixture;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class KkinipopMissionServiceTest {

    @Mock
    private KkinipopUseCase kkinipopUseCase;

    @Mock
    private KkinipopMissionRepository missionRepository;

    @Mock
    private KkinipopPostRepository postRepository;

    @Mock
    private DateTimeProvider dateTimeProvider;

    @InjectMocks
    private KkinipopMissionService kkinipopMissionService;

    @Test
    @DisplayName("오늘 미션을 조회하면 현재 진행 중인 미션을 포함한 목록을 반환한다")
    void shouldReturnMissionDateResponse_whenDateIsValid() {
        // Given
        KkinipopGroup group = createGroup(10L, "아침 챌린저스");
        Member member = createMember(1L, "끼록이");
        KkinipopGroupMember groupMember = createGroupMember(100L, group, member);
        LocalDateTime now = LocalDateTime.of(2026, 4, 24, 9, 5);
        LocalDate today = LocalDate.of(2026, 4, 24);

        KkinipopMission liveMission = createMission(20L, group, "현재 미션",
                LocalDateTime.of(2026, 4, 24, 9, 0),
                LocalDateTime.of(2026, 4, 24, 9, 10));
        KkinipopMission upcomingMission = createMission(21L, group, "다음 미션",
                LocalDateTime.of(2026, 4, 24, 18, 0),
                LocalDateTime.of(2026, 4, 24, 18, 10));
        KkinipopPost successPost = createPost(30L, group, member, liveMission, today, "uuid_post");

        given(kkinipopUseCase.findGroupMember(10L, 1L)).willReturn(groupMember);
        given(dateTimeProvider.today()).willReturn(today);
        given(dateTimeProvider.now()).willReturn(now);
        given(missionRepository.findMissionsByDate(10L, today.atStartOfDay(), today.plusDays(1).atStartOfDay()))
                .willReturn(List.of(liveMission, upcomingMission));
        given(postRepository.findDailyPosts(10L, today)).willReturn(List.of(successPost));

        // When
        KkinipopMissionDateResponse response = kkinipopMissionService.getTodayMissions(1L, 10L);

        // Then
        assertThat(response.date()).isEqualTo(today);
        assertThat(response.dayOfWeek()).isEqualTo(5);
        assertThat(response.missions()).hasSize(2);
        assertThat(response.missions().get(0).missionId()).isEqualTo(20L);
        assertThat(response.missions().get(0).isRealTime()).isTrue();
        assertThat(response.missions().get(0).successMemberCount()).isEqualTo(1);
        assertThat(response.missions().get(1).missionId()).isEqualTo(21L);
    }

    @Test
    @DisplayName("현재 시간에 해당하는 미션이 없으면 조회할 수 없다")
    void shouldThrowBadRequestException_whenNoLiveMissionExists() {
        // Given
        KkinipopGroup group = createGroup(10L, "아침 챌린저스");
        Member member = createMember(1L, "끼록이");
        KkinipopGroupMember groupMember = createGroupMember(100L, group, member);
        LocalDateTime now = LocalDateTime.of(2026, 4, 24, 9, 5);
        LocalDate today = LocalDate.of(2026, 4, 24);

        KkinipopMission upcomingMission = createMission(21L, group, "다음 미션",
                LocalDateTime.of(2026, 4, 24, 18, 0),
                LocalDateTime.of(2026, 4, 24, 18, 10));

        given(kkinipopUseCase.findGroupMember(10L, 1L)).willReturn(groupMember);
        given(dateTimeProvider.today()).willReturn(today);
        given(dateTimeProvider.now()).willReturn(now);
        given(missionRepository.findMissionsByDate(10L, today.atStartOfDay(), today.plusDays(1).atStartOfDay()))
                .willReturn(List.of(upcomingMission));

        // When, Then
        assertThatThrownBy(() -> kkinipopMissionService.getTodayMissions(1L, 10L))
                .isInstanceOf(BadRequestException.class)
                .extracting("baseErrorCode")
                .isEqualTo(KkinipopErrorCode.INVALID_MISSION_REQUEST);
    }

    @Test
    @DisplayName("현재 시간에 해당하는 미션이 둘 이상이면 조회할 수 없다")
    void shouldThrowBadRequestException_whenMultipleLiveMissionsExist() {
        // Given
        KkinipopGroup group = createGroup(10L, "아침 챌린저스");
        Member member = createMember(1L, "끼록이");
        KkinipopGroupMember groupMember = createGroupMember(100L, group, member);
        LocalDateTime now = LocalDateTime.of(2026, 4, 24, 9, 5);
        LocalDate today = LocalDate.of(2026, 4, 24);

        KkinipopMission firstMission = createMission(20L, group, "미션 1",
                LocalDateTime.of(2026, 4, 24, 9, 0),
                LocalDateTime.of(2026, 4, 24, 9, 10));
        KkinipopMission secondMission = createMission(21L, group, "미션 2",
                LocalDateTime.of(2026, 4, 24, 9, 1),
                LocalDateTime.of(2026, 4, 24, 9, 11));

        given(kkinipopUseCase.findGroupMember(10L, 1L)).willReturn(groupMember);
        given(dateTimeProvider.today()).willReturn(today);
        given(dateTimeProvider.now()).willReturn(now);
        given(missionRepository.findMissionsByDate(10L, today.atStartOfDay(), today.plusDays(1).atStartOfDay()))
                .willReturn(List.of(firstMission, secondMission));

        // When, Then
        assertThatThrownBy(() -> kkinipopMissionService.getTodayMissions(1L, 10L))
                .isInstanceOf(BadRequestException.class)
                .extracting("baseErrorCode")
                .isEqualTo(KkinipopErrorCode.INVALID_MISSION_REQUEST);
    }

    private Member createMember(Long memberId, String nickname) {
        Member member = MemberFixture.createLocalMember(nickname, memberId + "@test.com");
        ReflectionTestUtils.setField(member, "id", memberId);
        return member;
    }

    private KkinipopGroup createGroup(Long groupId, String name) {
        KkinipopGroup group = KkinipopGroup.create(new com.kkirok.server.domain.kkinipop.application.dto.request.KkinipopGroupCreateRequest(name), "AB12CD");
        ReflectionTestUtils.setField(group, "id", groupId);
        return group;
    }

    private KkinipopGroupMember createGroupMember(Long groupMemberId, KkinipopGroup group, Member member) {
        KkinipopGroupMember groupMember = KkinipopGroupMember.createMember(group, member);
        ReflectionTestUtils.setField(groupMember, "id", groupMemberId);
        return groupMember;
    }

    private KkinipopMission createMission(Long missionId, KkinipopGroup group, String title, LocalDateTime startAt, LocalDateTime endAt) {
        KkinipopMission mission = KkinipopMission.create(group, title, startAt, endAt);
        ReflectionTestUtils.setField(mission, "id", missionId);
        return mission;
    }

    private KkinipopPost createPost(
            Long postId,
            KkinipopGroup group,
            Member member,
            KkinipopMission mission,
            LocalDate recordDate,
            String imageKey
    ) {
        KkinipopPost post = KkinipopPost.builder()
                .group(group)
                .member(member)
                .mission(mission)
                .imageKey(imageKey)
                .recordDate(recordDate)
                .build();
        ReflectionTestUtils.setField(post, "id", postId);
        return post;
    }
}
