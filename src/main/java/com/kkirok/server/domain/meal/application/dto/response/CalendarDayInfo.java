package com.kkirok.server.domain.meal.application.dto.response;

import com.kkirok.server.domain.character.domain.CharacterStatusType;
import io.swagger.v3.oas.annotations.media.Schema;

public record CalendarDayInfo(

        @Schema(description = "날짜(일)", example = "2023-01-01")
        Integer dayOfMonth,
        @Schema(description = "해당 날짜 상태", example = "AVAILABLE")
        CharacterStatusType status

) {

}
