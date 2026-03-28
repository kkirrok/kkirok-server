package com.kkirok.server.domain.member.application.service;

import com.kkirok.server.domain.member.dao.EmailVerificationHistoryRepository;
import com.kkirok.server.domain.member.dao.redis.EmailVerificationRepository;
import com.kkirok.server.domain.member.domain.EmailVerificationHistory;
import com.kkirok.server.domain.member.exception.EmailErrorCode;
import com.kkirok.server.domain.member.exception.EmailException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EmailVerificationStateService {

    private final EmailVerificationHistoryRepository emailVerificationHistoryRepository;
    private final EmailVerificationRepository emailVerificationRepository;

    @Transactional
    public void verify(final String email, final String code) {
        String verificationCode = emailVerificationRepository.getVerificationCode(email)
                .orElseThrow(() -> new EmailException(EmailErrorCode.EMAIL_VERIFICATION_CODE_NOT_FOUND));

        if (!verificationCode.equals(code)) {
            throw new EmailException(EmailErrorCode.EMAIL_VERIFICATION_CODE_MISMATCH);
        }

        emailVerificationHistoryRepository.findLatestPendingHistory(email, code)
                .ifPresent(EmailVerificationHistory::markSuccess);

        emailVerificationRepository.deleteVerificationCode(email);
        emailVerificationRepository.markVerified(email);
    }

    public void validateVerifiedEmail(final String email) {
        if (!emailVerificationRepository.isVerified(email)) {
            throw new EmailException(EmailErrorCode.EMAIL_NOT_VERIFIED);
        }
    }

    @Transactional
    public void consumeVerifiedEmail(final String email) {
        validateVerifiedEmail(email);
        emailVerificationRepository.deleteVerified(email);
    }
}
