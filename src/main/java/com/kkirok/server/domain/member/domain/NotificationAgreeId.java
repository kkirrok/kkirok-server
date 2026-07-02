package com.kkirok.server.domain.member.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@Getter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class NotificationAgreeId implements Serializable {

    @Column(name = "member_id")
    private Long memberId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 50)
    private NotificationAgreeType type;
}
