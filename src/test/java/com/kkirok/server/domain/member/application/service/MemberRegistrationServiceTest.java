package com.kkirok.server.domain.member.application.service;

import com.kkirok.server.domain.character.application.service.CharacterInitializationService;
import com.kkirok.server.domain.member.application.dto.event.MemberRegisteredEvent;
import com.kkirok.server.domain.member.dao.AuthIdentityRepository;
import com.kkirok.server.domain.member.dao.MemberRepository;
import com.kkirok.server.domain.member.domain.AuthIdentity;
import com.kkirok.server.domain.member.domain.AuthProvider;
import com.kkirok.server.domain.member.domain.Member;
import com.kkirok.server.domain.member.domain.SocialType;
import com.kkirok.server.domain.user.dao.UserRepository;
import com.kkirok.server.domain.user.domain.Role;
import com.kkirok.server.domain.user.domain.Users;
import com.kkirok.server.global.auth.client.dto.MemberInfoResponse;
import com.kkirok.server.support.fixture.MemberInfoResponseFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class MemberRegistrationServiceTest {

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private AuthIdentityRepository authIdentityRepository;

    @Mock
    private CharacterInitializationService characterInitializationService;

    @InjectMocks
    private MemberRegistrationService memberRegistrationService;

    @Test
    @DisplayName("로컬 회원 정보를 등록하면 회원과 로컬 인증 수단이 함께 저장된다")
    void shouldRegisterLocalMember_whenValidLocalMemberInfoIsProvided() {
        // Given
        given(userRepository.save(any(Users.class))).willAnswer(invocation -> {
            Users user = invocation.getArgument(0);
            ReflectionTestUtils.setField(user, "id", 10L);
            return user;
        });
        given(memberRepository.save(any(Member.class))).willAnswer(invocation -> {
            Member member = invocation.getArgument(0);
            ReflectionTestUtils.setField(member, "id", 1L);
            return member;
        });

        // When
        Member registeredMember = memberRegistrationService.registerLocalMember(
                "kkirok@test.com",
                "encoded-password"
        );

        // Then
        assertThat(registeredMember.getId()).isEqualTo(1L);
        assertThat(registeredMember.getNickname()).isNull();
        assertThat(registeredMember.getEmail()).isEqualTo("kkirok@test.com");
        assertThat(registeredMember.getUser().getRole()).isEqualTo(Role.PENDING);

        ArgumentCaptor<AuthIdentity> authIdentityCaptor = ArgumentCaptor.forClass(AuthIdentity.class);
        ArgumentCaptor<MemberRegisteredEvent> eventCaptor = ArgumentCaptor.forClass(MemberRegisteredEvent.class);

        then(userRepository).should().flush();
        then(characterInitializationService).should().createInitialCharacter(registeredMember);
        then(authIdentityRepository).should().save(authIdentityCaptor.capture());
        then(eventPublisher).should().publishEvent(eventCaptor.capture());

        AuthIdentity authIdentity = authIdentityCaptor.getValue();
        assertThat(authIdentity.getProvider()).isEqualTo(AuthProvider.LOCAL);
        assertThat(authIdentity.getProviderUserId()).isEqualTo("kkirok@test.com");
        assertThat(authIdentity.getPasswordHash()).isEqualTo("encoded-password");
        assertThat(authIdentity.getMember()).isSameAs(registeredMember);

        assertThat(eventCaptor.getValue().nickname()).isNull();
    }

    @Test
    @DisplayName("소셜 회원 정보를 등록하면 소셜 인증 수단과 회원 ID를 반환한다")
    void shouldRegisterSocialMemberAndReturnMemberId_whenSocialMemberInfoIsProvided() {
        // Given
        MemberInfoResponse memberInfoResponse = MemberInfoResponseFixture.create(
                1001L,
                "kakao-1001",
                "kkirok",
                "kkirok@test.com",
                SocialType.KAKAO
        );

        given(userRepository.save(any(Users.class))).willAnswer(invocation -> {
            Users user = invocation.getArgument(0);
            ReflectionTestUtils.setField(user, "id", 10L);
            return user;
        });
        given(memberRepository.save(any(Member.class))).willAnswer(invocation -> {
            Member member = invocation.getArgument(0);
            ReflectionTestUtils.setField(member, "id", 2L);
            return member;
        });

        // When
        Long memberId = memberRegistrationService.registerMemberWithUserInfo(memberInfoResponse);

        // Then
        assertThat(memberId).isEqualTo(2L);

        ArgumentCaptor<AuthIdentity> authIdentityCaptor = ArgumentCaptor.forClass(AuthIdentity.class);
        ArgumentCaptor<MemberRegisteredEvent> eventCaptor = ArgumentCaptor.forClass(MemberRegisteredEvent.class);

        then(authIdentityRepository).should().save(authIdentityCaptor.capture());
        then(eventPublisher).should().publishEvent(eventCaptor.capture());

        AuthIdentity authIdentity = authIdentityCaptor.getValue();
        then(characterInitializationService).should().createInitialCharacter(authIdentity.getMember());
        assertThat(authIdentity.getProvider()).isEqualTo(AuthProvider.KAKAO);
        assertThat(authIdentity.getProviderUserId()).isEqualTo("kakao-1001");
        assertThat(authIdentity.getPasswordHash()).isNull();
        assertThat(authIdentity.getMember().getSocialId()).isEqualTo(1001L);
        assertThat(authIdentity.getMember().getSocialType()).isEqualTo(SocialType.KAKAO);
        assertThat(authIdentity.getMember().getUser().getRole()).isEqualTo(Role.PENDING);

        assertThat(eventCaptor.getValue().nickname()).isNull();
    }
}
