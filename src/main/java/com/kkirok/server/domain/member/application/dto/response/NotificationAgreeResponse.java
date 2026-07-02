package com.kkirok.server.domain.member.application.dto.response;

import com.kkirok.server.domain.member.domain.NotificationAgree;
import com.kkirok.server.domain.member.domain.NotificationAgreeType;

import java.util.List;

public record NotificationAgreeResponse(
        boolean isAll,
        List<NotificationAgreeItem> agrees
) {
    public record NotificationAgreeItem(
            NotificationAgreeType type,
            boolean isAgree
    ) {}

    public static NotificationAgreeResponse of(List<NotificationAgree> agrees) {
        List<NotificationAgreeItem> items = agrees.stream()
                .map(a -> new NotificationAgreeItem(a.getType(), a.isAgree()))
                .toList();
        boolean isAll = items.stream()
                .map(NotificationAgreeItem::isAgree)
                .distinct()
                .count() <= 1;
        return new NotificationAgreeResponse(isAll, items);
    }
}
