-- flyway:executeInTransaction=false
-- CREATE INDEX CONCURRENTLY는 트랜잭션 안에서 실행할 수 없어 executeInTransaction=false로 트랜잭션 밖에서 실행합니다.
-- PostgreSQL 전용 문법(CONCURRENTLY, 부분 인덱스)이라 db/migration-postgresql에 위치하며 dev/prod에만 적용되고 test(H2)에는 적용되지 않습니다.
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_notification_pending_delivery
ON notification (type, created_at)
WHERE sent_at IS NULL;
