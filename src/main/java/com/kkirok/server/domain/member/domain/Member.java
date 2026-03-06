package com.kkirok.server.domain.member.domain;

import com.kkirok.server.domain.BaseTimeEntity;
import com.kkirok.server.domain.user.domain.Users;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nickname;

    @Column(nullable = true)
    private String email;

    @Column(nullable = true)
    private LocalDateTime deletedAt;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Users user;

    @Column(nullable = true)
    private Long socialId;  // 소셜 회원번호 저장

    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    private SocialType socialType;

    @Builder
    private Member(String nickname, String email, LocalDateTime deletedAt, Users user, Long socialId,
                   SocialType socialType) {
        this.nickname = nickname;
        this.email = email;
        this.deletedAt = deletedAt;
        this.user = user;
        this.socialId = socialId;
        this.socialType = socialType;
    }

    public static Member create(
            final String nickname,
            final String email,
            final Users user,
            final Long socialId,
            final SocialType socialType
    ) {
        return Member.builder()
                .nickname(nickname)
                .email(email)
                .user(user)
                .socialId(socialId)
                .socialType(socialType)
                .build();
    }

    public static Member createLocal(
            final String nickname,
            final String email,
            final Users user
    ) {
        return Member.builder()
                .nickname(nickname)
                .email(email)
                .user(user)
                .socialId(null)
                .socialType(null)
                .build();
    }
}
