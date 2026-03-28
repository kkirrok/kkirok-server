package com.kkirok.server.domain.member.domain;

import com.kkirok.server.domain.BaseTimeEntity;
import com.kkirok.server.domain.member.application.dto.request.ProfileSettingRequest;
import com.kkirok.server.domain.user.domain.Users;
import com.kkirok.server.global.auth.client.dto.MemberInfoResponse;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

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

    @Column(name = "name", length = 20)
    private String name;

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

    @Column(name = "birthday")
    private LocalDate birthday;

    @Column(name = "phone", length = 20)
    private String phone;

    @OneToOne(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Onboarding onboarding;

    @Builder
    private Member(String email, String profileImage,
                   Users user, Long socialId, SocialType socialType
                   ) {
        this.email = email;
        this.profileImage = profileImage;
        this.user = user;
        this.socialId = socialId;
        this.socialType = socialType;
    }

    public static Member create(
            final MemberInfoResponse memberInfoResponse,
            final Users user
    ) {
        return Member.builder()
                .email(memberInfoResponse.email())
                .profileImage(null)
                .user(user)
                .socialId(memberInfoResponse.socialId())
                .socialType(memberInfoResponse.socialType())
                .build();
    }

    public static Member createLocal(
            final String email,
            final Users user
    ) {
        return Member.builder()
                .email(email)
                .profileImage(null)
                .user(user)
                .socialId(null)
                .socialType(null)
                .build();
    }

    public void updateOnboarding(ProfileSettingRequest dto, String profileImageKey){
        if (profileImageKey != null) {
            this.profileImage = profileImageKey;
        }
        this.name = dto.name();
        this.nickname = dto.nickname();
        this.phone = dto.phone();
        this.gender = dto.gender();
        this.birthday = dto.birth();
        this.onboardingCompleted = true;

        if (Objects.isNull(this.onboarding)) {
            this.onboarding = Onboarding.create(this, dto);
            return;
        }

        this.onboarding.update(dto);
    }

    void assignOnboarding(final Onboarding onboarding) {
        this.onboarding = onboarding;
    }

}
