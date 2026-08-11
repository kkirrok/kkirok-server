package com.kkirok.server.global.auth.jwt.provider;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;

@Slf4j
@Service
public class JwtTokenProvider {

	@Value("${jwt.secret}")
	private String jwtSecret;

	@Value("${jwt.access-token-expire-time}")
	private long accessTokenExpireTime;

	@Value("${jwt.refresh-token-expire-time}")
	private long refreshTokenExpireTime;

	private static final String MEMBER_ID = "memberId";

	@PostConstruct
	protected void init() {
		jwtSecret = Base64.getEncoder().encodeToString(jwtSecret.getBytes(StandardCharsets.UTF_8));
	}

	public String issueAccessToken(final Authentication authentication) {
		return issueToken(authentication, accessTokenExpireTime);
	}

	public String issueRefreshToken(final Authentication authentication) {
		return issueToken(authentication, refreshTokenExpireTime);
	}

	public JwtValidationType validateToken(String token) {
		try {
			Claims claims = getBody(token);
			return JwtValidationType.VALID_JWT;
		} catch (MalformedJwtException ex) {
			log.error("Invalid JWT Token: {}", ex.getMessage());
			return JwtValidationType.INVALID_JWT_TOKEN;
		} catch (ExpiredJwtException ex) {
			log.error("Expired JWT Token: {}", ex.getMessage());
			return JwtValidationType.EXPIRED_JWT_TOKEN;
		} catch (UnsupportedJwtException ex) {
			log.error("Unsupported JWT Token: {}", ex.getMessage());
			return JwtValidationType.UNSUPPORTED_JWT_TOKEN;
		} catch (IllegalArgumentException ex) {
			log.error("Empty JWT Token or Illegal Argument: {}", ex.getMessage());
			return JwtValidationType.EMPTY_JWT;
		} catch (SignatureException ex) {
			log.error("Invalid JWT Signature: {}", ex.getMessage());
			return JwtValidationType.INVALID_JWT_SIGNATURE;
		}
	}

	public Long getMemberIdFromJwt(String token) {
		Claims claims = getBody(token);
		Long memberId = Long.valueOf(claims.get(MEMBER_ID).toString());

		// 로그 추가: memberId 확인
		log.info("Extracted memberId from JWT: {}", memberId);

		return memberId;
	}

	private String issueToken(final Authentication authentication, final long expiredTime) {
		final Date now = new Date();

		final Claims claims = Jwts.claims().setIssuedAt(now).setExpiration(new Date(now.getTime() + expiredTime));

		claims.put(MEMBER_ID, authentication.getPrincipal());
		log.info("Added member ID to claims: {}", authentication.getPrincipal());

		return Jwts.builder()
			.setHeaderParam(Header.TYPE, Header.JWT_TYPE)
			.setClaims(claims)
			.signWith(getSigningKey())
			.compact();
	}

	private Claims getBody(final String token) {
		return Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token).getBody();
	}

	private SecretKey getSigningKey() {
		String encodedKey = Base64.getEncoder().encodeToString(jwtSecret.getBytes());
		return Keys.hmacShaKeyFor(encodedKey.getBytes());
	}
}
