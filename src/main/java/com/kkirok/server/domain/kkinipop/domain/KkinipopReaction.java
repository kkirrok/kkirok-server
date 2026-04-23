package com.kkirok.server.domain.kkinipop.domain;

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
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "kkinipop_reaction")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class KkinipopReaction extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_id", nullable = false)
    private KkinipopPost post;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "emoji_code", nullable = false, length = 30)
    private String emojiCode;

    @Column(name = "emoji_label", nullable = false, length = 30)
    private String emojiLabel;

    @Column(name = "custom_emoji", nullable = false)
    private boolean customEmoji;

    @Builder
    private KkinipopReaction(KkinipopPost post, Member member, String emojiCode, String emojiLabel, boolean customEmoji) {
        this.post = post;
        this.member = member;
        this.emojiCode = emojiCode;
        this.emojiLabel = emojiLabel;
        this.customEmoji = customEmoji;
    }

    public static KkinipopReaction createDefault(KkinipopPost post, Member member, KkinipopReactionEmoji emoji) {
        return KkinipopReaction.builder()
                .post(post)
                .member(member)
                .emojiCode(emoji.getCode())
                .emojiLabel(emoji.getLabel())
                .customEmoji(false)
                .build();
    }

    public static KkinipopReaction createCustom(KkinipopPost post, Member member, KkinipopCustomEmoji customEmoji) {
        return KkinipopReaction.builder()
                .post(post)
                .member(member)
                .emojiCode("CUSTOM_" + customEmoji.getId())
                .emojiLabel(customEmoji.getLabel())
                .customEmoji(true)
                .build();
    }
}
