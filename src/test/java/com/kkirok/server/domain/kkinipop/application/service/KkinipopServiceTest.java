package com.kkirok.server.domain.kkinipop.application.service;

import com.kkirok.server.domain.kkinipop.dao.KkinipopCustomEmojiRepository;
import com.kkirok.server.domain.kkinipop.dao.KkinipopGroupMemberRepository;
import com.kkirok.server.domain.kkinipop.dao.KkinipopGroupRepository;
import com.kkirok.server.domain.kkinipop.dao.KkinipopPostRepository;
import com.kkirok.server.domain.kkinipop.exception.KkinipopErrorCode;
import com.kkirok.server.global.common.exception.ForbiddenException;
import com.kkirok.server.global.common.exception.NotFoundException;
import com.kkirok.server.global.common.util.DateTimeProvider;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class KkinipopServiceTest {

    @Mock
    private KkinipopGroupMemberRepository groupMemberRepository;

    @Mock
    private KkinipopPostRepository postRepository;

    @Mock
    private KkinipopCustomEmojiRepository customEmojiRepository;

    @Mock
    private KkinipopGroupRepository groupRepository;

    @Mock
    private DateTimeProvider dateTimeProvider;

    @InjectMocks
    private KkinipopService kkinipopService;

    @Test
    @DisplayName("추방된 멤버가 그룹 API를 호출하면 403을 반환한다")
    void shouldThrowForbiddenException_whenBannedMemberCallsGroupApi() {
        // Given
        given(groupMemberRepository.findActiveMembership(10L, 1L)).willReturn(Optional.empty());
        given(groupMemberRepository.existsBannedMembership(10L, 1L)).willReturn(true);

        // When, Then
        assertThatThrownBy(() -> kkinipopService.findGroupMember(10L, 1L))
                .isInstanceOf(ForbiddenException.class)
                .extracting("baseErrorCode")
                .isEqualTo(KkinipopErrorCode.GROUP_BANNED);
    }

    @Test
    @DisplayName("그룹에 속하지 않은 멤버가 그룹 API를 호출하면 기존처럼 404를 반환한다")
    void shouldThrowNotFoundException_whenNonMemberCallsGroupApi() {
        // Given
        given(groupMemberRepository.findActiveMembership(10L, 1L)).willReturn(Optional.empty());
        given(groupMemberRepository.existsBannedMembership(10L, 1L)).willReturn(false);

        // When, Then
        assertThatThrownBy(() -> kkinipopService.findGroupMember(10L, 1L))
                .isInstanceOf(NotFoundException.class)
                .extracting("baseErrorCode")
                .isEqualTo(KkinipopErrorCode.GROUP_MEMBER_NOT_FOUND);
    }
}
