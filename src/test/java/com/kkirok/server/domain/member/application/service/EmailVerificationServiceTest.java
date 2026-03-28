package com.kkirok.server.domain.member.application.service;

import com.kkirok.server.domain.member.dao.EmailVerificationHistoryRepository;
import com.kkirok.server.domain.member.dao.redis.EmailVerificationRepository;
import com.kkirok.server.domain.member.util.EmailCodeGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class EmailVerificationServiceTest {


    @Mock
    private EmailSender emailSender;

    @Mock
    private EmailVerificationRepository emailVerificationRepository;

    @Mock
    private EmailVerificationHistoryRepository emailVerificationHistoryRepository;

    @InjectMocks
    private EmailVerificationService emailVerificationService;

    @Test
    @DisplayName("인증번호를 생성하고 Redis에 저장한 뒤 이메일을 발송한다")
    void shouldSendVerificationCode() {
        // Given
        String email = "kkirok@test.com";

        try (MockedStatic<EmailCodeGenerator> emailCodeGenerator = org.mockito.Mockito.mockStatic(EmailCodeGenerator.class)) {
            emailCodeGenerator.when(EmailCodeGenerator::generate).thenReturn("123456");

            // When
            emailVerificationService.sendVerificationCode(email);

            // Then
            then(emailVerificationHistoryRepository).should().save(org.mockito.ArgumentMatchers.any());
            then(emailVerificationRepository).should().saveVerificationCode(email, "123456");
            then(emailVerificationRepository).should().deleteVerified(email);
            then(emailSender).should().sendVerificationCode(email, "123456");
        }
    }

    @Test
    @DisplayName("이메일 발송에 실패하면 Redis 인증번호를 제거한다")
    void shouldDeleteVerificationCode_whenEmailSendFails() {
        // Given
        String email = "kkirok@test.com";
        RuntimeException exception = new RuntimeException("send fail");
        org.mockito.BDDMockito.willThrow(exception)
                .given(emailSender)
                .sendVerificationCode(email, "123456");

        try (MockedStatic<EmailCodeGenerator> emailCodeGenerator = org.mockito.Mockito.mockStatic(EmailCodeGenerator.class)) {
            emailCodeGenerator.when(EmailCodeGenerator::generate).thenReturn("123456");

            // When, Then
            assertThatThrownBy(() -> emailVerificationService.sendVerificationCode(email))
                    .isSameAs(exception);
            then(emailVerificationRepository).should().deleteVerificationCode(email);
        }
    }
}
