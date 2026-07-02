package com.kkirok.server.domain.member.application.service;

import com.kkirok.server.domain.member.application.dto.request.NotificationAgreeUpdateRequest;
import com.kkirok.server.domain.member.application.dto.response.NotificationAgreeResponse;
import com.kkirok.server.domain.member.dao.NotificationAgreeRepository;
import com.kkirok.server.domain.member.domain.Member;
import com.kkirok.server.domain.member.domain.NotificationAgree;
import com.kkirok.server.domain.member.domain.NotificationAgreeId;
import com.kkirok.server.domain.member.domain.NotificationAgreeType;
import com.kkirok.server.domain.member.exception.MemberErrorCode;
import com.kkirok.server.global.common.exception.BadRequestException;
import com.kkirok.server.global.common.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationAgreeService {

    private final NotificationAgreeRepository notificationAgreeRepository;

    @Transactional
    public void initializeForMember(Member member) {
        List<NotificationAgree> agrees = Arrays.stream(NotificationAgreeType.values())
                .map(type -> NotificationAgree.of(member, type))
                .toList();
        notificationAgreeRepository.saveAll(agrees);
    }

    @Transactional(readOnly = true)
    public NotificationAgreeResponse getAll(Long memberId) {
        return NotificationAgreeResponse.of(notificationAgreeRepository.findAllByMember_Id(memberId));
    }

    @Transactional
    public NotificationAgreeResponse update(Long memberId, NotificationAgreeUpdateRequest request) {
        if (request.isAll()) {
            validateAllRequest(request.agrees());
        } else {
            validatePartialRequest(request.agrees());
        }
        for (var item : request.agrees()) {
            updateOne(memberId, item.type(), item.isAgree());
        }
        return getAll(memberId);
    }

    private void validateAllRequest(List<NotificationAgreeUpdateRequest.NotificationAgreeItem> agrees) {
        Set<NotificationAgreeType> requested = agrees.stream()
                .map(NotificationAgreeUpdateRequest.NotificationAgreeItem::type)
                .collect(Collectors.toSet());
        if (!requested.equals(Set.of(NotificationAgreeType.values()))) {
            throw new BadRequestException(MemberErrorCode.NOTIFICATION_ALL_AGREE_TYPE_MISMATCH);
        }
        long distinct = agrees.stream()
                .map(NotificationAgreeUpdateRequest.NotificationAgreeItem::isAgree)
                .distinct().count();
        if (distinct > 1) {
            throw new BadRequestException(MemberErrorCode.NOTIFICATION_ALL_AGREE_VALUE_MISMATCH);
        }
    }

    private void validatePartialRequest(List<NotificationAgreeUpdateRequest.NotificationAgreeItem> agrees) {
        Set<NotificationAgreeType> types = agrees.stream()
                .map(NotificationAgreeUpdateRequest.NotificationAgreeItem::type)
                .collect(Collectors.toSet());
        if (types.size() != agrees.size()) {
            throw new BadRequestException(MemberErrorCode.NOTIFICATION_AGREE_DUPLICATE_TYPE);
        }
    }

    private void updateOne(Long memberId, NotificationAgreeType type, boolean isAgree) {
        NotificationAgree agree = notificationAgreeRepository
                .findById(new NotificationAgreeId(memberId, type))
                .orElseThrow(() -> new NotFoundException(MemberErrorCode.NOTIFICATION_AGREE_NOT_FOUND));
        agree.update(isAgree);
    }

    @Transactional(readOnly = true)
    public Set<Long> findOptedOutMemberIds(Set<Long> memberIds, NotificationAgreeType agreeType) {
        return notificationAgreeRepository.findOptedOutMemberIds(memberIds, agreeType);
    }
}
