package com.kkirok.server.domain.kkinipop.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.web.multipart.MultipartFile;

@Schema(description = "끼니팝 커스텀 이모지 업로드 multipart 요청")
public record KkinipopCustomEmojiCreateRequest(
        @Schema(type = "string", format = "binary", description = "커스텀 이모지 이미지 파일")
        MultipartFile image
) {
    public record KkinipopCustomEmojiMultipartRequest(
            @Schema(type = "string", format = "binary", description = "커스텀 이모지 이미지 파일")
            MultipartFile image
    ) {
    }
}
