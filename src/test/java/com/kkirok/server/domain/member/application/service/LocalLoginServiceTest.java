package com.kkirok.server.domain.member.application.service;

import com.kkirok.server.domain.member.application.dto.request.LocalLoginRequest;
import com.kkirok.server.domain.member.application.dto.request.LocalSignUpRequest;
import com.kkirok.server.domain.member.application.dto.response.LoginSuccessResponse;
import com.kkirok.server.domain.member.dao.AuthIdentityRepository;
import com.kkirok.server.domain.member.domain.AuthIdentity;
import com.kkirok.server.domain.member.domain.AuthProvider;
import com.kkirok.server.domain.member.domain.Member;
import com.kkirok.server.domain.member.domain.SocialType;
import com.kkirok.server.domain.member.exception.EmailErrorCode;
import com.kkirok.server.domain.member.exception.EmailException;
import com.kkirok.server.domain.member.exception.MemberErrorCode;
import com.kkirok.server.global.common.exception.ConflictException;
import com.kkirok.server.global.common.exception.ForbiddenException;
import com.kkirok.server.global.common.exception.NotFoundException;
import com.kkirok.server.global.common.exception.UnauthorizedException;
import com.kkirok.server.support.fixture.AuthIdentityFixture;
import com.kkirok.server.support.fixture.LocalLoginRequestFixture;
import com.kkirok.server.support.fixture.LocalSignUpRequestFixture;
import com.kkirok.server.support.fixture.MemberFixture;
import com.kkirok.server.support.fixture.UserFixture;
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
import static com.kkirok.server.domain.user.domain.Role.ADMIN;

@ExtendWith(MockitoExtension.class)
class LocalLoginServiceTest {

    @Mock
    private AuthIdentityRepository authIdentityRepository;

    @Mock
    private MemberRegistrationService memberRegistrationService;

    @Mock
    private AuthenticationService authenticationService;

    @Mock
    private EmailVerificationStateService emailVerificationStateService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private LocalLoginService localLoginService;

    @Test
    @DisplayName("로컬 회원가입 정보가 올바르면 회원가입 후 로그인 응답을 생성한다")
    void shouldReturnLoginSuccessResponse_whenLocalSignUpSucceeds() {
        // Given
        LocalSignUpRequest request = LocalSignUpRequestFixture.create();
        Member member = MemberFixture.createLocalMember("unused", request.email());
        ReflectionTestUtils.setField(member, "id", 1L);
        LoginSuccessResponse expected = LoginSuccessResponse.of("access-token", "refresh-token", "kkirok",
                "ROLE_MEMBER");

        given(authIdentityRepository.existsByProviderAndProviderUserId(AuthProvider.LOCAL, request.email()))
                .willReturn(false);
        given(passwordEncoder.encode(request.password())).willReturn("encoded-password");
        given(memberRegistrationService.registerLocalMember(request.email(), "encoded-password"))
                .willReturn(member);
        given(authenticationService.generateLoginSuccessResponse(member.getId(), member.getUser(), member.getNickname()))
                .willReturn(expected);

        // When
        LoginSuccessResponse response = localLoginService.signUp(request);

        // Then
        assertThat(response).isEqualTo(expected);
        then(emailVerificationStateService).should().consumeVerifiedEmail(request.email());
        then(passwordEncoder).should().encode(request.password());
        then(memberRegistrationService).should()
                .registerLocalMember(request.email(), "encoded-password");
        then(authenticationService).should()
                .generateLoginSuccessResponse(member.getId(), member.getUser(), member.getNickname());
    }

    @Test
    @DisplayName("이미 가입된 이메일로는 로컬 회원가입할 수 없다")
    void shouldThrowConflictException_whenLocalEmailAlreadyExists() {
        // Given
        LocalSignUpRequest request = LocalSignUpRequestFixture.create("already-exists@test.com",
                "password123!");

        given(authIdentityRepository.existsByProviderAndProviderUserId(AuthProvider.LOCAL, request.email()))
                .willReturn(true);

        // When, Then
        assertThatThrownBy(() -> localLoginService.signUp(request))
                .isInstanceOf(ConflictException.class)
                .extracting("baseErrorCode")
                .isEqualTo(MemberErrorCode.LOCAL_EMAIL_ALREADY_EXISTS);
    }

