package com.kkirok.server.global.external.fastapi;

import com.kkirok.server.global.external.ExternalClient;
import com.kkirok.server.global.external.fastapi.dto.request.FastApiRequest;
import com.kkirok.server.global.external.fastapi.dto.response.FastApiResponse;

public interface FastApiClient extends ExternalClient<FastApiRequest, FastApiResponse> {

	FastApiResponse call(FastApiRequest request);

	@Override
	default FastApiResponse execute(FastApiRequest request) {
		return call(request);
	}
}
