package com.kkirok.server.domain.kkinipop.application.usecase;

import com.kkirok.server.domain.kkinipop.domain.KkinipopCustomEmoji;
import com.kkirok.server.domain.kkinipop.domain.KkinipopGroupMember;
import com.kkirok.server.domain.kkinipop.domain.KkinipopPost;

public interface KkinipopUseCase {

    KkinipopGroupMember findGroupMember(Long groupId, Long memberId);
    KkinipopPost findPostById(Long postId);
    KkinipopCustomEmoji findCustomEmojiById(Long customEmojiId);
}
