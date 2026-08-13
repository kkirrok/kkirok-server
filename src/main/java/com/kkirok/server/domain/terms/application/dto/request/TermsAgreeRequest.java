package com.kkirok.server.domain.terms.application.dto.request;

import com.kkirok.server.domain.terms.domain.TermsType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record TermsAgreeRequest(
        @NotNull @Size(min = 1) List<@Valid TermsAgreeItem> agrees
) {
    public record TermsAgreeItem(
            @NotNull TermsType type,
            @NotNull Boolean isAgree
    ) {}
}
