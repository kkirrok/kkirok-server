package com.kkirok.server.domain.kkinipop.application.service;

import com.kkirok.server.domain.kkinipop.application.dto.response.KkinipopDailyPostResponse;
import com.kkirok.server.domain.kkinipop.application.dto.event.KkinipopReactionAddedEvent;
import com.kkirok.server.domain.kkinipop.application.dto.event.KkinipopReactionChangedEvent;
import com.kkirok.server.domain.kkinipop.application.dto.response.KkinipopMyKkirokStatusResponse;
import com.kkirok.server.domain.kkinipop.application.dto.response.KkinipopPostResponse;
import com.kkirok.server.domain.kkinipop.application.dto.response.KkinipopReactionSummaryResponse;
import com.kkirok.server.domain.kkinipop.application.usecase.KkinipopUseCase;
import com.kkirok.server.domain.kkinipop.dao.KkinipopMissionRepository;
import com.kkirok.server.domain.kkinipop.dao.KkinipopPostRepository;
import com.kkirok.server.domain.kkinipop.dao.KkinipopReactionRepository;
import com.kkirok.server.domain.kkinipop.domain.KkinipopCustomEmoji;
import com.kkirok.server.domain.kkinipop.domain.KkinipopGroupMember;
import com.kkirok.server.domain.kkinipop.domain.KkinipopMission;
import com.kkirok.server.domain.kkinipop.domain.KkinipopPost;
import com.kkirok.server.domain.kkinipop.domain.KkinipopReaction;
import com.kkirok.server.domain.kkinipop.domain.KkinipopReactionEmoji;
import com.kkirok.server.domain.kkinipop.exception.KkinipopErrorCode;
import com.kkirok.server.domain.meal.domain.ScanType;
import com.kkirok.server.global.common.exception.BadRequestException;
import com.kkirok.server.global.common.exception.ConflictException;
import com.kkirok.server.global.common.exception.ForbiddenException;
import com.kkirok.server.global.common.exception.NotFoundException;
import com.kkirok.server.global.common.util.DateTimeProvider;
import com.kkirok.server.global.external.r2.application.service.R2UploadService;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional(readOnly = false)
@RequiredArgsConstructor
public class KkinipopPostService {

    public static final int MAX_MY_KKIROK_SAVE_COUNT = 3;

    private final KkinipopUseCase kkinipopUseCase;
    private final KkinipopPersonalLogService personalLogService;
    private final KkinipopMissionRepository missionRepository;
    private final KkinipopPostRepository postRepository;
    private final KkinipopReactionRepository reactionRepository;
    private final R2UploadService r2UploadService;
    private final DateTimeProvider dateTimeProvider;
    private final ApplicationEventPublisher eventPublisher;

    // 지정 날짜가 속한 주의 월~금 게시글 조회
    @Transactional(readOnly = true)
    public List<KkinipopDailyPostResponse> getPosts(Long memberId, Long groupId, LocalDate date, Long missionId) {
        kkinipopUseCase.findGroupMember(groupId, memberId);

        LocalDate targetDate = date != null ? date : dateTimeProvider.today();
        LocalDate startDate = targetDate.with(DayOfWeek.MONDAY);
        LocalDate endDate = targetDate.with(DayOfWeek.SUNDAY);
        validateTodayMission(groupId, missionId, targetDate);

        List<KkinipopPost> posts = postRepository.findPostsInDateRange(groupId, startDate, endDate, missionId);
        Map<Long, List<KkinipopReaction>> reactionsByPostId = getReactionsByPostId(posts);

        return toDailyPostResponses(memberId, startDate, endDate, posts, reactionsByPostId);
    }

    // 오늘 나의끼록과 같이 저장 가능한 남은 횟수 조회
    @Transactional(readOnly = true)
    public KkinipopMyKkirokStatusResponse getMyKkirokStatus(Long memberId, Long groupId) {
        kkinipopUseCase.findGroupMember(groupId, memberId);
        long usedCount = kkinipopUseCase.getMyKkirokCount(memberId, groupId);
        return KkinipopMyKkirokStatusResponse.of(usedCount, MAX_MY_KKIROK_SAVE_COUNT);
    }

