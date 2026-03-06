package com.kkirok.server.domain.member.application.service;

import com.kkirok.server.domain.member.application.dto.request.LocalLoginRequest;
import com.kkirok.server.domain.member.application.dto.request.LocalSignUpRequest;
import com.kkirok.server.domain.member.application.dto.response.LoginSuccessResponse;
import com.kkirok.server.domain.member.dao.AuthIdentityRepository;
import com.kkirok.server.domain.member.domain.AuthIdentity;
import com.kkirok.server.domain.member.domain.AuthProvider;
import com.kkirok.server.domain.member.domain.Member;
import com.kkirok.server.domain.member.exception.MemberErrorCode;
import com.kkirok.server.global.common.exception.ConflictException;
import com.kkirok.server.global.common.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LocalLoginService {

    private final AuthIdentityRepository authIdentityRepository;
    private final MemberRegistrationService memberRegistrationService;
    private final AuthenticationService authenticationService;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public LoginSuccessResponse signUp(LocalSignUpRequest request) {
        if (authIdentityRepository.existsByProviderAndProviderUserId(AuthProvider.LOCAL, request.email())) {
            throw new ConflictException(MemberErrorCode.LOCAL_EMAIL_ALREADY_EXISTS);
        }

        String encodedPassword = passwordEncoder.encode(request.password());
        Member member = memberRegistrationService.registerLocalMember(
                request.nickname(),
                request.email(),
                encodedPassword
        );

        return authenticationService.generateLoginSuccessResponse(
                member.getId(),
                member.getUser(),
                member.getNickname()
        );
    }

    @Transactional
    public LoginSuccessResponse login(LocalLoginRequest request) {
        AuthIdentity authIdentity = authIdentityRepository
                .findByProviderAndProviderUserId(AuthProvider.LOCAL, request.email())
                .orElseThrow(() -> new UnauthorizedException(MemberErrorCode.LOCAL_LOGIN_FAILED));

        if (authIdentity.getPasswordHash() == null
                || !passwordEncoder.matches(request.password(), authIdentity.getPasswordHash())) {
            throw new UnauthorizedException(MemberErrorCode.LOCAL_LOGIN_FAILED);
        }

        Member member = authIdentity.getMember();
        return authenticationService.generateLoginSuccessResponse(
                member.getId(),
                member.getUser(),
                member.getNickname()
        );
    }
}
