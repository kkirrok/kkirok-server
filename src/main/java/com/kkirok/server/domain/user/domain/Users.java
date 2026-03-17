package com.kkirok.server.domain.user.domain;

import com.kkirok.server.domain.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Users extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Role role;

    @Builder
    private Users(Role role) {
        this.role = role;
    }

    public static Users of() {
        return Users.builder()
                .role(Role.USER)
                .build();
    }

    public static Users createWithRole(Role role) {
        return Users.builder()
                .role(role)
                .build();
    }

}
