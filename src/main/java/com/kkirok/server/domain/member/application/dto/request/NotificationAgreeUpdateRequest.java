package com.kkirok.server.domain.member.application.dto.request;

import com.kkirok.server.domain.member.domain.NotificationAgreeType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record NotificationAgreeUpdateRequest(
        @NotNull Boolean isAll,
        @NotNull @Size(min = 1) List<@Valid NotificationAgreeItem> agrees
) {
    public record NotificationAgreeItem(
            @NotNull NotificationAgreeType type,
            @NotNull Boolean isAgree
    ) {}
}
