package com.kkirok.server.domain.kkinipop.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "끼니팝 그룹 참여 요청")
public record KkinipopGroupJoinRequest(
        @Schema(description = "그룹 초대 코드", example = "AB12CD")
        @NotBlank(message = "초대코드는 필수입니다.")
        @Size(max = 12, message = "초대코드는 12자 이하여야 합니다.")
        String inviteCode
) {
}
