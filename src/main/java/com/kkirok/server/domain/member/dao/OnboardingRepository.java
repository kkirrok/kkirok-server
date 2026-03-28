package com.kkirok.server.domain.member.dao;

import com.kkirok.server.domain.member.domain.Onboarding;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OnboardingRepository extends JpaRepository<Onboarding, Long> {
}