    // 게시글 생성
    public KkinipopPostResponse createPost(Long memberId, Long groupId, boolean saveToPersonalLog, MultipartFile image, ScanType scanType) {

        // 준비
        KkinipopGroupMember groupMember = kkinipopUseCase.findGroupMember(groupId, memberId);
        LocalDate recordDate = dateTimeProvider.today();
        LocalDateTime currentTime = dateTimeProvider.now();

        // 이미지 없으면 안됨
        if (image == null || image.isEmpty()) {
            throw new BadRequestException(KkinipopErrorCode.PHOTO_REQUIRED);
        }

        // 현재의 실시간 미션 조회
        KkinipopMission mission = missionRepository.findLiveMissions(groupId, currentTime).stream()
                .findFirst()
                .orElseThrow(() -> new BadRequestException(KkinipopErrorCode.LIVE_MISSION_NOT_FOUND));

        // 그 미션이 끝난 상태면 예외 발생
        if (mission.isEnded(currentTime)) {
            throw new BadRequestException(KkinipopErrorCode.LIVE_MISSION_ENDED);
        }

        validateMyKkirokLimit(groupId, memberId, recordDate, saveToPersonalLog);

        // 사진 업로드
        String imageKey = r2UploadService.upload(image);
        KkinipopPost post = postRepository.save(
                KkinipopPost.create(groupMember, mission, imageKey, recordDate, saveToPersonalLog)
        );

        // 끼니팝 게시글과 식단기록을 동시에 올리기 (나의끼록 저장 실패 시 예외가 전파되어 게시글 저장도 함께 롤백됨)
        if (saveToPersonalLog) {
            personalLogService.recordPersonalLog(memberId, image, scanType);
        }

        return KkinipopPostResponse.from(post, List.of());
    }

    // 게시글 삭제
    public void deletePost(Long memberId, Long groupId, Long postId) {

        kkinipopUseCase.findGroupMember(groupId, memberId);
        KkinipopPost post = kkinipopUseCase.findPostById(postId);

        // 검증
        if (!post.getGroup().getId().equals(groupId)) {
            throw new ForbiddenException(KkinipopErrorCode.GROUP_ACCESS_FORBIDDEN);
        }
        if (!post.getMember().getId().equals(memberId)) {
            throw new ForbiddenException(KkinipopErrorCode.POST_DELETE_FORBIDDEN);
        }

        post.delete(dateTimeProvider.now());
    }

    // 게시글 반응하기
    public KkinipopReactionSummaryResponse reactToPost(Long memberId, Long groupId, Long postId, String emojiCode) {
        KkinipopGroupMember groupMember = kkinipopUseCase.findGroupMember(groupId, memberId);
        KkinipopPost post = kkinipopUseCase.findPostById(postId);

        if (!post.getGroup().getId().equals(groupId)) {
            throw new ForbiddenException(KkinipopErrorCode.GROUP_ACCESS_FORBIDDEN);
        }
        if (emojiCode == null || emojiCode.isBlank()) {
            throw new BadRequestException(KkinipopErrorCode.INVALID_REACTION_REQUEST);
        }

        String normalizedEmojiCode = emojiCode.trim();
        if (normalizedEmojiCode.startsWith("CUSTOM_")) { // 커스텀 이모지이면
            return createCustomEmojiReaction(groupMember, groupId, post, normalizedEmojiCode);
        }

        return createSystemEmojiReaction(groupMember, post, normalizedEmojiCode);
    }

