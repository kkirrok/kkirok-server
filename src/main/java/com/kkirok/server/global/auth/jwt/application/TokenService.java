package com.kkirok.server.global.auth.jwt.application;


import com.kkirok.server.global.auth.jwt.dao.TokenRepository;
import com.kkirok.server.global.auth.jwt.exception.TokenErrorCode;
import com.kkirok.server.global.auth.redis.Token;
import com.kkirok.server.global.common.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class TokenService {

	private final TokenRepository tokenRepository;

	@Transactional
	public void saveRefreshToken(final Long memberId, final String refreshToken) {
		tokenRepository.save(Token.of(memberId, refreshToken));
	}

	public Long findIdByRefreshToken(final String refreshToken) {
		Token token = tokenRepository.findByRefreshToken(refreshToken)
			.orElseThrow(() -> new NotFoundException(TokenErrorCode.REFRESH_TOKEN_NOT_FOUND));

		return token.getId();
	}

	@Transactional
	public void deleteRefreshToken(final Long memberId) {
		Token token = tokenRepository.findById(memberId)
			.orElseThrow(() -> new NotFoundException(TokenErrorCode.REFRESH_TOKEN_NOT_FOUND));

		tokenRepository.delete(token);
		log.info("Deleted refresh token: {}", token);
	}
}