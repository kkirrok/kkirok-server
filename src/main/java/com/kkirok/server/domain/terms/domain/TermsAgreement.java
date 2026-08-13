package com.kkirok.server.domain.terms.domain;

import com.kkirok.server.domain.BaseTimeEntity;
import com.kkirok.server.domain.member.domain.Member;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "terms_agreement")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TermsAgreement extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "terms_id", nullable = false)
    private Terms terms;

    @Column(name = "is_agree", nullable = false)
    private boolean isAgree;

    @Column(name = "agreed_at", nullable = false)
    private LocalDateTime agreedAt;

    public static TermsAgreement of(Member member, Terms terms, boolean isAgree) {
        TermsAgreement agreement = new TermsAgreement();
        agreement.member = member;
        agreement.terms = terms;
        agreement.isAgree = isAgree;
        agreement.agreedAt = LocalDateTime.now();
        return agreement;
    }

    public void update(boolean isAgree) {
        this.isAgree = isAgree;
        this.agreedAt = LocalDateTime.now();
    }
}
