package com.kkirok.server.domain.terms.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TermsType {
    TERMS_OF_SERVICE(true),
    PRIVACY_COLLECTION(true),
    PRIVACY_THIRD_PARTY(true),
    MARKETING(false),
    ;

    private final boolean required;
}
