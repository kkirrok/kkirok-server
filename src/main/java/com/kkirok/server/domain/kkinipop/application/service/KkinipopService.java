package com.kkirok.server.domain.kkinipop.application.service;

import com.kkirok.server.domain.kkinipop.application.usecase.KkinipopUseCase;
import com.kkirok.server.domain.kkinipop.dao.KkinipopCustomEmojiRepository;
import com.kkirok.server.domain.kkinipop.dao.KkinipopGroupMemberRepository;
import com.kkirok.server.domain.kkinipop.dao.KkinipopGroupRepository;
import com.kkirok.server.domain.kkinipop.dao.KkinipopPostRepository;
import com.kkirok.server.domain.kkinipop.domain.KkinipopCustomEmoji;
import com.kkirok.server.domain.kkinipop.domain.KkinipopGroup;
import com.kkirok.server.domain.kkinipop.domain.KkinipopGroupMember;
import com.kkirok.server.domain.kkinipop.domain.KkinipopPost;
import com.kkirok.server.domain.kkinipop.exception.KkinipopErrorCode;
import com.kkirok.server.global.common.exception.NotFoundException;
import com.kkirok.server.global.common.util.DateTimeProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class KkinipopService implements KkinipopUseCase {

    private final KkinipopGroupMemberRepository groupMemberRepository;
    private final KkinipopPostRepository postRepository;
    private final KkinipopCustomEmojiRepository customEmojiRepository;
    private final KkinipopGroupRepository groupRepository;
    private final DateTimeProvider dateTimeProvider;

    @Override
    public KkinipopGroupMember findGroupMember(Long groupId, Long memberId) {
        return groupMemberRepository.findActiveMembership(groupId, memberId)
                .orElseThrow(() -> new NotFoundException(KkinipopErrorCode.GROUP_MEMBER_NOT_FOUND));
    }

    @Override
    public KkinipopPost findPostById(Long postId) {
        return postRepository.findById(postId)
                .filter(savedPost -> !savedPost.isDeleted())
                .orElseThrow(() -> new NotFoundException(KkinipopErrorCode.POST_NOT_FOUND));
    }

    @Override
    public KkinipopCustomEmoji findCustomEmojiById(Long customEmojiId) {
        return customEmojiRepository.findActiveEmoji(customEmojiId)
                .orElseThrow(() -> new NotFoundException(KkinipopErrorCode.CUSTOM_EMOJI_NOT_FOUND));
    }

    @Override
    public List<KkinipopGroup> findAllGroup() {
        return groupRepository.findAll();
    }

    @Override
    public long getMyKkirokCount(Long memberId, Long groupId) {
        return postRepository.countMyKkirokSavedPosts(groupId, memberId, dateTimeProvider.today());
    }

}
