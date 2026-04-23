package com.kkirok.server.domain.kkinipop.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

@Schema(description = "끼니팝 미션 생성 요청")
public record KkinipopMissionCreateRequest(
        @Schema(description = "미션 제목", example = "10분 안에 과일 올리기")
        @NotBlank(message = "미션 제목은 필수입니다.")
        @Size(max = 40, message = "미션 제목은 40자 이하여야 합니다.")
        String title,

        @Schema(description = "미션 시작 시각", example = "2026-04-22T12:00:00")
        LocalDateTime startAt,
        @Schema(description = "미션 종료 시각", example = "2026-04-22T12:10:00")
        LocalDateTime endAt
) {
}
