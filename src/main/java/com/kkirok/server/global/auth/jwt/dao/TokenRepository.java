package com.kkirok.server.global.auth.jwt.dao;


import com.kkirok.server.global.auth.redis.Token;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface TokenRepository extends CrudRepository<Token, Long> {

	Optional<Token> findByRefreshToken(final String refreshToken);
}
