package com.kkirok.server.domain.character.domain;

import com.kkirok.server.domain.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "character_type")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CharacterType extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "type_code", nullable = false, length = 50)
    private String typeCode;

    @Column(name = "type_name", nullable = false, length = 100)
    private String typeName;

    @Column(name = "base_image", length = 255)
    private String baseImage; // TODO: 이미지 null 여부 체크하고 수정하기

    @Builder
    private CharacterType(String typeCode, String typeName, String baseImage) {
        this.typeCode = typeCode;
        this.typeName = typeName;
        this.baseImage = baseImage;
    }

    public static CharacterType create(String typeCode, String typeName, String baseImage) {
        return CharacterType.builder()
                .typeCode(typeCode)
                .typeName(typeName)
                .baseImage(baseImage)
                .build();
    }

}
