package com.kkirok.server.domain.notification.dao;

import com.kkirok.server.domain.notification.domain.Device;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeviceRepository extends JpaRepository<Device, Long> {

    List<Device> findAllByMember_IdIn(Collection<Long> memberIds);

    Optional<Device> findByToken(String token);

    void deleteByTokenIn(Collection<String> tokens);
}
