package com.kkirok.server.domain.kkinipop.application.service;

import com.kkirok.server.domain.kkinipop.application.dto.event.KkinipopGroupJoinedEvent;
import com.kkirok.server.domain.kkinipop.application.dto.request.KkinipopGroupCreateRequest;
import com.kkirok.server.domain.kkinipop.application.dto.response.KkinipopGroupResponse;
import com.kkirok.server.domain.kkinipop.application.dto.response.KkinipopMemberSummaryResponse;
import com.kkirok.server.domain.kkinipop.application.usecase.KkinipopUseCase;
import com.kkirok.server.domain.kkinipop.dao.KkinipopGroupMemberRepository;
import com.kkirok.server.domain.kkinipop.dao.KkinipopGroupRepository;
import com.kkirok.server.domain.kkinipop.domain.KkinipopGroup;
import com.kkirok.server.domain.kkinipop.domain.KkinipopGroupMember;
import com.kkirok.server.domain.kkinipop.exception.KkinipopErrorCode;
import com.kkirok.server.domain.member.application.usecase.MemberUseCase;
import com.kkirok.server.domain.member.domain.Member;
import com.kkirok.server.global.common.exception.ConflictException;
import com.kkirok.server.global.common.exception.ForbiddenException;
import com.kkirok.server.global.common.exception.NotFoundException;
import com.kkirok.server.global.common.util.DateTimeProvider;
import java.security.SecureRandom;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = false)
@RequiredArgsConstructor
public class KkinipopGroupService {

    private static final String INVITE_CODE_SOURCE = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final KkinipopUseCase kkinipopUseCase;
    private final MemberUseCase memberUseCase;
    private final ApplicationEventPublisher eventPublisher;
    private final KkinipopGroupRepository groupRepository;
    private final KkinipopGroupMemberRepository groupMemberRepository;
    private final DateTimeProvider dateTimeProvider;

    // 내가 속한 그룹
    @Transactional(readOnly = true)
    public List<KkinipopGroupResponse> getGroups(Long memberId) {
        List<KkinipopGroupMember> groupMembers = groupMemberRepository.findActiveMemberships(memberId);
        return groupMembers.stream()
                .sorted(Comparator.comparing(KkinipopGroupMember::getCreatedAt))
                .map(this::toGroupResponse)
                .toList();
    }

    // 특정 그룹 멤버 정보
    @Transactional(readOnly = true)
    public List<KkinipopMemberSummaryResponse> getGroupMembers(Long memberId, Long groupId) {
        KkinipopGroupMember currentGroupMember = kkinipopUseCase.findGroupMember(groupId, memberId);
        List<KkinipopGroupMember> groupMembers = groupMemberRepository.findActiveGroupMembers(groupId);

        return groupMembers.stream()
                .map(groupMember -> KkinipopMemberSummaryResponse.from(groupMember, currentGroupMember.getMember().getId()))
                .toList();
    }

    // 그룹 생성
    public KkinipopGroupResponse createGroup(Long memberId, KkinipopGroupCreateRequest request) {
        Member member = memberUseCase.findMemberByMemberId(memberId);
        KkinipopGroup group = groupRepository.save(KkinipopGroup.create(request, generateInviteCode()));
        KkinipopGroupMember groupMember = groupMemberRepository.save(KkinipopGroupMember.createLeader(group, member));
        return KkinipopGroupResponse.from(groupMember, 1);
    }

    // 그룹 참여 - 참여코드 이용
    public KkinipopGroupResponse joinGroup(Long memberId, String code) {
        Member member = memberUseCase.findMemberByMemberId(memberId);
        KkinipopGroup group = groupRepository.findByInviteCode(code)
                .orElseThrow(() -> new NotFoundException(KkinipopErrorCode.GROUP_NOT_FOUND));

        if (groupMemberRepository.existsActiveMembership(group.getId(), memberId)) {
            throw new ConflictException(KkinipopErrorCode.GROUP_JOIN_CONFLICT);
        }

        KkinipopGroupMember groupMember = groupMemberRepository.save(KkinipopGroupMember.createMember(group, member));
        eventPublisher.publishEvent(new KkinipopGroupJoinedEvent(group.getId(), memberId));
        return KkinipopGroupResponse.from(groupMember, groupMemberRepository.findActiveGroupMembers(group.getId()).size());
    }

    // 그룹 탈퇴 - 탈퇴한 사람이 리더면 그룹 삭제
    public void leaveGroup(Long memberId, Long groupId) {
        KkinipopGroupMember groupMember = kkinipopUseCase.findGroupMember(groupId, memberId);
        if (groupMember.isLeader()) {
            deleteGroup(memberId, groupId);
            return;
        }

        groupMember.leave(dateTimeProvider.now());
    }

    // 그룹 삭제
    public void deleteGroup(Long memberId, Long groupId) {
        KkinipopGroupMember groupMember = kkinipopUseCase.findGroupMember(groupId, memberId);
        validateLeader(groupMember);

        KkinipopGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new NotFoundException(KkinipopErrorCode.GROUP_NOT_FOUND));

        groupRepository.delete(group);
    }

    // 멤버 제거 - 리더 권한
    public void removeGroupMember(Long memberId, Long groupId, Long targetMemberId) {
        KkinipopGroupMember leaderGroupMember = kkinipopUseCase.findGroupMember(groupId, memberId);
        validateLeader(leaderGroupMember);

        KkinipopGroupMember targetGroupMember = kkinipopUseCase.findGroupMember(groupId, targetMemberId);
        if (targetGroupMember.isLeader()) {
            throw new ForbiddenException(KkinipopErrorCode.GROUP_MANAGEMENT_FORBIDDEN);
        }

        targetGroupMember.leave(dateTimeProvider.now());
    }

    // 그룹 응답 생성 - 그룹조회 stream에서 호출
    private KkinipopGroupResponse toGroupResponse(KkinipopGroupMember groupMember) {
        int memberCount = groupMemberRepository.findActiveGroupMembers(groupMember.getGroup().getId()).size();
        return KkinipopGroupResponse.from(groupMember, memberCount);
    }

    // GroupMember에 해당하는 member가 Group의 leader인지
    private void validateLeader(KkinipopGroupMember groupMember) {
        if (!groupMember.isLeader()) {
            throw new ForbiddenException(KkinipopErrorCode.GROUP_MANAGEMENT_FORBIDDEN);
        }
    }

    // 초대코드 생성
    private String generateInviteCode() {
        String inviteCode;
        do {
            StringBuilder builder = new StringBuilder(6);
            for (int index = 0; index < 6; index++) {
                builder.append(INVITE_CODE_SOURCE.charAt(RANDOM.nextInt(INVITE_CODE_SOURCE.length())));
            }
            inviteCode = builder.toString();
        } while (groupRepository.existsByInviteCode(inviteCode));
        return inviteCode;
    }

}
