package com.kkirok.server.domain.user.dao;

import com.kkirok.server.domain.user.domain.Users;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<Users, Long> {
}
