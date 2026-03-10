package com.kkirok.server.domain.member.application.service;

import com.kkirok.server.domain.member.application.dto.response.LoginSuccessResponse;
import com.kkirok.server.domain.member.application.usecase.MemberUseCase;
import com.kkirok.server.domain.member.dao.AuthIdentityRepository;
import com.kkirok.server.domain.member.domain.AuthIdentity;
import com.kkirok.server.domain.member.domain.AuthProvider;
import com.kkirok.server.domain.member.domain.Member;
import com.kkirok.server.global.auth.client.application.KakaoSocialService;
import com.kkirok.server.global.auth.client.application.NaverSocialService;
import com.kkirok.server.global.auth.client.dto.MemberInfoResponse;
import com.kkirok.server.global.auth.client.dto.MemberLoginRequest;
import com.kkirok.server.support.fixture.AuthIdentityFixture;
import com.kkirok.server.support.fixture.MemberFixture;
import com.kkirok.server.support.fixture.MemberInfoResponseFixture;
import com.kkirok.server.support.fixture.MemberLoginRequestFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class SocialLoginServiceTest {

    @Mock
    private MemberRegistrationService memberRegistrationService;

    @Mock
    private AuthenticationService authenticationService;

    @Mock
    private KakaoSocialService kakaoSocialService;

    @Mock
    private NaverSocialService naverSocialService;

    @Mock
    private MemberUseCase memberUseCase;

    @Mock
    private AuthIdentityRepository authIdentityRepository;

    @InjectMocks
    private SocialLoginService socialLoginService;

    @Test
    @DisplayName("이미 연결된 소셜 인증 수단이 있으면 기존 회원으로 로그인한다")
    void shouldLoginExistingMember_whenAuthIdentityAlreadyExists() {
        // Given
        String authorizationCode = "auth-code";
        MemberLoginRequest loginRequest = MemberLoginRequestFixture.createKakao();
        MemberInfoResponse memberInfoResponse = MemberInfoResponseFixture.createKakaoMember();
        Member member = MemberFixture.createSocialMember("kkirok", "kkirok@test.com",
                MemberFixture.createLocalMember().getUser(), 1001L, memberInfoResponse.socialType());
        ReflectionTestUtils.setField(member, "id", 1L);
        AuthIdentity authIdentity = AuthIdentityFixture.createSocial(member, AuthProvider.KAKAO,
                memberInfoResponse.providerUserId());
        LoginSuccessResponse expected = LoginSuccessResponse.of("access-token", "refresh-token", "kkirok",
                "ROLE_MEMBER");

        given(kakaoSocialService.login(authorizationCode, loginRequest)).willReturn(memberInfoResponse);
        given(authIdentityRepository.findByProviderAndProviderUserId(AuthProvider.KAKAO, memberInfoResponse.providerUserId()))
                .willReturn(Optional.of(authIdentity));
        given(memberUseCase.findMemberByMemberId(1L)).willReturn(member);
        given(authenticationService.generateLoginSuccessResponse(1L, member.getUser(), memberInfoResponse))
                .willReturn(expected);

        // When
        LoginSuccessResponse response = socialLoginService.handleSocialLogin(authorizationCode, loginRequest);

        // Then
        assertThat(response).isEqualTo(expected);
        then(memberRegistrationService).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("기존 소셜 회원에 인증 수단이 없으면 인증 수단을 연결한 뒤 로그인한다")
    void shouldCreateAuthIdentityForExistingMember_whenSocialMemberExistsWithoutIdentity() {
        // Given
        String authorizationCode = "auth-code";
        MemberLoginRequest loginRequest = MemberLoginRequestFixture.createNaver();
        MemberInfoResponse memberInfoResponse = MemberInfoResponseFixture.createNaverMember();
        Member member = MemberFixture.createSocialMember(2002L, memberInfoResponse.socialType());
        ReflectionTestUtils.setField(member, "id", 2L);
        LoginSuccessResponse expected = LoginSuccessResponse.of("access-token", "refresh-token", "kkirok",
                "ROLE_MEMBER");

        given(naverSocialService.login(authorizationCode, loginRequest)).willReturn(memberInfoResponse);
        given(authIdentityRepository.findByProviderAndProviderUserId(AuthProvider.NAVER, memberInfoResponse.providerUserId()))
                .willReturn(Optional.empty());
        given(memberUseCase.checkMemberExistsBySocialIdAndSocialType(2002L, memberInfoResponse.socialType()))
                .willReturn(true);
        given(memberUseCase.findMemberBySocialIdAndSocialType(2002L, memberInfoResponse.socialType()))
                .willReturn(member);
        given(memberUseCase.findMemberByMemberId(2L)).willReturn(member);
        given(authenticationService.generateLoginSuccessResponse(2L, member.getUser(), memberInfoResponse))
                .willReturn(expected);

        // When
        LoginSuccessResponse response = socialLoginService.handleSocialLogin(authorizationCode, loginRequest);

        // Then
        assertThat(response).isEqualTo(expected);

        ArgumentCaptor<AuthIdentity> authIdentityCaptor = ArgumentCaptor.forClass(AuthIdentity.class);
        then(authIdentityRepository).should().save(authIdentityCaptor.capture());
        assertThat(authIdentityCaptor.getValue().getProvider()).isEqualTo(AuthProvider.NAVER);
        assertThat(authIdentityCaptor.getValue().getProviderUserId()).isEqualTo(memberInfoResponse.providerUserId());
        assertThat(authIdentityCaptor.getValue().getMember()).isSameAs(member);
    }

    @Test
    @DisplayName("일치하는 기존 회원이 없으면 새로운 소셜 회원으로 가입시킨 뒤 로그인한다")
    void shouldRegisterMember_whenNoExistingMemberMatchesSocialInfo() {
        // Given
        String authorizationCode = "auth-code";
        MemberLoginRequest loginRequest = MemberLoginRequestFixture.createKakao();
        MemberInfoResponse memberInfoResponse = MemberInfoResponseFixture.createKakaoMember();
        Member member = MemberFixture.createSocialMember(1001L, memberInfoResponse.socialType());
        ReflectionTestUtils.setField(member, "id", 3L);
        LoginSuccessResponse expected = LoginSuccessResponse.of("access-token", "refresh-token", "kkirok",
                "ROLE_MEMBER");

        given(kakaoSocialService.login(authorizationCode, loginRequest)).willReturn(memberInfoResponse);
        given(authIdentityRepository.findByProviderAndProviderUserId(AuthProvider.KAKAO, memberInfoResponse.providerUserId()))
                .willReturn(Optional.empty());
        given(memberUseCase.checkMemberExistsBySocialIdAndSocialType(1001L, memberInfoResponse.socialType()))
                .willReturn(false);
        given(memberRegistrationService.registerMemberWithUserInfo(memberInfoResponse)).willReturn(3L);
        given(memberUseCase.findMemberByMemberId(3L)).willReturn(member);
        given(authenticationService.generateLoginSuccessResponse(3L, member.getUser(), memberInfoResponse))
                .willReturn(expected);

        // When
        LoginSuccessResponse response = socialLoginService.handleSocialLogin(authorizationCode, loginRequest);

        // Then
        assertThat(response).isEqualTo(expected);
        then(memberRegistrationService).should().registerMemberWithUserInfo(memberInfoResponse);
    }
}
