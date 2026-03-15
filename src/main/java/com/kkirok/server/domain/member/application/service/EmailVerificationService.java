package com.kkirok.server.domain.member.application.service;

import com.kkirok.server.domain.member.dao.redis.EmailVerificationRepository;
import com.kkirok.server.domain.member.util.EmailCodeGenerator;
import com.kkirok.server.global.common.redis.exception.RedisException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private final EmailSender emailSender;
    private final EmailVerificationRepository emailVerificationRepository;

    public void sendVerificationCode(final String email) {
        String code = EmailCodeGenerator.generate();
        emailVerificationRepository.saveVerificationCode(email, code);
        try {
            emailVerificationRepository.deleteVerified(email);
            emailSender.sendVerificationCode(email, code);
        } catch (RuntimeException exception) {
            cleanupVerificationCode(email, exception);
            throw exception;
        }
    }

    private void cleanupVerificationCode(final String email, final RuntimeException originException) {
        try {
            emailVerificationRepository.deleteVerificationCode(email);
        } catch (RedisException cleanupException) {
            log.error("Failed to cleanup verification code after email flow failure. email={}", email, cleanupException);
            originException.addSuppressed(cleanupException);
        }
    }
}
