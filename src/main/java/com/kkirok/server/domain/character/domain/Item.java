package com.kkirok.server.domain.character.domain;

import com.kkirok.server.domain.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "item")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Item extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "item_type", nullable = false, length = 50)
    private ItemType itemType;

    @Column(name = "item_image", length = 255)
    private String itemImage;

    @Builder
    private Item(String name, ItemType itemType, String itemImage) {
        this.name = name;
        this.itemType = itemType;
        this.itemImage = itemImage;
    }

}
