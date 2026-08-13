package com.kkirok.server.domain.notification.application.service;

import com.kkirok.server.domain.notification.dao.NotificationDispatchLogRepository;
import com.kkirok.server.domain.notification.domain.NotificationDispatchLog;
import com.kkirok.server.domain.notification.domain.NotificationType;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
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

    // 후보가 많을 때 건별 existsBy 대신 한 번의 IN 조회로 이미 발송된 suffix를 걸러낸다.
    @Transactional(readOnly = true)
    public Set<String> findAlreadyDispatchedSuffixes(NotificationType type, Collection<String> suffixes) {
        if (suffixes.isEmpty()) {
            return Set.of();
        }
        // 서로 다른 target이 동일한 suffix를 만들더라도(현재는 발생하지 않지만) 예외 없이 하나만 남긴다.
        Map<String, String> suffixByDispatchKey = suffixes.stream()
                .collect(Collectors.toMap(
                        suffix -> NotificationDispatchLog.createDispatchKey(type, suffix),
                        suffix -> suffix,
                        (a, b) -> a));
        return dispatchLogRepository.findExistingDispatchKeys(suffixByDispatchKey.keySet()).stream()
                .map(suffixByDispatchKey::get)
                .collect(Collectors.toSet());
    }
}
