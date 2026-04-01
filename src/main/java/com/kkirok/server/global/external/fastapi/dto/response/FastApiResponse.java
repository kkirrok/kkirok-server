package com.kkirok.server.global.external.fastapi.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record FastApiResponse(
		int statusCode,
		JsonNode body
) {
}