    @Test
    @DisplayName("이메일 인증이 완료되지 않으면 로컬 회원가입할 수 없다")
    void shouldThrowBadRequestException_whenEmailIsNotVerified() {
        // Given
        LocalSignUpRequest request = LocalSignUpRequestFixture.create();
        given(authIdentityRepository.existsByProviderAndProviderUserId(AuthProvider.LOCAL, request.email()))
                .willReturn(false);
        BDDMockito.willThrow(new EmailException(EmailErrorCode.EMAIL_NOT_VERIFIED))
                .given(emailVerificationStateService)
                .consumeVerifiedEmail(request.email());

        // When, Then
        assertThatThrownBy(() -> localLoginService.signUp(request))
                .isInstanceOf(EmailException.class)
                .extracting("baseErrorCode")
                .isEqualTo(EmailErrorCode.EMAIL_NOT_VERIFIED);
    }

    @Test
    @DisplayName("올바른 로컬 로그인 정보가 전달되면 로그인에 성공한다")
    void shouldReturnLoginSuccessResponse_whenLocalLoginSucceeds() {
        // Given
        LocalLoginRequest request = LocalLoginRequestFixture.create();
        Member member = MemberFixture.createLocalMember("kkirok", request.email());
        ReflectionTestUtils.setField(member, "id", 1L);
        AuthIdentity authIdentity = AuthIdentityFixture.createLocal(member, request.email(), "encoded-password");
        LoginSuccessResponse expected = LoginSuccessResponse.of("access-token", "refresh-token", "kkirok",
                "ROLE_MEMBER");

        given(authIdentityRepository.findByProviderAndProviderUserId(AuthProvider.LOCAL, request.email()))
                .willReturn(Optional.of(authIdentity));
        given(passwordEncoder.matches(request.password(), "encoded-password")).willReturn(true);
        given(authenticationService.generateLoginSuccessResponse(member.getId(), member.getUser(), member.getNickname()))
                .willReturn(expected);

        // When
        LoginSuccessResponse response = localLoginService.login(request);

        // Then
        assertThat(response).isEqualTo(expected);
        then(passwordEncoder).should().matches(request.password(), "encoded-password");
        then(authenticationService).should()
                .generateLoginSuccessResponse(member.getId(), member.getUser(), member.getNickname());
    }

    @Test
    @DisplayName("가입되지 않은 이메일로는 로컬 로그인할 수 없다")
    void shouldThrowNotFoundException_whenLocalAccountDoesNotExist() {
        // Given
        LocalLoginRequest request = LocalLoginRequestFixture.create();

        given(authIdentityRepository.findByProviderAndProviderUserId(AuthProvider.LOCAL, request.email()))
                .willReturn(Optional.empty());
        given(authIdentityRepository.existsByMemberEmailAndProviderNot(request.email(), AuthProvider.LOCAL))
                .willReturn(false);

        // When, Then
        assertNotFoundException(() -> localLoginService.login(request), MemberErrorCode.LOCAL_ACCOUNT_NOT_FOUND);
    }

    @Test
    @DisplayName("소셜 계정 이메일로는 로컬 로그인할 수 없다")
    void shouldThrowForbiddenException_whenSocialAccountAttemptsLocalLogin() {
        // Given
        LocalLoginRequest request = LocalLoginRequestFixture.create();

        given(authIdentityRepository.findByProviderAndProviderUserId(AuthProvider.LOCAL, request.email()))
                .willReturn(Optional.empty());
        given(authIdentityRepository.existsByMemberEmailAndProviderNot(request.email(), AuthProvider.LOCAL))
                .willReturn(true);

        // When, Then
        assertForbiddenException(
                () -> localLoginService.login(request),
                MemberErrorCode.SOCIAL_ACCOUNT_LOCAL_LOGIN_FORBIDDEN
        );
    }

    @Test
    @DisplayName("비밀번호 정보가 없는 계정으로는 로컬 로그인할 수 없다")
    void shouldThrowUnauthorizedException_whenPasswordHashIsMissing() {
        // Given
        LocalLoginRequest request = LocalLoginRequestFixture.create();
        Member member = MemberFixture.createLocalMember("kkirok", request.email());
        AuthIdentity authIdentity = AuthIdentityFixture.createLocal(member, request.email(), null);

        given(authIdentityRepository.findByProviderAndProviderUserId(AuthProvider.LOCAL, request.email()))
                .willReturn(Optional.of(authIdentity));

        // When, Then
        assertUnauthorizedException(
                () -> localLoginService.login(request),
                MemberErrorCode.LOCAL_LOGIN_PASSWORD_MISMATCH
        );
    }

