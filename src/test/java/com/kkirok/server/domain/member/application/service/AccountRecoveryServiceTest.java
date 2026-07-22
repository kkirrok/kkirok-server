package com.kkirok.server.domain.member.application.service;

import com.kkirok.server.domain.member.application.dto.request.FindEmailRequest;
import com.kkirok.server.domain.member.application.dto.request.ResetPasswordRequest;
import com.kkirok.server.domain.member.dao.AuthIdentityRepository;
import com.kkirok.server.domain.member.dao.MemberRepository;
import com.kkirok.server.domain.member.domain.AuthIdentity;
import com.kkirok.server.domain.member.domain.AuthProvider;
import com.kkirok.server.domain.member.domain.Member;
import com.kkirok.server.domain.member.exception.EmailErrorCode;
import com.kkirok.server.domain.member.exception.EmailException;
import com.kkirok.server.domain.member.exception.MemberErrorCode;
import com.kkirok.server.global.common.exception.ForbiddenException;
import com.kkirok.server.global.common.exception.NotFoundException;
import com.kkirok.server.support.fixture.AuthIdentityFixture;
import com.kkirok.server.support.fixture.FindEmailRequestFixture;
import com.kkirok.server.support.fixture.MemberFixture;
import com.kkirok.server.support.fixture.ProfileSettingRequestFixture;
import com.kkirok.server.support.fixture.ResetPasswordRequestFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class AccountRecoveryServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private AuthIdentityRepository authIdentityRepository;

    @Mock
    private EmailVerificationStateService emailVerificationStateService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AccountRecoveryService accountRecoveryService;

    @Test
    @DisplayName("이름과 생년월일과 전화번호가 일치하면 가입된 이메일을 찾을 수 있다")
    void shouldReturnEmail_whenMemberInfoMatches() {
        // Given
        FindEmailRequest request = FindEmailRequestFixture.create();
        Member member = createProfiledLocalMember();

        given(memberRepository.findByNameAndBirthdayAndPhone(request.name(), request.birth(), request.phone()))
                .willReturn(Optional.of(member));

        // When
        String email = accountRecoveryService.findEmail(request);

        // Then
        assertThat(email).isEqualTo(member.getEmail());
    }

    @Test
    @DisplayName("일치하는 회원 정보가 없으면 이메일을 찾을 수 없다")
    void shouldThrowNotFoundException_whenNoMemberMatchesForFindEmail() {
        // Given
        FindEmailRequest request = FindEmailRequestFixture.create();

        given(memberRepository.findByNameAndBirthdayAndPhone(request.name(), request.birth(), request.phone()))
                .willReturn(Optional.empty());

        // When, Then
        assertThatThrownBy(() -> accountRecoveryService.findEmail(request))
                .isInstanceOf(NotFoundException.class)
                .extracting("baseErrorCode")
                .isEqualTo(MemberErrorCode.ACCOUNT_RECOVERY_INFO_MISMATCH);
    }

    @Test
    @DisplayName("이메일 인증이 완료되고 이름이 일치하면 비밀번호를 재설정할 수 있다")
    void shouldResetPassword_whenEmailVerifiedAndNameMatches() {
        // Given
        ResetPasswordRequest request = ResetPasswordRequestFixture.create();
        Member member = createProfiledLocalMember();
        AuthIdentity authIdentity = AuthIdentityFixture.createLocal(member, request.email(), "old-password");

        ReflectionTestUtils.setField(member, "id", 1L);
        given(authIdentityRepository.findByProviderAndProviderUserId(AuthProvider.LOCAL, request.email()))
                .willReturn(Optional.of(authIdentity));
        given(passwordEncoder.encode(request.newPassword())).willReturn("encoded-new-password");

        // When
        accountRecoveryService.resetPassword(request);

        // Then
        then(emailVerificationStateService).should().consumeVerifiedEmail(request.email());
        then(passwordEncoder).should().encode(request.newPassword());
        assertThat(authIdentity.getPasswordHash()).isEqualTo("encoded-new-password");
    }

    @Test
    @DisplayName("이메일 인증이 완료되지 않으면 비밀번호를 재설정할 수 없다")
    void shouldThrowEmailException_whenEmailIsNotVerifiedForPasswordReset() {
        // Given
        ResetPasswordRequest request = ResetPasswordRequestFixture.create();
        BDDMockito.willThrow(new EmailException(EmailErrorCode.EMAIL_NOT_VERIFIED))
                .given(emailVerificationStateService)
                .consumeVerifiedEmail(request.email());

        // When, Then
        assertThatThrownBy(() -> accountRecoveryService.resetPassword(request))
                .isInstanceOf(EmailException.class)
                .extracting("baseErrorCode")
                .isEqualTo(EmailErrorCode.EMAIL_NOT_VERIFIED);
    }

    @Test
    @DisplayName("이메일과 이름이 일치하지 않으면 비밀번호를 재설정할 수 없다")
    void shouldThrowNotFoundException_whenResetPasswordInfoDoesNotMatch() {
        // Given
        ResetPasswordRequest request = new ResetPasswordRequest("kkirok@test.com", "다른이름", "newPassword123!");
        Member member = createProfiledLocalMember();
        AuthIdentity authIdentity = AuthIdentityFixture.createLocal(member, request.email(), "old-password");

        ReflectionTestUtils.setField(member, "id", 1L);
        given(authIdentityRepository.findByProviderAndProviderUserId(AuthProvider.LOCAL, request.email()))
                .willReturn(Optional.of(authIdentity));

        // When, Then
        assertThatThrownBy(() -> accountRecoveryService.resetPassword(request))
                .isInstanceOf(NotFoundException.class)
                .extracting("baseErrorCode")
                .isEqualTo(MemberErrorCode.ACCOUNT_RECOVERY_INFO_MISMATCH);
    }

    private Member createProfiledLocalMember() {
        Member member = MemberFixture.createLocalMember();
        member.updateOnboarding(ProfileSettingRequestFixture.create(), null);
        return member;
    }
}
