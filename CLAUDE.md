# CLAUDE.md

## Project Overview

- **Project name**: Kkirok Server
- **Tech stack**: Java 17, Spring Boot 3.3.5, Gradle
- **Package root**: `com.kkirok.server`
- **Structure**: Domain-oriented packages (`domain/`, `admin/`, etc.) with layer separation (`api`, `application`, `domain`, `dao`, `exception`)
- **Testing**: JUnit-based unit tests plus integration tests (`IntegrationTestSupport`)

---

## Language Rule

- When communicating with the user, speak Korean only.

---

## Build & Test

```bash
# Build
./gradlew build

# Run tests
./gradlew test

# Run a specific test only
./gradlew test --tests "com.kkirok.server.domain.*.ServiceNameTest"
```

---

## Coding Convention Reference

See [AGENTS.md](./AGENTS.md) for the detailed coding guidelines.
