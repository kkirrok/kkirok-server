package com.kkirok.server.domain.notification.dao;

import com.kkirok.server.domain.notification.domain.NotificationDispatchLog;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationDispatchLogRepository extends JpaRepository<NotificationDispatchLog, Long> {

    boolean existsByDispatchKey(String dispatchKey);

    @Query("select d.dispatchKey from NotificationDispatchLog d where d.dispatchKey in :dispatchKeys")
    List<String> findExistingDispatchKeys(@Param("dispatchKeys") Collection<String> dispatchKeys);
}
