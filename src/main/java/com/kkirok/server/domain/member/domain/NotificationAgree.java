package com.kkirok.server.domain.member.domain;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "notification_agree")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationAgree {

    @EmbeddedId
    private NotificationAgreeId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("memberId")
    @JoinColumn(name = "member_id")
    private Member member;

    @Column(name = "is_agree", nullable = false)
    private boolean isAgree;

    public static NotificationAgree of(Member member, NotificationAgreeType type) {
        NotificationAgree agree = new NotificationAgree();
        agree.id = new NotificationAgreeId(member.getId(), type);
        agree.member = member;
        agree.isAgree = true;
        return agree;
    }

    public NotificationAgreeType getType() {
        return id.getType();
    }

    public void update(boolean isAgree) {
        this.isAgree = isAgree;
    }

}
