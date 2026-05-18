package com.kkirok.server.domain.meal.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.web.multipart.MultipartFile;

public record MealImageUploadRequest(

        @Schema(
                description = "업로드할 이미지 파일",
                type = "string",
                format = "binary",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        MultipartFile file

) {
}