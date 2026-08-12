package com.kkirok.server.domain.kkinipop.application.service;

import com.kkirok.server.domain.kkinipop.application.dto.response.KkinipopCustomEmojiResponse;
import com.kkirok.server.domain.kkinipop.application.dto.response.KkinipopEmojiListResponse;
import com.kkirok.server.domain.kkinipop.application.dto.response.KkinipopSystemEmojiResponse;
import com.kkirok.server.domain.kkinipop.application.usecase.KkinipopUseCase;
import com.kkirok.server.domain.kkinipop.dao.KkinipopCustomEmojiRepository;
import com.kkirok.server.domain.kkinipop.domain.KkinipopCustomEmoji;
import com.kkirok.server.domain.kkinipop.domain.KkinipopGroup;
import com.kkirok.server.domain.kkinipop.domain.KkinipopGroupMember;
import com.kkirok.server.domain.kkinipop.exception.KkinipopErrorCode;
import com.kkirok.server.domain.member.domain.Member;
import com.kkirok.server.global.common.exception.ConflictException;
import com.kkirok.server.global.common.exception.ForbiddenException;
import com.kkirok.server.global.common.util.DateTimeProvider;
import com.kkirok.server.global.external.r2.application.service.R2UploadService;
import com.kkirok.server.global.external.r2.application.service.R2UploadType;
import com.kkirok.server.support.fixture.MemberFixture;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class KkinipopEmojiServiceTest {

    @Mock
    private KkinipopUseCase kkinipopUseCase;

    @Mock
    private KkinipopCustomEmojiRepository customEmojiRepository;

    @Mock
    private R2UploadService r2UploadService;

    @Mock
    private DateTimeProvider dateTimeProvider;

    @InjectMocks
    private KkinipopEmojiService kkinipopEmojiService;

    @Test
    @DisplayName("시스템 이모지 목록을 조회하면 접두사가 포함된 코드를 반환한다")
    void shouldReturnSystemEmojiOptions_whenRequested() {
        // When
        List<KkinipopSystemEmojiResponse> responses = kkinipopEmojiService.getSystemEmojiOptions();

        // Then
        assertThat(responses).isNotEmpty();
        assertThat(responses).allMatch(response -> response.emojiCode().startsWith("SYSTEM_"));
    }

    @Test
    @DisplayName("그룹 커스텀 이모지가 최대 개수에 도달하면 더 생성할 수 없다")
    void shouldThrowConflictException_whenCustomEmojiLimitExceeded() {
        // Given
        KkinipopGroup group = createGroup(10L, "아침 챌린저스");
        Member member = createMember(1L, "끼록이");
        KkinipopGroupMember groupMember = createGroupMember(100L, group, member, true);
        MockMultipartFile image = new MockMultipartFile("image", "chew.png", "image/png", "emoji".getBytes());

        given(kkinipopUseCase.findGroupMember(10L, 1L)).willReturn(groupMember);
        given(customEmojiRepository.countActiveGroupEmojis(10L)).willReturn(2L);

        // When, Then
        assertThatThrownBy(() -> kkinipopEmojiService.createCustomEmoji(1L, 10L, image))
                .isInstanceOf(ConflictException.class)
                .extracting("baseErrorCode")
                .isEqualTo(KkinipopErrorCode.CUSTOM_EMOJI_LIMIT_EXCEEDED);
    }

    @Test
    @DisplayName("커스텀 이모지를 생성하면 업로드된 이미지 키와 라벨을 응답으로 반환한다")
    void shouldCreateCustomEmoji_whenRequestIsValid() {
        // Given
        KkinipopGroup group = createGroup(10L, "아침 챌린저스");
        Member member = createMember(1L, "끼록이");
        KkinipopGroupMember groupMember = createGroupMember(100L, group, member, false);
        MockMultipartFile image = new MockMultipartFile("image", "Chew.png", "image/png", "emoji".getBytes());

        given(kkinipopUseCase.findGroupMember(10L, 1L)).willReturn(groupMember);
        given(customEmojiRepository.countActiveGroupEmojis(10L)).willReturn(1L);
        given(r2UploadService.upload(image, R2UploadType.KKINIPOP_EMOJI)).willReturn("uuid_kkinipopEmojiImage");
        given(customEmojiRepository.save(any(KkinipopCustomEmoji.class))).willAnswer(invocation -> {
            KkinipopCustomEmoji emoji = invocation.getArgument(0);
            ReflectionTestUtils.setField(emoji, "id", 5L);
            return emoji;
        });

        // When
        KkinipopCustomEmojiResponse response = kkinipopEmojiService.createCustomEmoji(1L, 10L, image);

        // Then
        assertThat(response.customEmojiId()).isEqualTo(5L);
        assertThat(response.emojiCode()).isEqualTo("CUSTOM_5");
        assertThat(response.label()).isEqualTo("chew");
        assertThat(response.image()).isEqualTo("uuid_kkinipopEmojiImage");
    }

    @Test
    @DisplayName("작성자도 방장도 아니면 커스텀 이모지를 삭제할 수 없다")
    void shouldThrowForbiddenException_whenDeletingOthersCustomEmojiWithoutLeaderRole() {
        // Given
        KkinipopGroup group = createGroup(10L, "아침 챌린저스");
        Member member = createMember(1L, "끼록이");
        Member creator = createMember(2L, "다른사람");
        KkinipopGroupMember groupMember = createGroupMember(100L, group, member, false);
        KkinipopCustomEmoji customEmoji = createCustomEmoji(5L, group, creator, "chew", "uuid_kkinipopEmojiImage");

        given(kkinipopUseCase.findGroupMember(10L, 1L)).willReturn(groupMember);
        given(kkinipopUseCase.findCustomEmojiById(5L)).willReturn(customEmoji);

        // When, Then
        assertThatThrownBy(() -> kkinipopEmojiService.deleteCustomEmoji(1L, 10L, 5L))
                .isInstanceOf(ForbiddenException.class)
                .extracting("baseErrorCode")
                .isEqualTo(KkinipopErrorCode.CUSTOM_EMOJI_DELETE_FORBIDDEN);
    }

    @Test
    @DisplayName("그룹 이모지를 조회하면 시스템 이모지와 커스텀 이모지를 함께 반환한다")
    void shouldReturnEmojiList_whenGroupMemberRequestsEmojis() {
        // Given
        KkinipopGroup group = createGroup(10L, "아침 챌린저스");
        Member member = createMember(1L, "끼록이");
        KkinipopGroupMember groupMember = createGroupMember(100L, group, member, false);
        KkinipopCustomEmoji customEmoji = createCustomEmoji(5L, group, member, "chew", "uuid_kkinipopEmojiImage");

        given(kkinipopUseCase.findGroupMember(10L, 1L)).willReturn(groupMember);
        given(customEmojiRepository.findActiveGroupEmojis(10L)).willReturn(List.of(customEmoji));

        // When
        KkinipopEmojiListResponse response = kkinipopEmojiService.getEmojis(1L, 10L);

        // Then
        assertThat(response.systemEmojis()).isNotEmpty();
        assertThat(response.customEmojis()).hasSize(1);
        assertThat(response.customEmojis().get(0).image()).isEqualTo("uuid_kkinipopEmojiImage");
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

    private KkinipopGroupMember createGroupMember(Long groupMemberId, KkinipopGroup group, Member member, boolean leader) {
        KkinipopGroupMember groupMember = leader
                ? KkinipopGroupMember.createLeader(group, member)
                : KkinipopGroupMember.createMember(group, member);
        ReflectionTestUtils.setField(groupMember, "id", groupMemberId);
        return groupMember;
    }

    private KkinipopCustomEmoji createCustomEmoji(Long customEmojiId, KkinipopGroup group, Member creator, String label, String imageKey) {
        KkinipopCustomEmoji customEmoji = KkinipopCustomEmoji.builder()
                .group(group)
                .creator(creator)
                .label(label)
                .imageKey(imageKey)
                .build();
        ReflectionTestUtils.setField(customEmoji, "id", customEmojiId);
        return customEmoji;
    }
}
