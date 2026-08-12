# DB Migration (Flyway)

마이그레이션 파일 작성 규칙은 [`docs/db-migration.md`](../../../../../docs/db-migration.md) 를 참고하세요.

- 파일명: `V{yyyyMMddHHmmss}__{snake_case_description}.sql`
- 예: `V20260813153000__create_member_table.sql`
- 이 디렉토리의 파일은 병합 후 절대 수정하지 않습니다. 변경이 필요하면 새 마이그레이션 파일을 추가하세요.
- H2(test)가 지원하지 않는 PostgreSQL 전용 문법을 쓰는 마이그레이션은 이 디렉토리가 아니라 `../db/migration-postgresql/`에 작성하세요 (dev/prod에만 적용됨).
