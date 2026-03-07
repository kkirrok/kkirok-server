package com.kkirok.server.global.auth.client.naver.response;

public record NaverUserProfile(
	String id,
	String nickname,
	String email
) {
}

