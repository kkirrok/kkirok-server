package com.kkirok.server.domain.character.domain;

import com.kkirok.server.domain.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "item_possession")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ItemPossession extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "character_id", nullable = false)
    private Character character;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @Column(name = "is_wearing", nullable = false)
    private Boolean isWearing;

    @Builder
    private ItemPossession(Character character, Item item) {
        this.character = character;
        this.item = item;
        this.isWearing = false;
    }

    public boolean hasItemId(Long itemId) {
        return item.getId().equals(itemId);
    }

    public boolean isWearing() {
        return Boolean.TRUE.equals(isWearing);
    }

    public boolean hasSameItemType(ItemType itemType) {
        return item.getItemType() == itemType;
    }

    public void wear() {
        this.isWearing = true;
    }

    public void cancelWear() {
        this.isWearing = false;
    }

}
