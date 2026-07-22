package com.kkirok.server.domain.member.application.service;

import com.kkirok.server.domain.member.application.dto.request.FindEmailRequest;
import com.kkirok.server.domain.member.application.dto.request.ResetPasswordRequest;
import com.kkirok.server.domain.member.dao.AuthIdentityRepository;
import com.kkirok.server.domain.member.dao.MemberRepository;
import com.kkirok.server.domain.member.domain.AuthIdentity;
import com.kkirok.server.domain.member.domain.AuthProvider;
import com.kkirok.server.domain.member.domain.Member;
import com.kkirok.server.domain.member.exception.MemberErrorCode;
import com.kkirok.server.global.common.exception.ForbiddenException;
import com.kkirok.server.global.common.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/*
    이메일 찾기, 비밀번호 수정 맡는 Service 클래스
 */
@Service
@RequiredArgsConstructor
public class AccountRecoveryService {

    private final MemberRepository memberRepository;
    private final AuthIdentityRepository authIdentityRepository;
    private final EmailVerificationStateService emailVerificationStateService;
    private final PasswordEncoder passwordEncoder;

    // 이메일 찾기
    @Transactional(readOnly = true)
    public String findEmail(final FindEmailRequest request) {

        // 멤버 조회, 결과 없으면 예외 발생
        Member member = memberRepository.findByNameAndBirthdayAndPhone(
                        request.name(),
                        request.birth(),
                        request.phone()
                ).orElseThrow(() -> new NotFoundException(MemberErrorCode.ACCOUNT_RECOVERY_INFO_MISMATCH));

        return member.getEmail();
    }

    // 비밀번호 재설정
    @Transactional
    public void resetPassword(final ResetPasswordRequest request) {

        // 이메일 인증 여부 검증
        emailVerificationStateService.validateVerifiedEmail(request.email());

        // 인증정보 조회
        AuthIdentity authIdentity = authIdentityRepository
                .findByProviderAndProviderUserId(AuthProvider.LOCAL, request.email())
                .orElseThrow(() -> new NotFoundException(MemberErrorCode.ACCOUNT_RECOVERY_INFO_MISMATCH));

        Member member = authIdentity.getMember();

        // 정보 일치하지 않으면 예외 발생
        if (!member.compareInfo(request)) {
            throw new NotFoundException(MemberErrorCode.ACCOUNT_RECOVERY_INFO_MISMATCH);
        }

        // 비밀번호 변경
        authIdentity.changePassword(passwordEncoder.encode(request.newPassword()));

        // 이메일 인증번호 폐기
        emailVerificationStateService.consumeVerifiedEmail(request.email());
    }
}
