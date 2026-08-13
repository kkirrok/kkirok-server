package com.kkirok.server.domain.notification.application.service;

import com.kkirok.server.domain.notification.dao.NotificationDispatchLogRepository;
import com.kkirok.server.domain.notification.domain.NotificationDispatchLog;
import com.kkirok.server.domain.notification.domain.NotificationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationDispatchLogService {

    private final NotificationDispatchLogRepository dispatchLogRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW) // 내부 실패가 상위에 영향이 가지 않도록 함
    public boolean claim(NotificationType type, Long sourceId) {
        return claim(type, String.valueOf(sourceId));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW) // 내부 실패가 상위에 영향이 가지 않도록 함
    public boolean claim(NotificationType type, String suffix) {
        String dispatchKey = NotificationDispatchLog.createDispatchKey(type, suffix);
        if (dispatchLogRepository.existsByDispatchKey(dispatchKey)) {
            log.debug("Skipping duplicated notification dispatch for {}:{}", type, suffix);
            return false;
        }
        try {
            dispatchLogRepository.saveAndFlush(NotificationDispatchLog.of(type, suffix));
            return true;
        } catch (DataIntegrityViolationException e) {
            log.debug("Skipping duplicated notification dispatch for {}:{} (race)", type, suffix);
            return false;
        }
    }
}
