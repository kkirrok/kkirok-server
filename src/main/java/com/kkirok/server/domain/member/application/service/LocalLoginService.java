package com.kkirok.server.domain.member.application.service;

import com.kkirok.server.domain.member.application.dto.request.LocalLoginRequest;
import com.kkirok.server.domain.member.application.dto.request.LocalSignUpRequest;
import com.kkirok.server.domain.member.application.dto.response.LoginSuccessResponse;
import com.kkirok.server.domain.member.dao.AuthIdentityRepository;
import com.kkirok.server.domain.member.domain.AuthIdentity;
import com.kkirok.server.domain.member.domain.AuthProvider;
import com.kkirok.server.domain.member.domain.Member;
import com.kkirok.server.domain.member.exception.MemberErrorCode;
import com.kkirok.server.global.common.exception.BadRequestException;
import com.kkirok.server.domain.user.domain.Role;
import com.kkirok.server.global.common.exception.ConflictException;
import com.kkirok.server.global.common.exception.ForbiddenException;
import com.kkirok.server.global.common.exception.NotFoundException;
import com.kkirok.server.global.common.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.regex.Pattern;

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
        checkPasswordValidation(request.password());

        String encodedPassword = passwordEncoder.encode(request.password());
        Member member = memberRegistrationService.registerLocalMember(
                request.email(),
                encodedPassword
        );

        return authenticationService.generateLoginSuccessResponse(member);
    }

    @Transactional
    public LoginSuccessResponse login(LocalLoginRequest request) {
        return login(request, List.of(Role.PENDING, Role.USER));
    }

    @Transactional
    public LoginSuccessResponse adminLogin(LocalLoginRequest request) {
        return login(request, List.of(Role.ADMIN));
    }

    private LoginSuccessResponse login(LocalLoginRequest request, List<Role> expectedRoles) {
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
        if (!expectedRoles.contains(member.getUser().getRole())) {
            throwRoleMismatchException(expectedRoles);
        }
        return authenticationService.generateLoginSuccessResponse(member);
    }

    // 올바르지 않은 권한에 대해 처리
    private void throwRoleMismatchException(List<Role> expectedRoles) {
        if (expectedRoles.contains(Role.ADMIN) && !expectedRoles.contains(Role.USER)) {
            throw new ForbiddenException(MemberErrorCode.ADMIN_LOGIN_FOR_USER_ACCOUNT);
        }
        if ((expectedRoles.contains(Role.USER) || expectedRoles.contains(Role.PENDING))
                && !expectedRoles.contains(Role.ADMIN)) {
            throw new ForbiddenException(MemberErrorCode.USER_LOGIN_FOR_ADMIN_ACCOUNT);
        }
        throw new ForbiddenException(MemberErrorCode.INVALID_ROLE);
    }

    private RuntimeException resolveMissingLocalAccount(String email) {
        if (authIdentityRepository.existsByMemberEmailAndProviderNot(email, AuthProvider.LOCAL)) {
            return new ForbiddenException(MemberErrorCode.SOCIAL_ACCOUNT_LOCAL_LOGIN_FORBIDDEN);
        }
        return new NotFoundException(MemberErrorCode.LOCAL_ACCOUNT_NOT_FOUND);
    }

    // 비밀번호가 정해진 규격에 맞는지
    private void checkPasswordValidation(String password){

        Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[^A-Za-z\\d]).+$"); // 비밀번호는 영문,숫자,특수문자의 조합이어야 함

        if (password == null || !PASSWORD_PATTERN.matcher(password).matches()) {
            throw new BadRequestException(MemberErrorCode.INVALID_PASSWORD_FORMAT);
        }
    }

}
