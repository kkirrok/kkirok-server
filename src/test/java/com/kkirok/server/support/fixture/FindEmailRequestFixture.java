package com.kkirok.server.support.fixture;

import com.kkirok.server.domain.member.application.dto.request.FindEmailRequest;

import java.time.LocalDate;

public final class FindEmailRequestFixture {

    private FindEmailRequestFixture() {
    }

    public static FindEmailRequest create() {
        return new FindEmailRequest(
                "김준용",
                LocalDate.of(2002, 4, 13),
                "01012345678"
        );
    }
}
