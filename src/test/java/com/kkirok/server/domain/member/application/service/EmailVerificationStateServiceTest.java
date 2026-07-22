package com.kkirok.server.domain.member.application.service;

import com.kkirok.server.domain.member.dao.EmailVerificationHistoryRepository;
import com.kkirok.server.domain.member.dao.redis.EmailVerificationRepository;
import com.kkirok.server.domain.member.domain.EmailVerificationHistory;
import com.kkirok.server.domain.member.exception.EmailErrorCode;
import com.kkirok.server.domain.member.exception.EmailException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class EmailVerificationStateServiceTest {

    @Mock
    private EmailVerificationRepository emailVerificationRepository;

    @Mock
    private EmailVerificationHistoryRepository emailVerificationHistoryRepository;

    @InjectMocks
    private EmailVerificationStateService emailVerificationStateService;

    @Test
    @DisplayName("인증번호가 일치하면 이메일을 인증 완료 상태로 저장한다")
    void shouldVerifyEmail_whenCodeMatches() {
        // Given
        String email = "kkirok@test.com";
        String code = "123456";
        EmailVerificationHistory history = EmailVerificationHistory.create(email, code);
        given(emailVerificationRepository.getVerificationCode(email)).willReturn(Optional.of(code));
        given(emailVerificationHistoryRepository.findLatestPendingHistory(email, code))
                .willReturn(Optional.of(history));

        // When
        emailVerificationStateService.verify(email, code);

        // Then
        then(emailVerificationHistoryRepository).should()
                .findLatestPendingHistory(email, code);
        then(emailVerificationRepository).should().deleteVerificationCode(email);
        then(emailVerificationRepository).should().markVerified(email);
    }

    @Test
    @DisplayName("저장된 인증번호가 없으면 예외가 발생한다")
    void shouldThrowBadRequestException_whenVerificationCodeDoesNotExist() {
        // Given
        String email = "kkirok@test.com";
        given(emailVerificationRepository.getVerificationCode(email)).willReturn(Optional.empty());

        // When, Then
        assertBadRequest(() -> emailVerificationStateService.verify(email, "123456"),
                EmailErrorCode.EMAIL_VERIFICATION_CODE_NOT_FOUND);
    }

    @Test
    @DisplayName("인증번호가 일치하지 않으면 예외가 발생한다")
    void shouldThrowBadRequestException_whenVerificationCodeDoesNotMatch() {
        // Given
        String email = "kkirok@test.com";
        given(emailVerificationRepository.getVerificationCode(email)).willReturn(Optional.of("654321"));

        // When, Then
        assertBadRequest(() -> emailVerificationStateService.verify(email, "123456"),
                EmailErrorCode.EMAIL_VERIFICATION_CODE_MISMATCH);
    }

    @Test
    @DisplayName("인증된 이메일이 아니면 회원가입 검증에서 예외가 발생한다")
    void shouldThrowBadRequestException_whenEmailIsNotVerified() {
        // Given
        String email = "kkirok@test.com";
        given(emailVerificationRepository.isVerified(email)).willReturn(false);

        // When, Then
        assertBadRequest(() -> emailVerificationStateService.validateVerifiedEmail(email),
                EmailErrorCode.EMAIL_NOT_VERIFIED);
    }

    private void assertBadRequest(final Runnable action, final EmailErrorCode errorCode) {
        assertThatThrownBy(action::run)
                .isInstanceOf(EmailException.class)
                .extracting("baseErrorCode")
                .isEqualTo(errorCode);
    }
}
