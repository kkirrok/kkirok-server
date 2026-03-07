package com.kkirok.server.domain.member.domain;

import com.kkirok.server.domain.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

/**
 * 회원의 "인증 수단"을 저장하는 엔티티.
 * Member가 사용자 본체(프로필/권한)라면, AuthIdentity는 로그인 방법(LOCAL/KAKAO/NAVER)과
 * 해당 방법에서의 식별자(providerUserId)를 분리해서 관리한다.
 *
 * - LOCAL: providerUserId=email, passwordHash 사용
 * - SOCIAL: providerUserId=각 소셜 제공자 고유 식별자(ex. kakao id, naver id), passwordHash는 null
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "auth_identity",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_auth_identity_provider_user_id", columnNames = {"provider", "provider_user_id"})
        }
)
public class AuthIdentity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AuthProvider provider;

    @Column(name = "provider_user_id", nullable = false, length = 255)
    private String providerUserId;

    @Column(name = "password_hash", length = 255)
    private String passwordHash;

    @Builder
    private AuthIdentity(Member member, AuthProvider provider, String providerUserId, String passwordHash) {
        this.member = member;
        this.provider = provider;
        this.providerUserId = providerUserId;
        this.passwordHash = passwordHash;
    }

    public static AuthIdentity createSocial(Member member, AuthProvider provider, String providerUserId) {
        return AuthIdentity.builder()
                .member(member)
                .provider(provider)
                .providerUserId(providerUserId)
                .passwordHash(null)
                .build();
    }

    public static AuthIdentity createLocal(Member member, String email, String passwordHash) {
        return AuthIdentity.builder()
                .member(member)
                .provider(AuthProvider.LOCAL)
                .providerUserId(email)
                .passwordHash(passwordHash)
                .build();
    }
}