    @Test
    @DisplayName("비밀번호가 일치하지 않으면 로컬 로그인할 수 없다")
    void shouldThrowUnauthorizedException_whenPasswordDoesNotMatch() {
        // Given
        LocalLoginRequest request = LocalLoginRequestFixture.create();
        Member member = MemberFixture.createLocalMember("kkirok", request.email());
        AuthIdentity authIdentity = AuthIdentityFixture.createLocal(member, request.email(), "encoded-password");

        given(authIdentityRepository.findByProviderAndProviderUserId(AuthProvider.LOCAL, request.email()))
                .willReturn(Optional.of(authIdentity));
        given(passwordEncoder.matches(request.password(), "encoded-password")).willReturn(false);

        // When, Then
        assertUnauthorizedException(
                () -> localLoginService.login(request),
                MemberErrorCode.LOCAL_LOGIN_PASSWORD_MISMATCH
        );
    }

    @Test
    @DisplayName("관리자 계정으로 일반 사용자 로그인 API를 호출하면 권한 예외가 발생한다")
    void shouldThrowForbiddenException_whenAdminAccountAttemptsUserLogin() {
        // Given
        LocalLoginRequest request = LocalLoginRequestFixture.create();
        Member adminMember = MemberFixture.createLocalMember("admin", request.email(), UserFixture.create(ADMIN));
        AuthIdentity authIdentity = AuthIdentityFixture.createLocal(adminMember, request.email(), "encoded-password");

        given(authIdentityRepository.findByProviderAndProviderUserId(AuthProvider.LOCAL, request.email()))
                .willReturn(Optional.of(authIdentity));
        given(passwordEncoder.matches(request.password(), "encoded-password")).willReturn(true);

        // When, Then
        assertForbiddenException(
                () -> localLoginService.login(request),
                MemberErrorCode.USER_LOGIN_FOR_ADMIN_ACCOUNT
        );
    }

    @Test
    @DisplayName("일반 사용자 계정으로 관리자 로그인 API를 호출하면 권한 예외가 발생한다")
    void shouldThrowForbiddenException_whenUserAccountAttemptsAdminLogin() {
        // Given
        LocalLoginRequest request = LocalLoginRequestFixture.create();
        Member member = MemberFixture.createLocalMember("kkirok", request.email());
        AuthIdentity authIdentity = AuthIdentityFixture.createLocal(member, request.email(), "encoded-password");

        given(authIdentityRepository.findByProviderAndProviderUserId(AuthProvider.LOCAL, request.email()))
                .willReturn(Optional.of(authIdentity));
        given(passwordEncoder.matches(request.password(), "encoded-password")).willReturn(true);

        // When, Then
        assertForbiddenException(
                () -> localLoginService.adminLogin(request),
                MemberErrorCode.ADMIN_LOGIN_FOR_USER_ACCOUNT
        );
    }

    @Test
    @DisplayName("탈퇴한 계정은 로컬 로그인할 수 없다")
    void shouldThrowConflictException_whenDeletedMemberAttemptsLogin() {
        // Given
        LocalLoginRequest request = LocalLoginRequestFixture.create();
        Member member = MemberFixture.createLocalMember("kkirok", request.email());
        ReflectionTestUtils.setField(member, "deletedAt", java.time.LocalDateTime.now());
        AuthIdentity authIdentity = AuthIdentityFixture.createLocal(member, request.email(), "encoded-password");

        given(authIdentityRepository.findByProviderAndProviderUserId(AuthProvider.LOCAL, request.email()))
                .willReturn(Optional.of(authIdentity));

        // When, Then
        assertThatThrownBy(() -> localLoginService.login(request))
                .isInstanceOf(ConflictException.class)
                .extracting("baseErrorCode")
                .isEqualTo(MemberErrorCode.DELETED_MEMBER);
    }

    private void assertUnauthorizedException(Runnable action, MemberErrorCode errorCode) {
        assertThatThrownBy(action::run)
                .isInstanceOf(UnauthorizedException.class)
                .extracting("baseErrorCode")
                .isEqualTo(errorCode);
    }

    private void assertForbiddenException(Runnable action, MemberErrorCode errorCode) {
        assertThatThrownBy(action::run)
                .isInstanceOf(ForbiddenException.class)
                .extracting("baseErrorCode")
                .isEqualTo(errorCode);
    }

    private void assertNotFoundException(Runnable action, MemberErrorCode errorCode) {
        assertThatThrownBy(action::run)
                .isInstanceOf(NotFoundException.class)
                .extracting("baseErrorCode")
                .isEqualTo(errorCode);
    }
}
