package com.kkirok.server.global.auth.client.application;

import com.kkirok.server.global.auth.client.dto.MemberInfoResponse;
import com.kkirok.server.global.auth.client.dto.MemberLoginRequest;

public interface SocialService {
	MemberInfoResponse login(final MemberLoginRequest loginRequest);
}
