package com.kkirok.server.domain.kkinipop.application.dto.response;

import com.kkirok.server.global.common.util.DateTimeUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.List;

@Schema(description = "끼니팝 날짜별 게시글 응답")
public record KkinipopDailyPostResponse(
        @Schema(description = "기준 날짜", example = "2026-04-22")
        LocalDate date,
        @Schema(description = "요일 라벨", example = "수")
        String label,
        @Schema(description = "시작 요일 (0:일 ~ 6:토)", example = "0")
        int dayOfWeek,
        @Schema(description = "해당 날짜의 게시글 목록")
        List<KkinipopPostResponse> posts
) {
    public static KkinipopDailyPostResponse of(LocalDate date, List<KkinipopPostResponse> posts) {
        return new KkinipopDailyPostResponse(
                date,
                DateTimeUtils.toDayLabel(date),
                DateTimeUtils.toDayOfWeekNumber(date),
                posts
        );
    }
}
