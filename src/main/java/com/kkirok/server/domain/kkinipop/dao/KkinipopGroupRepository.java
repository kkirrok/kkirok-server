package com.kkirok.server.domain.kkinipop.dao;

import com.kkirok.server.domain.kkinipop.domain.KkinipopGroup;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KkinipopGroupRepository extends JpaRepository<KkinipopGroup, Long> {

    boolean existsByInviteCode(String inviteCode);

    Optional<KkinipopGroup> findByInviteCode(String inviteCode);
}
