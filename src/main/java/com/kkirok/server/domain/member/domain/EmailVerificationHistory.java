package com.kkirok.server.domain.member.domain;

import com.kkirok.server.domain.BaseTimeEntity;
import com.kkirok.server.domain.member.dao.redis.EmailVerificationRepository;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/*
    인증번호 발생시 데이터 생성 -> isSuccess default = false
    인증하면 isSuccess = true
    하지만 expired될 때까지 인증 안 하면 영원히 false로 남음
 */
@Getter
@Entity
@Table(name = "email_verification_history")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EmailVerificationHistory extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email", nullable = false, length = 50)
    private String email;

    @Column(name = "code", nullable = false, length = 50)
    private String code;

    @Column(name = "is_success", nullable = false)
    private Boolean isSuccess;

    @Column(name = "request_at", nullable = false)
    private LocalDateTime requestAt;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Column(name = "expire_date_time", nullable = false)
    private LocalDateTime expireDateTime;

    @Builder
    private EmailVerificationHistory(String email, String code, LocalDateTime expireDateTime) {
        this.email = email;
        this.code = code;
        this.isSuccess = false;
        this.requestAt = LocalDateTime.now();
        this.verifiedAt = null;
        this.expireDateTime = expireDateTime;
    }

    public static EmailVerificationHistory create(final String email, final String code) {
        return EmailVerificationHistory.builder()
                .email(email)
                .code(code)
                .expireDateTime(LocalDateTime.now().plus(EmailVerificationRepository.verificationCodeTtl()))
                .build();
    }

    public void markSuccess() {
        this.isSuccess = true;
        this.verifiedAt = LocalDateTime.now();
    }

}
