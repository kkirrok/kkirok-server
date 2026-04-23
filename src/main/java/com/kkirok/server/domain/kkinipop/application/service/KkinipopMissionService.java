package com.kkirok.server.domain.kkinipop.application.service;

import com.kkirok.server.domain.kkinipop.application.dto.response.KkinipopMissionDateResponse;
import com.kkirok.server.domain.kkinipop.application.dto.response.KkinipopMissionSuccessMemberResponse;
import com.kkirok.server.domain.kkinipop.application.usecase.KkinipopUseCase;
import com.kkirok.server.domain.kkinipop.dao.KkinipopGroupMemberRepository;
import com.kkirok.server.domain.kkinipop.dao.KkinipopMissionRepository;
import com.kkirok.server.domain.kkinipop.dao.KkinipopPostRepository;
import com.kkirok.server.domain.kkinipop.domain.KkinipopGroupMember;
import com.kkirok.server.domain.kkinipop.domain.KkinipopMission;
import com.kkirok.server.domain.kkinipop.domain.KkinipopPost;
import com.kkirok.server.domain.kkinipop.exception.KkinipopErrorCode;
import com.kkirok.server.global.common.exception.BadRequestException;
import com.kkirok.server.global.common.exception.ForbiddenException;
import com.kkirok.server.global.common.exception.NotFoundException;
import com.kkirok.server.global.common.util.DateTimeProvider;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = false)
@RequiredArgsConstructor
public class KkinipopMissionService {

    private final KkinipopUseCase kkinipopUseCase;
    private final KkinipopGroupMemberRepository groupMemberRepository;
    private final KkinipopMissionRepository missionRepository;
    private final KkinipopPostRepository postRepository;
    private final DateTimeProvider dateTimeProvider;

    // 오늘 날짜 미션 조회
    @Transactional(readOnly = true)
    public KkinipopMissionDateResponse getTodayMissions(Long memberId, Long groupId) {
        kkinipopUseCase.findGroupMember(groupId, memberId);

        LocalDate targetDate = dateTimeProvider.today();
        LocalDateTime startOfDay = targetDate.atStartOfDay();
        LocalDateTime endOfDay = targetDate.plusDays(1).atStartOfDay();
        LocalDateTime currentTime = dateTimeProvider.now();

        // mission 조회
        List<KkinipopMission> missions = missionRepository.findMissionsByDate(groupId, startOfDay, endOfDay);
        validateLiveRealtimeMission(missions, currentTime);
        List<KkinipopPost> posts = postRepository.findDailyPosts(groupId, targetDate);
        Map<Long, List<KkinipopMissionSuccessMemberResponse>> successMembersByMission = getSuccessMembersByMission(posts);

        return KkinipopMissionDateResponse.from(targetDate, missions, currentTime, successMembersByMission);
    }

    private void validateLiveRealtimeMission(List<KkinipopMission> missions, LocalDateTime currentTime) {
        long liveRealtimeMissionCount = missions.stream()
                .filter(KkinipopMission::isRealtime)
                .filter(mission -> mission.isLive(currentTime))
                .count();

        if (liveRealtimeMissionCount > 1) {
            throw new BadRequestException(KkinipopErrorCode.INVALID_MISSION_REQUEST);
        }
    }

    private KkinipopGroupMember findLeaderMembership(Long memberId) {
        return groupMemberRepository.findLeaderMemberships(memberId).stream()
                .findFirst()
                .orElseThrow(() -> new NotFoundException(KkinipopErrorCode.GROUP_MEMBER_NOT_FOUND));
    }

    private void validateLeader(KkinipopGroupMember groupMember) {
        if (!groupMember.isLeader()) {
            throw new ForbiddenException(KkinipopErrorCode.GROUP_MANAGEMENT_FORBIDDEN);
        }
    }

    private Map<Long, List<KkinipopMissionSuccessMemberResponse>> getSuccessMembersByMission(List<KkinipopPost> posts) {
        Map<Long, Map<Long, KkinipopMissionSuccessMemberResponse>> mappedMembers = new LinkedHashMap<>();

        for (KkinipopPost post : posts) {
            if (post.getMission() == null) {
                continue;
            }

            mappedMembers
                    .computeIfAbsent(post.getMission().getId(), ignored -> new LinkedHashMap<>())
                    .putIfAbsent(
                            post.getMember().getId(),
                            KkinipopMissionSuccessMemberResponse.from(post.getMember())
                    );
        }

        return mappedMembers.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().values().stream().toList()
                ));
    }
}
