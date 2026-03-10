package com.kkirok.server.domain.member.application.service;

import com.kkirok.server.domain.member.dao.MemberRepository;
import com.kkirok.server.domain.member.domain.Member;
import com.kkirok.server.domain.member.domain.SocialType;
import com.kkirok.server.domain.member.exception.MemberErrorCode;
import com.kkirok.server.domain.user.dao.UserRepository;
import com.kkirok.server.domain.user.domain.Users;
import com.kkirok.server.global.common.exception.NotFoundException;
import com.kkirok.server.support.fixture.MemberFixture;
import com.kkirok.server.support.fixture.UserFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private MemberService memberService;

    @Test
    @DisplayName("존재하는 회원 ID로 조회하면 회원 정보를 반환한다")
    void shouldReturnMember_whenMemberIdExists() {
        // Given
        Member member = MemberFixture.createLocalMember();

        given(memberRepository.findById(1L)).willReturn(Optional.of(member));

        // When
        Member foundMember = memberService.findMemberByMemberId(1L);

        // Then
        assertThat(foundMember).isSameAs(member);
    }

    @Test
    @DisplayName("존재하지 않는 회원 ID로는 회원 정보를 조회할 수 없다")
    void shouldThrowNotFoundException_whenMemberIdDoesNotExist() {
        // Given
        given(memberRepository.findById(1L)).willReturn(Optional.empty());

        // When, Then
        assertThatThrownBy(() -> memberService.findMemberByMemberId(1L))
                .isInstanceOf(NotFoundException.class)
                .extracting("baseErrorCode")
                .isEqualTo(MemberErrorCode.MEMBER_NOT_FOUND);
    }

    @Test
    @DisplayName("소셜 식별자와 소셜 타입이 일치하면 회원이 존재한다고 판단한다")
    void shouldReturnTrue_whenMemberExistsBySocialIdAndSocialType() {
        // Given
        Member member = MemberFixture.createSocialMember(1001L, SocialType.KAKAO);

        given(memberRepository.findBySocialTypeAndSocialId(1001L, SocialType.KAKAO))
                .willReturn(Optional.of(member));

        // When
        boolean exists = memberService.checkMemberExistsBySocialIdAndSocialType(1001L, SocialType.KAKAO);

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("소셜 식별자와 소셜 타입이 일치하면 해당 회원을 조회할 수 있다")
    void shouldReturnMember_whenSocialMemberExists() {
        // Given
        Member member = MemberFixture.createSocialMember(1001L, SocialType.KAKAO);

        given(memberRepository.findBySocialTypeAndSocialId(1001L, SocialType.KAKAO))
                .willReturn(Optional.of(member));

        // When
        Member foundMember = memberService.findMemberBySocialIdAndSocialType(1001L, SocialType.KAKAO);

        // Then
        assertThat(foundMember).isSameAs(member);
    }

    @Test
    @DisplayName("소셜 식별자와 소셜 타입이 일치하는 회원이 없으면 조회할 수 없다")
    void shouldThrowNotFoundException_whenSocialMemberDoesNotExist() {
        // Given
        given(memberRepository.findBySocialTypeAndSocialId(1001L, SocialType.KAKAO))
                .willReturn(Optional.empty());

        // When, Then
        assertThatThrownBy(() -> memberService.findMemberBySocialIdAndSocialType(1001L, SocialType.KAKAO))
                .isInstanceOf(NotFoundException.class)
                .extracting("baseErrorCode")
                .isEqualTo(MemberErrorCode.MEMBER_NOT_FOUND);
    }

    @Test
    @DisplayName("존재하는 사용자 ID를 전달하면 사용자를 삭제한다")
    void shouldDeleteUser_whenUserExists() {
        // Given
        Users user = UserFixture.create();

        given(userRepository.findById(1L)).willReturn(Optional.of(user));

        // When
        memberService.deleteUser(1L);

        // Then
        then(userRepository).should().delete(user);
    }

    @Test
    @DisplayName("존재하지 않는 사용자 ID로는 사용자를 삭제할 수 없다")
    void shouldThrowNotFoundException_whenDeleteTargetUserDoesNotExist() {
        // Given
        given(userRepository.findById(1L)).willReturn(Optional.empty());

        // When, Then
        assertThatThrownBy(() -> memberService.deleteUser(1L))
                .isInstanceOf(NotFoundException.class)
                .extracting("baseErrorCode")
                .isEqualTo(MemberErrorCode.MEMBER_NOT_FOUND);
    }

    @Test
    @DisplayName("회원 수를 조회하면 저장된 회원 수를 반환한다")
    void shouldReturnMemberCount_whenCountingMembers() {
        // Given
        given(memberRepository.count()).willReturn(7L);

        // When
        long count = memberService.countMembers();

        // Then
        assertThat(count).isEqualTo(7L);
    }
}
