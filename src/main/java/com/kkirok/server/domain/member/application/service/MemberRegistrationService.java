package com.kkirok.server.domain.member.application.service;

import com.kkirok.server.domain.character.application.service.CharacterInitializationService;
import com.kkirok.server.domain.member.application.dto.event.MemberRegisteredEvent;
import com.kkirok.server.domain.member.dao.AuthIdentityRepository;
import com.kkirok.server.domain.member.dao.MemberRepository;
import com.kkirok.server.domain.member.domain.AuthIdentity;
import com.kkirok.server.domain.member.domain.AuthProvider;
import com.kkirok.server.domain.member.domain.Member;
import com.kkirok.server.domain.user.dao.UserRepository;
import com.kkirok.server.domain.user.domain.Role;
import com.kkirok.server.domain.user.domain.Users;
import com.kkirok.server.global.auth.client.dto.MemberInfoResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberRegistrationService {

    private final ApplicationEventPublisher eventPublisher;
    private final UserRepository userRepository;
    private final MemberRepository memberRepository;
    private final AuthIdentityRepository authIdentityRepository;
    private final CharacterInitializationService characterInitializationService;
    private final NotificationAgreeService notificationAgreeService;

    @Transactional
    public Long registerMemberWithUserInfo(final MemberInfoResponse memberInfoResponse) {
        Member member = createMember(memberInfoResponse);
        characterInitializationService.createInitialCharacter(member);
        notificationAgreeService.initializeForMember(member);
        authIdentityRepository.save(AuthIdentity.createSocial(
                member,
                AuthProvider.fromSocialType(memberInfoResponse.socialType()),
                memberInfoResponse.providerUserId()
        ));

        eventPublisher.publishEvent(new MemberRegisteredEvent(member.getNickname()));

        return member.getId();
    }

    @Transactional
    public Member registerLocalMember(final String email, final String passwordHash) {
        return registerLocalMember(email, passwordHash, Role.PENDING, null, true, true);
    }

    @Transactional
    public Member registerAdminMember(final String email, final String passwordHash, final String name) {
        return registerLocalMember(email, passwordHash, Role.ADMIN, name, false, false);
    }

    private Member createMember(final MemberInfoResponse memberInfoResponse) {
        Users users = createUserWithMemberRole();
        Member member = Member.create(memberInfoResponse, users);
        memberRepository.save(member);
        log.info("Member registered with memberId: {}, role: {}", member.getId(), users.getRole());
        return member;
    }

    private Member createLocalMember(final String email, final Role role, final String name) {
        Users users = createUserWithRole(role);
        Member member = role == Role.ADMIN
                ? Member.createAdminLocal(email, name, users)
                : Member.createLocal(email, users);
        memberRepository.save(member);
        log.info("Local member registered with memberId: {}, role: {}", member.getId(), users.getRole());
        return member;
    }

    private Users createUserWithMemberRole() {
        return createUserWithRole(Role.PENDING);
    }

    private Users createUserWithRole(final Role role) {
        Users users = Users.createWithRole(role);
        log.info("Granting {} role to new user", users.getRole());
        users = userRepository.save(users);
        userRepository.flush();
        return users;
    }

    // 멤버 생성. 캐릭터 생성 포함
    private Member registerLocalMember(
            final String email,
            final String passwordHash,
            final Role role,
            final String name,
            final boolean initializeCharacter,
            final boolean publishRegisteredEvent
    ) {
        Member member = createLocalMember(email, role, name);
        if (initializeCharacter) {
            characterInitializationService.createInitialCharacter(member);
            notificationAgreeService.initializeForMember(member);
        }
        authIdentityRepository.save(AuthIdentity.createLocal(member, email, passwordHash));
        if (publishRegisteredEvent) {
            eventPublisher.publishEvent(new MemberRegisteredEvent(member.getNickname()));
        }
        return member;
    }
}
