package com.kkirok.server.domain.kkinipop.application.service;

import com.kkirok.server.domain.kkinipop.application.dto.response.KkinipopCustomEmojiResponse;
import com.kkirok.server.domain.kkinipop.application.dto.response.KkinipopEmojiListResponse;
import com.kkirok.server.domain.kkinipop.application.dto.response.KkinipopEmojiResponse;
import com.kkirok.server.domain.kkinipop.application.dto.response.KkinipopSystemEmojiResponse;
import com.kkirok.server.domain.kkinipop.application.usecase.KkinipopUseCase;
import com.kkirok.server.domain.kkinipop.dao.KkinipopCustomEmojiRepository;
import com.kkirok.server.domain.kkinipop.domain.KkinipopCustomEmoji;
import com.kkirok.server.domain.kkinipop.domain.KkinipopGroupMember;
import com.kkirok.server.domain.kkinipop.domain.KkinipopReactionEmoji;
import com.kkirok.server.domain.kkinipop.exception.KkinipopErrorCode;
import com.kkirok.server.global.common.exception.BadRequestException;
import com.kkirok.server.global.common.exception.ConflictException;
import com.kkirok.server.global.common.exception.ForbiddenException;
import com.kkirok.server.global.common.exception.NotFoundException;
import com.kkirok.server.global.common.util.DateTimeProvider;
import com.kkirok.server.global.external.r2.application.service.R2UploadService;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional(readOnly = false)
@RequiredArgsConstructor
public class KkinipopEmojiService {

    private static final int CUSTOM_EMOJI_LIMIT = 3;

    private final KkinipopUseCase kkinipopUseCase;
    private final KkinipopCustomEmojiRepository customEmojiRepository;
    private final R2UploadService r2UploadService;
    private final DateTimeProvider dateTimeProvider;

    // 그룳 이모지 조회
    @Transactional(readOnly = true)
    public KkinipopEmojiListResponse getEmojis(Long memberId, Long groupId) {
        kkinipopUseCase.findGroupMember(groupId, memberId);
        return new KkinipopEmojiListResponse(getSystemEmojis(), getCustomEmojis(groupId));
    }

    // 시스템이모지 조회
    @Transactional(readOnly = true)
    public List<KkinipopSystemEmojiResponse> getSystemEmojiOptions() {
        return EnumSet.allOf(KkinipopReactionEmoji.class).stream()
                .map(KkinipopSystemEmojiResponse::from)
                .toList();
    }

    // 커스텀 이모지 생성 - 팀 내 최대 개수 제한 있음
    public KkinipopCustomEmojiResponse createCustomEmoji(Long memberId, Long groupId, MultipartFile image) {

        // 기본 조회
        KkinipopGroupMember groupMember = kkinipopUseCase.findGroupMember(groupId, memberId);
        if (customEmojiRepository.countActiveGroupEmojis(groupId) >= CUSTOM_EMOJI_LIMIT) {
            throw new ConflictException(KkinipopErrorCode.CUSTOM_EMOJI_LIMIT_EXCEEDED);
        }

        // 이미지 업로드하고 이미지 정보를 KkinipopCustomEmoji로 저장
        String imageKey = uploadImage(image);
        String label = extractEmojiLabel(image);
        KkinipopCustomEmoji customEmoji = customEmojiRepository.save(
                KkinipopCustomEmoji.create(groupMember, label, imageKey)
        );

        // 반환
        return KkinipopCustomEmojiResponse.from(customEmoji);
    }

    // 커스텀 이모지 제거
    public void deleteCustomEmoji(Long memberId, Long groupId, Long customEmojiId) {
        KkinipopGroupMember groupMember = kkinipopUseCase.findGroupMember(groupId, memberId);
        KkinipopCustomEmoji customEmoji = kkinipopUseCase.findCustomEmojiById(customEmojiId);

        if (!customEmoji.getGroup().getId().equals(groupId)) {
            throw new ForbiddenException(KkinipopErrorCode.GROUP_ACCESS_FORBIDDEN);
        }
        if (!customEmoji.getCreator().getId().equals(memberId) && !groupMember.isLeader()) {
            throw new ForbiddenException(KkinipopErrorCode.CUSTOM_EMOJI_DELETE_FORBIDDEN);
        }

        customEmoji.delete(dateTimeProvider.now());
    }

    private List<KkinipopEmojiResponse> getSystemEmojis() {
        return EnumSet.allOf(KkinipopReactionEmoji.class).stream()
                .map(KkinipopEmojiResponse::ofDefault)
                .toList();
    }

    private List<KkinipopEmojiResponse> getCustomEmojis(Long groupId) {
        return customEmojiRepository.findActiveGroupEmojis(groupId).stream()
                .map(KkinipopEmojiResponse::ofCustom)
                .toList();
    }

    // 이미지는 필수
    private String uploadImage(MultipartFile image) {
        if (image == null || image.isEmpty()) {
            throw new BadRequestException(KkinipopErrorCode.PHOTO_REQUIRED);
        }
        return r2UploadService.upload(image);
    }

    // 이미지 원본 이름으로부터 Label 추출
    private String extractEmojiLabel(MultipartFile image) {
        String originalFilename = image.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            return "custom emoji";
        }

        int extensionIndex = originalFilename.lastIndexOf('.');
        String baseName = extensionIndex < 0
                ? originalFilename
                : originalFilename.substring(0, extensionIndex);

        return baseName.trim().isEmpty() ? "custom emoji" : baseName.trim().toLowerCase(Locale.ROOT);
    }

}
