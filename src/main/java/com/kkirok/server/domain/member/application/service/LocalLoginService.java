package com.kkirok.server.domain.member.application.service;

import com.kkirok.server.domain.member.application.dto.request.LocalLoginRequest;
import com.kkirok.server.domain.member.application.dto.request.LocalSignUpRequest;
import com.kkirok.server.domain.member.application.dto.response.LoginSuccessResponse;
import com.kkirok.server.domain.member.dao.AuthIdentityRepository;
import com.kkirok.server.domain.member.domain.AuthIdentity;
import com.kkirok.server.domain.member.domain.AuthProvider;
import com.kkirok.server.domain.member.domain.Member;
import com.kkirok.server.domain.member.exception.MemberErrorCode;
import com.kkirok.server.domain.user.domain.Role;
import com.kkirok.server.global.common.exception.ConflictException;
import com.kkirok.server.global.common.exception.ForbiddenException;
import com.kkirok.server.global.common.exception.NotFoundException;
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
    private final EmailVerificationStateService emailVerificationStateService;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public LoginSuccessResponse signUp(LocalSignUpRequest request) {
        if (authIdentityRepository.existsByProviderAndProviderUserId(AuthProvider.LOCAL, request.email())) {
            throw new ConflictException(MemberErrorCode.LOCAL_EMAIL_ALREADY_EXISTS);
        }
        emailVerificationStateService.consumeVerifiedEmail(request.email());

        String encodedPassword = passwordEncoder.encode(request.password());
        Member member = memberRegistrationService.registerLocalMember(
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
        return login(request, Role.USER);
    }

    @Transactional
    public LoginSuccessResponse adminLogin(LocalLoginRequest request) {
        return login(request, Role.ADMIN);
    }

    private LoginSuccessResponse login(LocalLoginRequest request, Role expectedRole) {
        AuthIdentity authIdentity = authIdentityRepository
                .findByProviderAndProviderUserId(AuthProvider.LOCAL, request.email())
                .orElseThrow(() -> resolveMissingLocalAccount(request.email()));

        if (authIdentity.getMember().getDeletedAt() != null) {
            throw new ConflictException(MemberErrorCode.DELETED_MEMBER);
        }

        if (authIdentity.getPasswordHash() == null
                || !passwordEncoder.matches(request.password(), authIdentity.getPasswordHash())) {
            throw new UnauthorizedException(MemberErrorCode.LOCAL_LOGIN_PASSWORD_MISMATCH);
        }

        Member member = authIdentity.getMember();
        if (member.getUser().getRole() != expectedRole) {
            throwRoleMismatchException(expectedRole);
        }
        return authenticationService.generateLoginSuccessResponse(
                member.getId(),
                member.getUser(),
                member.getNickname()
        );
    }

    // 올바르지 않은 권한에 대해 처리
    private void throwRoleMismatchException(Role expectedRole) {
        if (expectedRole == Role.ADMIN) {
            throw new ForbiddenException(MemberErrorCode.ADMIN_LOGIN_FOR_USER_ACCOUNT);
        }
        throw new ForbiddenException(MemberErrorCode.USER_LOGIN_FOR_ADMIN_ACCOUNT);
    }

    private RuntimeException resolveMissingLocalAccount(String email) {
        if (authIdentityRepository.existsByMemberEmailAndProviderNot(email, AuthProvider.LOCAL)) {
            return new ForbiddenException(MemberErrorCode.SOCIAL_ACCOUNT_LOCAL_LOGIN_FORBIDDEN);
        }
        return new NotFoundException(MemberErrorCode.LOCAL_ACCOUNT_NOT_FOUND);
    }
}
