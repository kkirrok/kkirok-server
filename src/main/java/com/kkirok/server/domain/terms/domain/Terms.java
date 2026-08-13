package com.kkirok.server.domain.terms.domain;

import com.kkirok.server.domain.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "terms")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Terms extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private TermsType type;

    @Column(nullable = false)
    private int version;

    @Column(name = "r2_key", nullable = false, length = 255)
    private String r2Key;

    public static Terms of(TermsType type, int version, String r2Key) {
        Terms terms = new Terms();
        terms.type = type;
        terms.version = version;
        terms.r2Key = r2Key;
        return terms;
    }

    public boolean isRequired() {
        return type.isRequired();
    }
}
