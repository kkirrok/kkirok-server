package com.kkirok.server.domain.member.application.service;

import com.kkirok.server.domain.member.application.dto.response.AccessTokenGenerateResponse;
import com.kkirok.server.domain.member.application.dto.response.LoginSuccessResponse;
import com.kkirok.server.domain.member.application.usecase.MemberUseCase;
import com.kkirok.server.domain.member.domain.Member;
import com.kkirok.server.domain.user.domain.Role;
import com.kkirok.server.domain.user.domain.Users;
import com.kkirok.server.global.auth.jwt.application.TokenService;
import com.kkirok.server.global.auth.jwt.exception.TokenErrorCode;
import com.kkirok.server.global.auth.jwt.provider.JwtTokenProvider;
import com.kkirok.server.global.auth.jwt.provider.JwtValidationType;
import com.kkirok.server.global.auth.security.AdminAuthentication;
import com.kkirok.server.global.auth.security.MemberAuthentication;
import com.kkirok.server.global.common.exception.BadRequestException;
import com.kkirok.server.global.common.exception.UnauthorizedException;
import com.kkirok.server.support.fixture.MemberFixture;
import com.kkirok.server.support.fixture.UserFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private TokenService tokenService;

    @Mock
    private MemberUseCase memberUseCase;

    @InjectMocks
    private AuthenticationService authenticationService;

    @Test
    @DisplayName("일반 회원이 로그인하면 회원 권한으로 토큰을 발급한다")
    void shouldReturnLoginSuccessResponseWithMemberAuthentication_whenMemberLogsIn() {
        // Given
        Users user = UserFixture.create(Role.USER);
        Member member = MemberFixture.createLocalMember("kkirok", "kkirok@test.com", user);
        org.springframework.test.util.ReflectionTestUtils.setField(member, "id", 1L);
        org.springframework.test.util.ReflectionTestUtils.setField(member, "nickname", "kkirok");
        ArgumentCaptor<UsernamePasswordAuthenticationToken> authCaptor =
                ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);

        given(jwtTokenProvider.issueRefreshToken(any(UsernamePasswordAuthenticationToken.class)))
                .willReturn("refresh-token");
        given(jwtTokenProvider.issueAccessToken(any(UsernamePasswordAuthenticationToken.class)))
                .willReturn("access-token");

        // When
        LoginSuccessResponse response = authenticationService.generateLoginSuccessResponse(member);

        // Then
        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isEqualTo("refresh-token");
        assertThat(response.nickname()).isEqualTo("kkirok");
        assertThat(response.role()).isEqualTo(Role.USER.getRoleName());

        then(jwtTokenProvider).should().issueRefreshToken(authCaptor.capture());
        assertThat(authCaptor.getValue()).isInstanceOf(MemberAuthentication.class);
        assertThat(authCaptor.getValue().getPrincipal()).isEqualTo(1L);
        then(tokenService).should().saveRefreshToken(1L, "refresh-token");
    }

    @Test
    @DisplayName("관리자가 로그인하면 관리자 권한으로 토큰을 발급한다")
    void shouldReturnLoginSuccessResponseWithAdminAuthentication_whenAdminLogsIn() {
        // Given
        Users admin = UserFixture.create(Role.ADMIN);
        Member member = MemberFixture.createLocalMember("admin", "admin@test.com", admin);
        org.springframework.test.util.ReflectionTestUtils.setField(member, "id", 99L);
        org.springframework.test.util.ReflectionTestUtils.setField(member, "nickname", "admin");
        ArgumentCaptor<UsernamePasswordAuthenticationToken> authCaptor =
                ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);

        given(jwtTokenProvider.issueRefreshToken(any(UsernamePasswordAuthenticationToken.class)))
                .willReturn("refresh-token");
        given(jwtTokenProvider.issueAccessToken(any(UsernamePasswordAuthenticationToken.class)))
                .willReturn("access-token");

        // When
        LoginSuccessResponse response = authenticationService.generateLoginSuccessResponse(member);

        // Then
        assertThat(response.role()).isEqualTo(Role.ADMIN.getRoleName());

        then(jwtTokenProvider).should().issueRefreshToken(authCaptor.capture());
        assertThat(authCaptor.getValue()).isInstanceOf(AdminAuthentication.class);
        assertThat(authCaptor.getValue().getPrincipal()).isEqualTo(99L);
    }

    @Test
    @DisplayName("유효한 리프레시 토큰이 전달되면 새로운 액세스 토큰을 발급한다")
    void shouldGenerateAccessToken_whenRefreshTokenIsValid() {
        // Given
        ArgumentCaptor<UsernamePasswordAuthenticationToken> authCaptor =
                ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);

        given(jwtTokenProvider.validateToken("refresh-token")).willReturn(JwtValidationType.VALID_JWT);
        given(jwtTokenProvider.getMemberIdFromJwt("refresh-token")).willReturn(1L);
        given(jwtTokenProvider.getRoleFromJwt("refresh-token")).willReturn(Role.USER);
        given(tokenService.findIdByRefreshToken("refresh-token")).willReturn(1L);
        given(jwtTokenProvider.issueAccessToken(any(UsernamePasswordAuthenticationToken.class)))
                .willReturn("new-access-token");

        // When
        AccessTokenGenerateResponse response =
                authenticationService.generateAccessTokenFromRefreshToken("refresh-token");

        // Then
        assertThat(response.accessToken()).isEqualTo("new-access-token");
        then(jwtTokenProvider).should().issueAccessToken(authCaptor.capture());
        assertThat(authCaptor.getValue()).isInstanceOf(MemberAuthentication.class);
        assertThat(authCaptor.getValue().getPrincipal()).isEqualTo(1L);
    }

    @Test
    @DisplayName("만료된 리프레시 토큰으로는 액세스 토큰을 재발급할 수 없다")
    void shouldThrowUnauthorizedException_whenRefreshTokenIsExpired() {
        // Given
        given(jwtTokenProvider.validateToken("refresh-token")).willReturn(JwtValidationType.EXPIRED_JWT_TOKEN);

        // When, Then
        assertThatThrownBy(() -> authenticationService.generateAccessTokenFromRefreshToken("refresh-token"))
                .isInstanceOf(UnauthorizedException.class)
                .extracting("baseErrorCode")
                .isEqualTo(TokenErrorCode.REFRESH_TOKEN_EXPIRED_ERROR);
    }

    @Test
    @DisplayName("형식이 잘못된 리프레시 토큰으로는 액세스 토큰을 재발급할 수 없다")
    void shouldThrowBadRequestException_whenRefreshTokenIsInvalid() {
        // Given
        given(jwtTokenProvider.validateToken("refresh-token")).willReturn(JwtValidationType.INVALID_JWT_TOKEN);

        // When, Then
        assertThatThrownBy(() -> authenticationService.generateAccessTokenFromRefreshToken("refresh-token"))
                .isInstanceOf(BadRequestException.class)
                .extracting("baseErrorCode")
                .isEqualTo(TokenErrorCode.INVALID_REFRESH_TOKEN_ERROR);
    }

    @Test
    @DisplayName("서명이 올바르지 않은 리프레시 토큰으로는 액세스 토큰을 재발급할 수 없다")
    void shouldThrowBadRequestException_whenRefreshTokenSignatureIsInvalid() {
        // Given
        given(jwtTokenProvider.validateToken("refresh-token")).willReturn(JwtValidationType.INVALID_JWT_SIGNATURE);

        // When, Then
        assertThatThrownBy(() -> authenticationService.generateAccessTokenFromRefreshToken("refresh-token"))
                .isInstanceOf(BadRequestException.class)
                .extracting("baseErrorCode")
                .isEqualTo(TokenErrorCode.REFRESH_TOKEN_SIGNATURE_ERROR);
    }

    @Test
    @DisplayName("지원하지 않는 형식의 리프레시 토큰으로는 액세스 토큰을 재발급할 수 없다")
    void shouldThrowBadRequestException_whenRefreshTokenIsUnsupported() {
        // Given
        given(jwtTokenProvider.validateToken("refresh-token")).willReturn(JwtValidationType.UNSUPPORTED_JWT_TOKEN);

        // When, Then
        assertThatThrownBy(() -> authenticationService.generateAccessTokenFromRefreshToken("refresh-token"))
                .isInstanceOf(BadRequestException.class)
                .extracting("baseErrorCode")
                .isEqualTo(TokenErrorCode.UNSUPPORTED_REFRESH_TOKEN_ERROR);
    }

    @Test
    @DisplayName("비어 있는 리프레시 토큰으로는 액세스 토큰을 재발급할 수 없다")
    void shouldThrowBadRequestException_whenRefreshTokenIsEmpty() {
        // Given
        given(jwtTokenProvider.validateToken("refresh-token")).willReturn(JwtValidationType.EMPTY_JWT);

        // When, Then
        assertThatThrownBy(() -> authenticationService.generateAccessTokenFromRefreshToken("refresh-token"))
                .isInstanceOf(BadRequestException.class)
                .extracting("baseErrorCode")
                .isEqualTo(TokenErrorCode.REFRESH_TOKEN_EMPTY_ERROR);
    }

    @Test
    @DisplayName("저장된 사용자 정보와 일치하지 않는 리프레시 토큰으로는 액세스 토큰을 재발급할 수 없다")
    void shouldThrowBadRequestException_whenStoredMemberIdDoesNotMatch() {
        // Given
        given(jwtTokenProvider.validateToken("refresh-token")).willReturn(JwtValidationType.VALID_JWT);
        given(jwtTokenProvider.getMemberIdFromJwt("refresh-token")).willReturn(1L);
        given(tokenService.findIdByRefreshToken("refresh-token")).willReturn(2L);

        // When, Then
        assertThatThrownBy(() -> authenticationService.generateAccessTokenFromRefreshToken("refresh-token"))
                .isInstanceOf(BadRequestException.class)
                .extracting("baseErrorCode")
                .isEqualTo(TokenErrorCode.REFRESH_TOKEN_MEMBER_ID_MISMATCH_ERROR);
    }
}
