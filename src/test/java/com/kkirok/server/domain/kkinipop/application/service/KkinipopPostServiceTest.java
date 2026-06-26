package com.kkirok.server.domain.kkinipop.application.service;

import com.kkirok.server.domain.kkinipop.application.dto.response.KkinipopDailyPostResponse;
import com.kkirok.server.domain.kkinipop.application.dto.event.KkinipopReactionAddedEvent;
import com.kkirok.server.domain.kkinipop.application.dto.response.KkinipopPostResponse;
import com.kkirok.server.domain.kkinipop.application.dto.response.KkinipopReactionSummaryResponse;
import com.kkirok.server.domain.kkinipop.application.usecase.KkinipopUseCase;
import com.kkirok.server.domain.kkinipop.dao.KkinipopMissionRepository;
import com.kkirok.server.domain.kkinipop.dao.KkinipopPostRepository;
import com.kkirok.server.domain.kkinipop.dao.KkinipopReactionRepository;
import com.kkirok.server.domain.kkinipop.domain.KkinipopCustomEmoji;
import com.kkirok.server.domain.kkinipop.domain.KkinipopGroup;
import com.kkirok.server.domain.kkinipop.domain.KkinipopGroupMember;
import com.kkirok.server.domain.kkinipop.domain.KkinipopMission;
import com.kkirok.server.domain.kkinipop.domain.KkinipopPost;
import com.kkirok.server.domain.kkinipop.domain.KkinipopReaction;
import com.kkirok.server.domain.kkinipop.domain.KkinipopReactionEmoji;
import com.kkirok.server.domain.kkinipop.exception.KkinipopErrorCode;
import com.kkirok.server.domain.meal.domain.ScanType;
import com.kkirok.server.domain.member.domain.Member;
import com.kkirok.server.global.common.exception.BadRequestException;
import com.kkirok.server.global.common.exception.ConflictException;
import com.kkirok.server.global.common.exception.ForbiddenException;
import com.kkirok.server.global.common.exception.NotFoundException;
import com.kkirok.server.global.common.util.DateTimeProvider;
import com.kkirok.server.global.external.r2.application.service.R2UploadService;
import com.kkirok.server.support.fixture.MemberFixture;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class KkinipopPostServiceTest {

    @Mock
    private KkinipopUseCase kkinipopUseCase;

    @Mock
    private KkinipopPersonalLogService personalLogService;

    @Mock
    private KkinipopMissionRepository missionRepository;

    @Mock
    private KkinipopPostRepository postRepository;

    @Mock
    private KkinipopReactionRepository reactionRepository;

    @Mock
    private R2UploadService r2UploadService;

    @Mock
    private DateTimeProvider dateTimeProvider;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private KkinipopPostService kkinipopPostService;

    @Test
    @DisplayName("현재 시간에 해당하는 실시간 미션이 있으면 게시글을 생성할 수 있다")
    void shouldCreatePost_whenLiveMissionExists() {
        // Given
        KkinipopGroup group = createGroup(10L, "아침 챌린저스");
        Member member = createMember(1L, "끼록이");
        KkinipopGroupMember groupMember = createGroupMember(100L, group, member);
        KkinipopMission mission = createMission(20L, group, "실시간 미션",
                LocalDateTime.of(2026, 4, 24, 9, 0),
                LocalDateTime.of(2026, 4, 24, 9, 10));
        LocalDateTime now = LocalDateTime.of(2026, 4, 24, 9, 5);
        LocalDate today = LocalDate.of(2026, 4, 24);
        MockMultipartFile image = new MockMultipartFile("image", "meal.png", "image/png", "meal".getBytes());

        given(kkinipopUseCase.findGroupMember(10L, 1L)).willReturn(groupMember);
        given(dateTimeProvider.today()).willReturn(today);
        given(dateTimeProvider.now()).willReturn(now);
        given(missionRepository.findLiveMissions(10L, now)).willReturn(List.of(mission));
        given(postRepository.countMyKkirokSavedPosts(10L, 1L, today)).willReturn(0L);
        given(r2UploadService.upload(image)).willReturn("uuid_kkinipopPostImage");
        given(personalLogService.tryRecordPersonalLog(1L, image, ScanType.CAMERA)).willReturn(true);
        given(postRepository.save(any(KkinipopPost.class))).willAnswer(invocation -> {
            KkinipopPost post = invocation.getArgument(0);
            ReflectionTestUtils.setField(post, "id", 30L);
            ReflectionTestUtils.setField(post, "createdAt", LocalDateTime.of(2026, 4, 24, 9, 5, 10));
            return post;
        });

        // When
        KkinipopPostResponse response = kkinipopPostService.createPost(1L, 10L, true, image, ScanType.CAMERA);

        // Then
        assertThat(response.postId()).isEqualTo(30L);
        assertThat(response.memberId()).isEqualTo(1L);
        assertThat(response.missionId()).isEqualTo(20L);
        assertThat(response.image()).isEqualTo("uuid_kkinipopPostImage");
        then(personalLogService).should().tryRecordPersonalLog(1L, image, ScanType.CAMERA);
    }

    @Test
    @DisplayName("나의끼록 저장 실패 시 게시글은 생성되고 저장 횟수는 차감되지 않는다")
    void shouldCreatePostWithoutPersonalLog_whenPersonalLogRecordingFails() {
        // Given
        KkinipopGroup group = createGroup(10L, "아침 챌린저스");
        Member member = createMember(1L, "끼록이");
        KkinipopGroupMember groupMember = createGroupMember(100L, group, member);
        KkinipopMission mission = createMission(20L, group, "실시간 미션",
                LocalDateTime.of(2026, 4, 24, 9, 0),
                LocalDateTime.of(2026, 4, 24, 9, 10));
        LocalDateTime now = LocalDateTime.of(2026, 4, 24, 9, 5);
        LocalDate today = LocalDate.of(2026, 4, 24);
        MockMultipartFile image = new MockMultipartFile("image", "meal.png", "image/png", "meal".getBytes());

        given(kkinipopUseCase.findGroupMember(10L, 1L)).willReturn(groupMember);
        given(dateTimeProvider.today()).willReturn(today);
        given(dateTimeProvider.now()).willReturn(now);
        given(missionRepository.findLiveMissions(10L, now)).willReturn(List.of(mission));
        given(postRepository.countMyKkirokSavedPosts(10L, 1L, today)).willReturn(0L);
        given(r2UploadService.upload(image)).willReturn("uuid_kkinipopPostImage");
        given(personalLogService.tryRecordPersonalLog(1L, image, ScanType.CAMERA)).willReturn(false);

        ArgumentCaptor<KkinipopPost> postCaptor = ArgumentCaptor.forClass(KkinipopPost.class);
        given(postRepository.save(postCaptor.capture())).willAnswer(invocation -> {
            KkinipopPost post = invocation.getArgument(0);
            ReflectionTestUtils.setField(post, "id", 31L);
            ReflectionTestUtils.setField(post, "createdAt", LocalDateTime.of(2026, 4, 24, 9, 5, 10));
            return post;
        });

        // When
        KkinipopPostResponse response = kkinipopPostService.createPost(1L, 10L, true, image, ScanType.CAMERA);

        // Then
        assertThat(response.postId()).isEqualTo(31L);
        assertThat(postCaptor.getValue().isSaveToPersonalLog()).isFalse();
    }

    @Test
    @DisplayName("현재 시간에 해당하는 실시간 미션이 없으면 게시글을 작성할 수 없다")
    void shouldThrowBadRequestException_whenNoLiveMissionExists() {
        // Given
        KkinipopGroup group = createGroup(10L, "아침 챌린저스");
        Member member = createMember(1L, "끼록이");
        KkinipopGroupMember groupMember = createGroupMember(100L, group, member);
        LocalDateTime now = LocalDateTime.of(2026, 4, 24, 9, 5);
        MockMultipartFile image = new MockMultipartFile("image", "meal.png", "image/png", "meal".getBytes());

        given(kkinipopUseCase.findGroupMember(10L, 1L)).willReturn(groupMember);
        given(dateTimeProvider.today()).willReturn(LocalDate.of(2026, 4, 24));
        given(dateTimeProvider.now()).willReturn(now);
        given(missionRepository.findLiveMissions(10L, now)).willReturn(List.of());

        // When, Then
        assertThatThrownBy(() -> kkinipopPostService.createPost(1L, 10L, true, image, ScanType.CAMERA))
                .isInstanceOf(BadRequestException.class)
                .extracting("baseErrorCode")
                .isEqualTo(KkinipopErrorCode.LIVE_MISSION_NOT_FOUND);
    }

    @Test
    @DisplayName("나의끼록과 같이 저장하는 끼니팝은 하루 세 번까지만 저장할 수 있다")
    void shouldThrowConflictException_whenMyKkirokSaveLimitExceeded() {
        // Given
        KkinipopGroup group = createGroup(10L, "아침 챌린저스");
        Member member = createMember(1L, "끼록이");
        KkinipopGroupMember groupMember = createGroupMember(100L, group, member);
        KkinipopMission mission = createMission(20L, group, "실시간 미션",
                LocalDateTime.of(2026, 4, 24, 9, 0),
                LocalDateTime.of(2026, 4, 24, 9, 10));
        LocalDateTime now = LocalDateTime.of(2026, 4, 24, 9, 5);
        LocalDate today = LocalDate.of(2026, 4, 24);
        MockMultipartFile image = new MockMultipartFile("image", "meal.png", "image/png", "meal".getBytes());

        given(kkinipopUseCase.findGroupMember(10L, 1L)).willReturn(groupMember);
        given(dateTimeProvider.today()).willReturn(today);
        given(dateTimeProvider.now()).willReturn(now);
        given(missionRepository.findLiveMissions(10L, now)).willReturn(List.of(mission));
        given(postRepository.countMyKkirokSavedPosts(10L, 1L, today)).willReturn(3L);

        // When, Then
        assertThatThrownBy(() -> kkinipopPostService.createPost(1L, 10L, true, image, ScanType.CAMERA))
                .isInstanceOf(ConflictException.class)
                .extracting("baseErrorCode")
                .isEqualTo(KkinipopErrorCode.GENERAL_POST_LIMIT_EXCEEDED);
    }

    @Test
    @DisplayName("본인 게시글이 아니면 삭제할 수 없다")
    void shouldThrowForbiddenException_whenDeletingOthersPost() {
        // Given
        KkinipopGroup group = createGroup(10L, "아침 챌린저스");
        Member member = createMember(1L, "끼록이");
        Member writer = createMember(2L, "다른사람");
        KkinipopGroupMember groupMember = createGroupMember(100L, group, member);
        KkinipopPost post = createPost(30L, group, writer, null, LocalDate.of(2026, 4, 24), "uuid_post");

        given(kkinipopUseCase.findGroupMember(10L, 1L)).willReturn(groupMember);
        given(kkinipopUseCase.findPostById(30L)).willReturn(post);

        // When, Then
        assertThatThrownBy(() -> kkinipopPostService.deletePost(1L, 10L, 30L))
                .isInstanceOf(ForbiddenException.class)
                .extracting("baseErrorCode")
                .isEqualTo(KkinipopErrorCode.POST_DELETE_FORBIDDEN);
    }

    @Test
    @DisplayName("커스텀 이모지로 반응하면 커스텀 타입으로 응답을 반환한다")
    void shouldCreateCustomReaction_whenEmojiCodeIsCustom() {
        // Given
        KkinipopGroup group = createGroup(10L, "아침 챌린저스");
        Member member = createMember(1L, "끼록이");
        KkinipopGroupMember groupMember = createGroupMember(100L, group, member);
        KkinipopPost post = createPost(30L, group, member, null, LocalDate.of(2026, 4, 24), "uuid_post");
        KkinipopCustomEmoji customEmoji = createCustomEmoji(5L, group, member, "chew", "uuid_emoji");

        given(kkinipopUseCase.findGroupMember(10L, 1L)).willReturn(groupMember);
        given(kkinipopUseCase.findPostById(30L)).willReturn(post);
        given(kkinipopUseCase.findCustomEmojiById(5L)).willReturn(customEmoji);
        given(reactionRepository.findByPostAndMemberAndEmojiCode(30L, 1L, "CUSTOM_5")).willReturn(Optional.empty());
        given(reactionRepository.save(any(KkinipopReaction.class))).willAnswer(invocation -> invocation.getArgument(0));
        given(reactionRepository.countByPostAndEmojiCode(30L, "CUSTOM_5")).willReturn(1L);

        // When
        KkinipopReactionSummaryResponse response = kkinipopPostService.reactToPost(1L, 10L, 30L, "CUSTOM_5");

        // Then
        assertThat(response.emojiCode()).isEqualTo("CUSTOM_5");
        assertThat(response.label()).isEqualTo("chew");
        assertThat(response.count()).isEqualTo(1L);
        assertThat(response.emojiType()).isEqualTo("CUSTOM_EMOJI");
        assertThat(response.reacted()).isTrue();
    }

    @Test
    @DisplayName("시스템 이모지 첫 반응이면 알림 이벤트를 발행한다")
    void shouldPublishReactionEvent_whenSystemEmojiIsFirstReaction() {
        // Given
        KkinipopGroup group = createGroup(10L, "아침 챌린저스");
        Member writer = createMember(1L, "작성자");
        Member reactor = createMember(2L, "반응자");
        KkinipopGroupMember groupMember = createGroupMember(100L, group, reactor);
        KkinipopPost post = createPost(30L, group, writer, null, LocalDate.of(2026, 4, 24), "uuid_post");

        given(kkinipopUseCase.findGroupMember(10L, 2L)).willReturn(groupMember);
        given(kkinipopUseCase.findPostById(30L)).willReturn(post);
        given(reactionRepository.findByPostAndMemberAndEmojiCode(30L, 2L, "SYSTEM_HEART")).willReturn(Optional.empty());
        given(reactionRepository.save(any(KkinipopReaction.class))).willAnswer(invocation -> invocation.getArgument(0));
        given(reactionRepository.countByPostAndEmojiCode(30L, "SYSTEM_HEART")).willReturn(1L);

        // When
        kkinipopPostService.reactToPost(2L, 10L, 30L, "SYSTEM_HEART");

        // Then
        ArgumentCaptor<KkinipopReactionAddedEvent> eventCaptor =
                ArgumentCaptor.forClass(KkinipopReactionAddedEvent.class);
        then(eventPublisher).should().publishEvent(eventCaptor.capture());

        KkinipopReactionAddedEvent event = eventCaptor.getValue();
        assertThat(event.postId()).isEqualTo(30L);
        assertThat(event.groupId()).isEqualTo(10L);
        assertThat(event.postAuthorMemberId()).isEqualTo(1L);
        assertThat(event.reactorMemberId()).isEqualTo(2L);
        assertThat(event.emojiCode()).isEqualTo("SYSTEM_HEART");
        assertThat(event.customEmoji()).isFalse();
        assertThat(event.customEmojiImageKey()).isNull();
    }

    @Test
    @DisplayName("동일한 게시글, 반응자, 이모지 조합이 이미 있으면 리액션을 해제한다")
    void shouldRemoveReaction_whenSameEmojiAlreadyExists() {
        // Given
        KkinipopGroup group = createGroup(10L, "아침 챌린저스");
        Member writer = createMember(1L, "작성자");
        Member reactor = createMember(2L, "반응자");
        KkinipopGroupMember groupMember = createGroupMember(100L, group, reactor);
        KkinipopPost post = createPost(30L, group, writer, null, LocalDate.of(2026, 4, 24), "uuid_post");
        KkinipopReaction existingReaction = createDefaultReaction(post, reactor, KkinipopReactionEmoji.HEART);

        given(kkinipopUseCase.findGroupMember(10L, 2L)).willReturn(groupMember);
        given(kkinipopUseCase.findPostById(30L)).willReturn(post);
        given(reactionRepository.findByPostAndMemberAndEmojiCode(30L, 2L, "SYSTEM_HEART"))
                .willReturn(Optional.of(existingReaction));
        given(reactionRepository.countByPostAndEmojiCode(30L, "SYSTEM_HEART")).willReturn(1L);

        // When
        KkinipopReactionSummaryResponse response = kkinipopPostService.reactToPost(2L, 10L, 30L, "SYSTEM_HEART");

        // Then
        assertThat(response.emojiCode()).isEqualTo("SYSTEM_HEART");
        assertThat(response.label()).isEqualTo("하트");
        assertThat(response.count()).isEqualTo(1L);
        assertThat(response.emojiType()).isEqualTo("SYSTEM_EMOJI");
        assertThat(response.reacted()).isFalse();
        then(reactionRepository).should().delete(existingReaction);
        then(reactionRepository).should(never()).save(any(KkinipopReaction.class));
    }

    @Test
    @DisplayName("리액션 해제 시 알림 이벤트를 발행하지 않는다")
    void shouldNotPublishEvent_whenReactionIsRemoved() {
        // Given
        KkinipopGroup group = createGroup(10L, "아침 챌린저스");
        Member writer = createMember(1L, "작성자");
        Member reactor = createMember(2L, "반응자");
        KkinipopGroupMember groupMember = createGroupMember(100L, group, reactor);
        KkinipopPost post = createPost(30L, group, writer, null, LocalDate.of(2026, 4, 24), "uuid_post");
        KkinipopReaction existingReaction = createDefaultReaction(post, reactor, KkinipopReactionEmoji.HEART);

        given(kkinipopUseCase.findGroupMember(10L, 2L)).willReturn(groupMember);
        given(kkinipopUseCase.findPostById(30L)).willReturn(post);
        given(reactionRepository.findByPostAndMemberAndEmojiCode(30L, 2L, "SYSTEM_HEART"))
                .willReturn(Optional.of(existingReaction));
        given(reactionRepository.countByPostAndEmojiCode(30L, "SYSTEM_HEART")).willReturn(0L);

        // When
        kkinipopPostService.reactToPost(2L, 10L, 30L, "SYSTEM_HEART");

        // Then
        then(eventPublisher).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("본인 게시글에 본인이 반응하면 알림을 발행하지 않는다")
    void shouldNotPublishReactionEvent_whenReactingToOwnPost() {
        // Given
        KkinipopGroup group = createGroup(10L, "아침 챌린저스");
        Member member = createMember(1L, "작성자");
        KkinipopGroupMember groupMember = createGroupMember(100L, group, member);
        KkinipopPost post = createPost(30L, group, member, null, LocalDate.of(2026, 4, 24), "uuid_post");

        given(kkinipopUseCase.findGroupMember(10L, 1L)).willReturn(groupMember);
        given(kkinipopUseCase.findPostById(30L)).willReturn(post);
        given(reactionRepository.findByPostAndMemberAndEmojiCode(30L, 1L, "SYSTEM_HEART")).willReturn(Optional.empty());
        given(reactionRepository.save(any(KkinipopReaction.class))).willAnswer(invocation -> invocation.getArgument(0));
        given(reactionRepository.countByPostAndEmojiCode(30L, "SYSTEM_HEART")).willReturn(1L);

        // When
        kkinipopPostService.reactToPost(1L, 10L, 30L, "SYSTEM_HEART");

        // Then
        then(eventPublisher).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("게시글을 조회하면 이번 주 월요일부터 일요일까지 날짜별 목록을 반환한다")
    void shouldReturnWeeklyDailyPostResponses_whenRequestingPosts() {
        // Given
        KkinipopGroup group = createGroup(10L, "아침 챌린저스");
        Member member = createMember(1L, "끼록이");
        KkinipopGroupMember groupMember = createGroupMember(100L, group, member);
        KkinipopMission mission = createMission(20L, group, "실시간 미션",
                LocalDateTime.of(2026, 4, 24, 9, 0),
                LocalDateTime.of(2026, 4, 24, 9, 10));
        KkinipopPost post = createPost(30L, group, member, mission, LocalDate.of(2026, 4, 24), "uuid_post");
        KkinipopReaction reaction = createDefaultReaction(post, member, KkinipopReactionEmoji.HEART);

        ReflectionTestUtils.setField(post, "createdAt", LocalDateTime.of(2026, 4, 24, 9, 5));

        given(kkinipopUseCase.findGroupMember(10L, 1L)).willReturn(groupMember);
        given(dateTimeProvider.today()).willReturn(LocalDate.of(2026, 4, 24));
        given(postRepository.findPostsInDateRange(10L, LocalDate.of(2026, 4, 20), LocalDate.of(2026, 4, 26), null))
                .willReturn(List.of(post));
        given(reactionRepository.findPostReactions(List.of(30L))).willReturn(List.of(reaction));

        // When
        List<KkinipopDailyPostResponse> responses = kkinipopPostService.getPosts(1L, 10L, null, null);

        // Then
        assertThat(responses).hasSize(7);
        assertThat(responses.get(0).date()).isEqualTo(LocalDate.of(2026, 4, 26));
        assertThat(responses.get(2).date()).isEqualTo(LocalDate.of(2026, 4, 24));
        assertThat(responses.get(2).posts()).hasSize(1);
        assertThat(responses.get(2).posts().get(0).reactions()).hasSize(1);
        assertThat(responses.get(2).posts().get(0).reactions().get(0).emojiType()).isEqualTo("SYSTEM_EMOJI");
        assertThat(responses.get(2).posts().get(0).reactions().get(0).reacted()).isTrue();
        assertThat(responses.get(6).date()).isEqualTo(LocalDate.of(2026, 4, 20));
    }

    @Test
    @DisplayName("게시글 조회 시 추방된 멤버의 게시글은 제외된다")
    void shouldExcludeBannedMemberPosts_whenGetPosts() {
        // Given
        KkinipopGroup group = createGroup(10L, "아침 챌린저스");
        Member member = createMember(1L, "끼록이");
        KkinipopGroupMember groupMember = createGroupMember(100L, group, member);

        given(kkinipopUseCase.findGroupMember(10L, 1L)).willReturn(groupMember);
        given(dateTimeProvider.today()).willReturn(LocalDate.of(2026, 4, 24));
        given(postRepository.findPostsInDateRange(10L, LocalDate.of(2026, 4, 20), LocalDate.of(2026, 4, 26), null))
                .willReturn(List.of());

        // When
        List<KkinipopDailyPostResponse> responses = kkinipopPostService.getPosts(1L, 10L, null, null);

        // Then
        // Banned member filtering is enforced by the repository query; this unit test only simulates the query result.
        assertThat(responses).hasSize(7);
        assertThat(responses.get(2).date()).isEqualTo(LocalDate.of(2026, 4, 24));
        assertThat(responses.get(2).posts()).isEmpty();
    }

    @Test
    @DisplayName("게시글 조회 시 추방된 멤버의 리액션은 제외된다")
    void shouldExcludeBannedMemberReactions_whenGetPosts() {
        // Given
        KkinipopGroup group = createGroup(10L, "아침 챌린저스");
        Member member = createMember(1L, "끼록이");
        KkinipopGroupMember groupMember = createGroupMember(100L, group, member);
        KkinipopMission mission = createMission(20L, group, "실시간 미션",
                LocalDateTime.of(2026, 4, 24, 9, 0),
                LocalDateTime.of(2026, 4, 24, 9, 10));
        KkinipopPost post = createPost(30L, group, member, mission, LocalDate.of(2026, 4, 24), "uuid_post");

        ReflectionTestUtils.setField(post, "createdAt", LocalDateTime.of(2026, 4, 24, 9, 5));

        given(kkinipopUseCase.findGroupMember(10L, 1L)).willReturn(groupMember);
        given(dateTimeProvider.today()).willReturn(LocalDate.of(2026, 4, 24));
        given(postRepository.findPostsInDateRange(10L, LocalDate.of(2026, 4, 20), LocalDate.of(2026, 4, 26), null))
                .willReturn(List.of(post));
        given(reactionRepository.findPostReactions(List.of(30L))).willReturn(List.of());

        // When
        List<KkinipopDailyPostResponse> responses = kkinipopPostService.getPosts(1L, 10L, null, null);

        // Then
        // Banned reaction filtering is enforced by the repository query; this unit test only simulates the query result.
        assertThat(responses.get(2).date()).isEqualTo(LocalDate.of(2026, 4, 24));
        assertThat(responses.get(2).posts()).hasSize(1);
        assertThat(responses.get(2).posts().get(0).reactions()).isEmpty();
    }

    @Test
    @DisplayName("미션 ID를 전달하면 해당 미션 게시글만 조회한다")
    void shouldReturnMissionPostsOnly_whenMissionIdIsProvided() {
        // Given
        KkinipopGroup group = createGroup(10L, "아침 챌린저스");
        Member member = createMember(1L, "끼록이");
        KkinipopGroupMember groupMember = createGroupMember(100L, group, member);
        KkinipopMission mission = createMission(20L, group, "실시간 미션",
                LocalDateTime.of(2026, 4, 24, 9, 0),
                LocalDateTime.of(2026, 4, 24, 9, 10));
        KkinipopPost post = createPost(30L, group, member, mission, LocalDate.of(2026, 4, 24), "uuid_post");

        given(kkinipopUseCase.findGroupMember(10L, 1L)).willReturn(groupMember);
        given(dateTimeProvider.today()).willReturn(LocalDate.of(2026, 4, 24));
        given(missionRepository.findTodayMission(
                10L,
                20L,
                LocalDate.of(2026, 4, 24).atStartOfDay(),
                LocalDate.of(2026, 4, 25).atStartOfDay()
        )).willReturn(java.util.Optional.of(mission));
        given(postRepository.findPostsInDateRange(10L, LocalDate.of(2026, 4, 20), LocalDate.of(2026, 4, 26), 20L))
                .willReturn(List.of(post));
        given(reactionRepository.findPostReactions(List.of(30L))).willReturn(List.of());

        // When
        List<KkinipopDailyPostResponse> responses = kkinipopPostService.getPosts(1L, 10L, null, 20L);

        // Then
        assertThat(responses.get(2).posts()).hasSize(1);
        assertThat(responses.get(2).posts().get(0).missionId()).isEqualTo(20L);
    }

    @Test
    @DisplayName("오늘 날짜 미션이 아닌 missionId로는 게시글을 조회할 수 없다")
    void shouldThrowNotFoundException_whenMissionIdIsNotTodayMission() {
        // Given
        KkinipopGroup group = createGroup(10L, "아침 챌린저스");
        Member member = createMember(1L, "끼록이");
        KkinipopGroupMember groupMember = createGroupMember(100L, group, member);

        given(kkinipopUseCase.findGroupMember(10L, 1L)).willReturn(groupMember);
        given(dateTimeProvider.today()).willReturn(LocalDate.of(2026, 4, 24));
        given(missionRepository.findTodayMission(
                10L,
                20L,
                LocalDate.of(2026, 4, 24).atStartOfDay(),
                LocalDate.of(2026, 4, 25).atStartOfDay()
        )).willReturn(java.util.Optional.empty());

        // When, Then
        assertThatThrownBy(() -> kkinipopPostService.getPosts(1L, 10L, null, 20L))
                .isInstanceOf(NotFoundException.class)
                .extracting("baseErrorCode")
                .isEqualTo(KkinipopErrorCode.MISSION_NOT_FOUND);
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
        KkinipopMission mission = KkinipopMission.builder()
                .group(group)
                .title(title)
                .startAt(startAt)
                .endAt(endAt)
                .build();
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
                .saveToPersonalLog(false)
                .build();
        ReflectionTestUtils.setField(post, "id", postId);
        return post;
    }

    private KkinipopCustomEmoji createCustomEmoji(
            Long customEmojiId,
            KkinipopGroup group,
            Member member,
            String label,
            String imageKey
    ) {
        KkinipopCustomEmoji customEmoji = KkinipopCustomEmoji.builder()
                .group(group)
                .creator(member)
                .label(label)
                .imageKey(imageKey)
                .build();
        ReflectionTestUtils.setField(customEmoji, "id", customEmojiId);
        return customEmoji;
    }

    private KkinipopReaction createDefaultReaction(KkinipopPost post, Member member, KkinipopReactionEmoji emoji) {
        return KkinipopReaction.createDefault(post, member, emoji);
    }
}
