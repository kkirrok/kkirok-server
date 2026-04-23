package com.kkirok.server.domain.kkinipop.application.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "나의끼록과 같이 저장 가능 횟수 응답")
public record KkinipopMyKkirokStatusResponse(
        @Schema(description = "현재 남은 횟수", example = "2")
        int remainingCount,
        @Schema(description = "최대 횟수", example = "3")
        int maxCount
) {
    public static KkinipopMyKkirokStatusResponse of(long usedCount, int maxCount) {
        return new KkinipopMyKkirokStatusResponse(
                Math.max(0, maxCount - (int) usedCount),
                maxCount
        );
    }
}
