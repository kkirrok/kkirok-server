package com.kkirok.server.global.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.Page;

@Schema(description = "공통 페이징 메타 정보")
public record PageInfoResponse(
        @Schema(description = "현재 페이지", example = "0")
        int page,
        @Schema(description = "페이지 크기", example = "20")
        int size,
        @Schema(description = "전체 개수", example = "42")
        long totalElements,
        @Schema(description = "전체 페이지 수", example = "3")
        int totalPages,
        @Schema(description = "다음 페이지 존재 여부", example = "true")
        boolean hasNext
) {
    public static PageInfoResponse from(Page<?> page) {
        return new PageInfoResponse(
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.hasNext()
        );
    }
}
