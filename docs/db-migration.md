# DB 마이그레이션 규칙 (Flyway)

## 배경

dev/prod 모두 기존에는 `ddl-auto: update`(dev) / `none`(prod)로 엔티티 기반 자동 스키마 관리를 사용했습니다.
스키마 변경 이력을 코드처럼 버전 관리하고, 환경 간 스키마 상태를 동일하게 보장하기 위해 Flyway를 도입합니다.

## 파일 위치

`src/main/resources/db/migration/`

## 네이밍 규칙

`V{yyyyMMddHHmmss}__{snake_case_description}.sql`

- 버전은 타임스탬프(`yyyyMMddHHmmss`) 기반으로 부여합니다. 순차 번호(V1, V2 ...) 대신 타임스탬프를 쓰는 이유는 여러 브랜치에서 동시에 마이그레이션을 추가해도 버전 충돌이 나지 않기 때문입니다.
- 설명(description)은 영어 snake_case로, 무엇을 하는지 동사로 시작해 간결하게 작성합니다.
  - 예: `V20260813153000__create_member_table.sql`, `V20260814090000__add_nickname_column_to_user.sql`
- 파일 하나당 논리적으로 하나의 변경만 담습니다. (테이블 생성 1개, 컬럼 추가 1개 등)

## 불변성 원칙

- `develop`에 병합된 마이그레이션 파일은 **절대 수정하지 않습니다.** Flyway는 적용된 파일의 체크섬을 검증하므로, 병합 후 수정하면 다른 환경에서 마이그레이션이 실패합니다.
- 잘못된 마이그레이션을 고쳐야 한다면 그 파일을 고치지 말고, 되돌리는 새 마이그레이션 파일을 추가합니다.
- PR 리뷰 중(병합 전)에는 같은 PR 내에서 파일을 수정해도 됩니다.

## 기존 스키마 처리 (Baseline)

dev/prod DB에는 이미 `ddl-auto`로 생성된 스키마와 데이터가 존재합니다. 이를 덤프해서 V1으로 만드는 대신 baseline 방식을 사용합니다.

- `spring.flyway.baseline-on-migrate: true`
- `spring.flyway.baseline-version: 1`

스키마 히스토리 테이블(`flyway_schema_history`)이 없는 상태에서 Flyway가 처음 실행될 때:
- 스키마가 비어있지 않으면(dev/prod처럼 기존 테이블이 있으면) 버전 1을 baseline으로 기록하고, 버전 1보다 큰 마이그레이션부터 적용합니다.
- 스키마가 비어있으면(신규 환경, 테스트 H2 등) baseline 없이 V1부터 순서대로 전체 마이그레이션을 적용합니다.

따라서 새로 작성하는 첫 마이그레이션 파일의 버전(타임스탬프)은 항상 `1`보다 크므로 별도 조정 없이 그대로 사용하면 됩니다.

## `ddl-auto` 정책

Flyway 도입 이후 모든 프로파일(dev/prod/test)에서 `ddl-auto: validate`로 통일합니다.

- Flyway가 스키마의 유일한 소스(source of truth)가 되고, Hibernate는 엔티티와 실제 스키마가 일치하는지 검증만 합니다.
- 엔티티를 변경했는데 대응하는 마이그레이션 파일을 추가하지 않으면 애플리케이션 구동 시 `validate` 단계에서 즉시 실패합니다. 이 실패가 "마이그레이션 파일을 빠뜨렸다"는 신호입니다.

## 테스트(H2) 전략

테스트 프로파일은 `db/migration`(공용) 마이그레이션만 적용합니다 (H2, `MODE=PostgreSQL`).

- 장점: 실제 스키마와 테스트 스키마가 항상 동일하게 유지됩니다.
- 주의: H2의 PostgreSQL 호환 모드가 모든 PostgreSQL 전용 문법(예: `CONCURRENTLY`, 부분 인덱스, `JSONB`, `gen_random_uuid()`)을 지원하지는 않습니다. 표준 SQL로 작성 가능한 마이그레이션은 `db/migration`에 두고, PostgreSQL 전용 문법이 꼭 필요한 마이그레이션은 아래 "PostgreSQL 전용 마이그레이션" 규칙을 따릅니다.

## PostgreSQL 전용 마이그레이션

H2가 지원하지 않는 PostgreSQL 전용 문법(`CREATE INDEX CONCURRENTLY`, 부분 인덱스 등)을 쓰는 마이그레이션은 `src/main/resources/db/migration-postgresql/`에 작성합니다.

- 이 디렉토리는 dev/prod의 `spring.flyway.locations`에만 포함되어 있고, test(H2)에는 포함되지 않습니다.
- 파일명 규칙은 `db/migration`과 동일합니다 (`V{yyyyMMddHHmmss}__{snake_case_description}.sql`). 버전은 전체 마이그레이션 히스토리에서 하나로 합쳐져 순서대로 적용되므로, 두 디렉토리 간 타임스탬프만 겹치지 않으면 됩니다.
- `CREATE INDEX CONCURRENTLY`처럼 트랜잭션 안에서 실행할 수 없는 문은 파일 첫 줄에 `-- flyway:executeInTransaction=false`를 추가해 해당 마이그레이션을 트랜잭션 밖에서 실행합니다.

## PR 워크플로우

- 엔티티 구조를 변경하는 PR에는 반드시 대응하는 마이그레이션 파일을 함께 포함합니다.
- 마이그레이션 파일은 코드 리뷰 대상입니다. (컬럼 삭제/타입 변경 등 파괴적 변경은 특히 주의 깊게 리뷰)
- 마이그레이션 실행은 애플리케이션 기동 시 Flyway가 자동으로 수행합니다(`spring.flyway.locations: classpath:db/migration`). 별도의 수동 실행 스크립트는 두지 않습니다.

## 롤백 전략

Flyway Community 버전은 자동 undo를 지원하지 않습니다. 롤백이 필요하면 변경을 되돌리는 새 마이그레이션 파일을 추가하는 방식(forward-only)을 원칙으로 합니다.
