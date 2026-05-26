package com.kkirok.server.global.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.Map;

public record EnumResponse(

        @Schema(description = "지원하는 enum 종류")
        List<EnumSummary> summary,
        @Schema(description = "enum 상세")
        List<EnumDetail> details

){

    public record EnumSummary(
            String enumName,
            String description
    ){}

    public record EnumDetail(
        String enumName,
        Map<String, String> sort
    ){}


}
