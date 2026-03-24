package com.kkirok.server.domain.report.application.dto.response;

import com.kkirok.server.domain.report.domain.ReportSuggestion;
import io.swagger.v3.oas.annotations.media.Schema;

public record SuggestionResponse (
    @Schema(example = "영양소는 고르게 섭취해요")
    String title,
    @Schema(example = " 당은 과다, 다른 영양소는 부족해요 에너지뿐 아니라 필수 영양소도 챙기며 균형잡힌 식사로 쉽게 시작해보세요")
    String content
) {
    public static SuggestionResponse from(ReportSuggestion suggestion) {
        return new SuggestionResponse(
                suggestion.getTitle(),
                suggestion.getContent()
        );
    }
}
