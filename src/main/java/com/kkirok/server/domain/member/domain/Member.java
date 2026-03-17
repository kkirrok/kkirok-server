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
@Table(name = "member")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nickname;

    @Column(nullable = false, length = 50)
    private String email;

    @Column(name = "profile_image", length = 255)
    private String profileImage;

    @Column(name = "onboarding_completed", nullable = false)
    private boolean onboardingCompleted;

    @Column(nullable = true)
    private LocalDateTime deletedAt;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Users user;

    @Column(nullable = true)
    private Long socialId;  // 소셜 회원번호 저장

    @Enumerated(EnumType.STRING)
    @Column(nullable = true, length = 20)
    private SocialType socialType;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private Gender gender;

    private Integer age;

    @Builder
    private Member(String nickname, String email, String profileImage, boolean onboardingCompleted,
                   LocalDateTime deletedAt, Users user, Long socialId, SocialType socialType,
                   Gender gender, Integer age) {
        this.nickname = nickname;
        this.email = email;
        this.profileImage = profileImage;
        this.onboardingCompleted = onboardingCompleted;
        this.deletedAt = deletedAt;
        this.user = user;
        this.socialId = socialId;
        this.socialType = socialType;
        this.gender = gender;
        this.age = age;
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
                .profileImage(null)
                .onboardingCompleted(false)
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
                .profileImage(null)
                .onboardingCompleted(false)
                .user(user)
                .socialId(null)
                .socialType(null)
                .build();
    }
}
