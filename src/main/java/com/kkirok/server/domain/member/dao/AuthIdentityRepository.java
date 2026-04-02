package com.kkirok.server.domain.member.dao;

import com.kkirok.server.domain.member.domain.AuthIdentity;
import com.kkirok.server.domain.member.domain.AuthProvider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AuthIdentityRepository extends JpaRepository<AuthIdentity, Long> {

    Optional<AuthIdentity> findByProviderAndProviderUserId(AuthProvider provider, String providerUserId);

    boolean existsByProviderAndProviderUserId(AuthProvider provider, String providerUserId);

    boolean existsByMemberEmailAndProviderNot(String email, AuthProvider provider);
}
