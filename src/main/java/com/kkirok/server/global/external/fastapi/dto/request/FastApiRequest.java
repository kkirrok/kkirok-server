package com.kkirok.server.global.external.fastapi.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.kkirok.server.global.external.fastapi.FastApiEndpoint;

import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record FastApiRequest(
		FastApiEndpoint endpoint,
		Object body,
		Map<String, String> headers
) {

	public String path() {
		return endpoint == null ? null : endpoint.getPath();
	}

}
