package com.kkirok.server.global.external.fastapi;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FastApiEndpoint {

	HEALTH("/health")
	;

	private final String path;
}