    // 커스텀 이모지로 반응 생성
    private KkinipopReactionSummaryResponse createCustomEmojiReaction(
            KkinipopGroupMember groupMember,
            Long groupId,
            KkinipopPost post,
            String emojiCode
    ) {

        Long customEmojiId = parseCustomEmojiId(emojiCode);
        KkinipopCustomEmoji customEmoji = kkinipopUseCase.findCustomEmojiById(customEmojiId);
        if (!customEmoji.getGroup().getId().equals(groupId)) {
            throw new ForbiddenException(KkinipopErrorCode.GROUP_ACCESS_FORBIDDEN);
        }

        Optional<KkinipopReaction> existingReaction = reactionRepository.findByPostAndMemberAndEmojiCode(
                post.getId(),
                groupMember.getMember().getId(),
                emojiCode
        );
        if (existingReaction.isPresent()) {
            reactionRepository.delete(existingReaction.get());
            long count = reactionRepository.countByPostAndEmojiCode(post.getId(), emojiCode);
            eventPublisher.publishEvent(new KkinipopReactionChangedEvent(post.getId(), groupId, emojiCode, count, false));
            return new KkinipopReactionSummaryResponse(emojiCode, customEmoji.getLabel(), count, "CUSTOM_EMOJI", false);
        }

        KkinipopReaction reaction = reactionRepository.save(
                KkinipopReaction.createCustom(post, groupMember.getMember(), customEmoji)
        );
        long count = reactionRepository.countByPostAndEmojiCode(post.getId(), emojiCode);
        eventPublisher.publishEvent(new KkinipopReactionChangedEvent(post.getId(), groupId, emojiCode, count, true));

        if (!groupMember.getMember().getId().equals(post.getMember().getId())) {
            eventPublisher.publishEvent(new KkinipopReactionAddedEvent(
                    post.getId(),
                    groupId,
                    post.getMember().getId(),
                    groupMember.getMember().getId(),
                    emojiCode,
                    true,
                    customEmoji.getImageKey()
            ));
        }

        return KkinipopReactionSummaryResponse.from(reaction, count, true);
    }

    // 시스템 이미지로 반응 생성
    private KkinipopReactionSummaryResponse createSystemEmojiReaction(
            KkinipopGroupMember groupMember,
            KkinipopPost post,
            String emojiCode
    ) {
        try {
            KkinipopReactionEmoji emoji = KkinipopReactionEmoji.fromCode(emojiCode);
            Optional<KkinipopReaction> existingReaction = reactionRepository.findByPostAndMemberAndEmojiCode(
                    post.getId(),
                    groupMember.getMember().getId(),
                    emojiCode
            );
            if (existingReaction.isPresent()) {
                reactionRepository.delete(existingReaction.get());
                long count = reactionRepository.countByPostAndEmojiCode(post.getId(), emojiCode);
                eventPublisher.publishEvent(new KkinipopReactionChangedEvent(post.getId(), post.getGroup().getId(), emojiCode, count, false));
                return new KkinipopReactionSummaryResponse(emojiCode, emoji.getLabel(), count, "SYSTEM_EMOJI", false);
            }

            KkinipopReaction reaction = reactionRepository.save(
                    KkinipopReaction.createDefault(post, groupMember.getMember(), emoji)
            );
            long count = reactionRepository.countByPostAndEmojiCode(post.getId(), emojiCode);
            eventPublisher.publishEvent(new KkinipopReactionChangedEvent(post.getId(), post.getGroup().getId(), emojiCode, count, true));

            if (!groupMember.getMember().getId().equals(post.getMember().getId())) {
                eventPublisher.publishEvent(new KkinipopReactionAddedEvent(
                        post.getId(),
                        post.getGroup().getId(),
                        post.getMember().getId(),
                        groupMember.getMember().getId(),
                        emojiCode,
                        false,
                        null
                ));
            }

            return KkinipopReactionSummaryResponse.from(reaction, count, true);
        } catch (IllegalArgumentException exception) {
            throw new BadRequestException(KkinipopErrorCode.INVALID_REACTION_REQUEST, exception);
        }
    }

