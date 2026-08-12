package com.kkirok.server.global.common.logging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;

@Slf4j
@Component
@Profile({"dev", "prod"})
@RequiredArgsConstructor
public class LogArchiveScheduler {

    private static final Path LOG_DIR = Paths.get("logs");
    private static final String ROLLED_OVER_SUFFIX = ".log.gz";
    private static final Duration STABLE_AFTER = Duration.ofSeconds(60);

    private final LogFileUploader logFileUploader;

    /** 매시 정각에 롤오버된 로그 파일들을 R2에 업로드하고, 성공한 파일은 로컬에서 삭제한다. */
    @Scheduled(cron = "0 0 * * * *")
    public void archiveRolledOverLogs() {
        File logDir = LOG_DIR.toFile();
        if (!logDir.isDirectory()) {
            return;
        }

        File[] rolledOverFiles = logDir.listFiles((dir, name) -> name.endsWith(ROLLED_OVER_SUFFIX));
        if (rolledOverFiles == null || rolledOverFiles.length == 0) {
            return;
        }

        for (File file : rolledOverFiles) {
            try {
                archiveFile(file);
            } catch (RuntimeException exception) {
                log.error("로그 파일 아카이빙 중 예상치 못한 오류: {}", file.getName(), exception);
            }
        }
    }

    private void archiveFile(final File file) {
        if (!isStable(file)) {
            return;
        }

        boolean uploaded = logFileUploader.upload(file);
        if (!uploaded) {
            return;
        }

        if (!file.delete()) {
            log.warn("업로드된 로그 파일 삭제 실패: {}", file.getName());
        }
    }

    /** 롤오버 직후 아직 압축 등으로 쓰기 중일 수 있으므로, 최근에 수정된 파일은 건너뛰고 다음 주기에 재시도한다. */
    private boolean isStable(final File file) {
        Instant lastModified = Instant.ofEpochMilli(file.lastModified());
        return Duration.between(lastModified, Instant.now()).compareTo(STABLE_AFTER) >= 0;
    }
}
