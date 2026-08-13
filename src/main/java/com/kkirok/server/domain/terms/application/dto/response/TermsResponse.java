package com.kkirok.server.domain.terms.application.dto.response;

import com.kkirok.server.domain.terms.domain.Terms;
import com.kkirok.server.domain.terms.domain.TermsType;

import java.net.URL;

public record TermsResponse(
        TermsType type,
        boolean isRequired,
        int version,
        String url
) {
    public static TermsResponse of(Terms terms, URL url) {
        return new TermsResponse(terms.getType(), terms.isRequired(), terms.getVersion(), url.toString());
    }
}