    private void validateMyKkirokLimit(Long groupId, Long memberId, LocalDate recordDate, boolean saveToPersonalLog) {
        if (!saveToPersonalLog) {
            return;
        }

        long usedCount = postRepository.countMyKkirokSavedPosts(groupId, memberId, recordDate);
        if (usedCount >= MAX_MY_KKIROK_SAVE_COUNT) {
            throw new ConflictException(KkinipopErrorCode.GENERAL_POST_LIMIT_EXCEEDED);
        }
    }

    private void validateTodayMission(Long groupId, Long missionId, LocalDate today) {

        if (missionId == null) return;

        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.plusDays(1).atStartOfDay();
        missionRepository.findTodayMission(groupId, missionId, startOfDay, endOfDay) // 오늘 날짜인지 검증
                .orElseThrow(() -> new NotFoundException(KkinipopErrorCode.MISSION_NOT_FOUND));
    }

    // List<Post>로부터 리액션 정보 조회
    private Map<Long, List<KkinipopReaction>> getReactionsByPostId(List<KkinipopPost> posts) {
        if (posts.isEmpty()) {
            return Map.of();
        }

        List<Long> postIds = posts.stream()
                .map(KkinipopPost::getId)
                .toList();

        return reactionRepository.findPostReactions(postIds).stream()
                .collect(Collectors.groupingBy(reaction -> reaction.getPost().getId()));
    }

    private List<KkinipopDailyPostResponse> toDailyPostResponses(
            Long memberId,
            LocalDate startDate,
            LocalDate endDate,
            List<KkinipopPost> posts,
            Map<Long, List<KkinipopReaction>> reactionsByPostId
    ) {
        Map<LocalDate, List<KkinipopPost>> postsByDate = posts.stream()
                .collect(Collectors.groupingBy(
                        KkinipopPost::getRecordDate,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        return startDate.datesUntil(endDate.plusDays(1))
                .sorted(Comparator.reverseOrder())
                .map(date -> KkinipopDailyPostResponse.of(
                        date,
                        postsByDate.getOrDefault(date, List.of()).stream()
                                .map(post -> toPostResponse(memberId, post, reactionsByPostId))
                                .toList()
                ))
                .toList();
    }

    private KkinipopPostResponse toPostResponse(
            Long memberId,
            KkinipopPost post,
            Map<Long, List<KkinipopReaction>> reactionsByPostId
    ) {
        List<KkinipopReactionSummaryResponse> reactionSummaries = toReactionSummaries(
                memberId,
                reactionsByPostId.getOrDefault(post.getId(), List.of())
        );

        return KkinipopPostResponse.from(post, reactionSummaries);
    }

    private List<KkinipopReactionSummaryResponse> toReactionSummaries(Long memberId, List<KkinipopReaction> reactions) {
        if (reactions.isEmpty()) {
            return List.of();
        }

        Map<String, KkinipopReactionSummaryResponse> summaryMap = new LinkedHashMap<>();
        for (KkinipopReaction reaction : reactions) {
            boolean reacted = reaction.getMember().getId().equals(memberId);
            KkinipopReactionSummaryResponse summary = summaryMap.get(reaction.getEmojiCode());
            if (summary == null) {
                summaryMap.put(reaction.getEmojiCode(), KkinipopReactionSummaryResponse.from(reaction, 1L, reacted));
                continue;
            }

            summaryMap.put(
                    reaction.getEmojiCode(),
                    KkinipopReactionSummaryResponse.of(summary, summary.count() + 1L, summary.reacted() || reacted)
            );
        }

        return new ArrayList<>(summaryMap.values());
    }

    private Long parseCustomEmojiId(String emojiCode) {
        try {
            return Long.parseLong(emojiCode.substring("CUSTOM_".length()));
        } catch (NumberFormatException exception) {
            throw new BadRequestException(KkinipopErrorCode.INVALID_REACTION_REQUEST);
        }
    }
}
