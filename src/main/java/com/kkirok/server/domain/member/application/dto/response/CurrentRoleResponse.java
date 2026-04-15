package com.kkirok.server.domain.member.application.dto.response;

import com.kkirok.server.domain.user.domain.Role;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "현재 로그인한 사용자의 권한")
public record CurrentRoleResponse(

        @Schema(example = "USER", description = "역할 enum 값")
        String role

) {
    public static CurrentRoleResponse from(final Role role) {
        return new CurrentRoleResponse(role.name());
    }
}
