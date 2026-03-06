package com.kkirok.server.global.auth.client.naver.response;

public record NaverUserResponse(
	String resultcode,
	String message,
	NaverUserProfile response
) {
}

