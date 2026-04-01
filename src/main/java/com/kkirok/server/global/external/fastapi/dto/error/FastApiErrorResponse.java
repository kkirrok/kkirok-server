package com.kkirok.server.global.external.fastapi.dto.error;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record FastApiErrorResponse(
		String detail,
		JsonNode error
) {
}
