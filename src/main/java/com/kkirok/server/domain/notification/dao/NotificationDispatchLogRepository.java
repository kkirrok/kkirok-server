package com.kkirok.server.domain.notification.dao;

import com.kkirok.server.domain.notification.domain.NotificationDispatchLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationDispatchLogRepository extends JpaRepository<NotificationDispatchLog, Long> {

    boolean existsByDispatchKey(String dispatchKey);
}
