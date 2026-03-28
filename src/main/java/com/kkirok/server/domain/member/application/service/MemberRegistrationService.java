package com.kkirok.server.domain.member.application.service;

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

    @Transactional
    public Long registerMemberWithUserInfo(final MemberInfoResponse memberInfoResponse) {
        Member member = createMember(memberInfoResponse);
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
        Member member = createLocalMember(email);
        authIdentityRepository.save(AuthIdentity.createLocal(member, email, passwordHash));
        eventPublisher.publishEvent(new MemberRegisteredEvent(member.getNickname()));
        return member;
    }

    private Member createMember(final MemberInfoResponse memberInfoResponse) {
        Users users = createUserWithMemberRole();
        Member member = Member.create(memberInfoResponse, users);
        memberRepository.save(member);
        log.info("Member registered with memberId: {}, role: {}", member.getId(), users.getRole());
        return member;
    }

    private Member createLocalMember(final String email) {
        Users users = createUserWithMemberRole();
        Member member = Member.createLocal( email, users);
        memberRepository.save(member);
        log.info("Local member registered with memberId: {}, role: {}", member.getId(), users.getRole());
        return member;
    }

    private Users createUserWithMemberRole() {
        Users users = Users.createWithRole(Role.USER);
        log.info("Granting USER role to new user with role: {}", users.getRole());
        users = userRepository.save(users);
        userRepository.flush();
        return users;
    }
}
