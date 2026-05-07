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

## Claude ↔ Codex Collaboration Workflow

This project is developed with a two-agent cycle: **Claude (planning/review) + Codex (implementation)**.

```
[1] Claude planning  →  [2] Codex implementation  →  [3] Claude review
     (define scope)       (write code)              (review & approve)
```

### Responsibilities by Stage

#### Stage 1 - Claude Planning
When Claude receives a task request, it does not write code directly. Instead, it:

- Analyzes the requirements and decides the implementation approach
- Asks the user to clarify any ambiguous points
- Writes a **Task Spec** to hand off to Codex, including:
  - Implementation goal (What)
  - Target files to modify or create (Where)
  - Concrete implementation steps (How)
  - Completion criteria (verification checks)
- Specifies which items from AGENTS.md should be applied

**Output**: a Task Spec in the following format

```
## Task Spec

### Goal
[One-sentence description of the task]

### Files to Modify
- `path/file.java` - [summary of the change]

### Implementation Guidance
1. [Concrete step]
2. [Concrete step]

### Completion Criteria
- [ ] [Verifiable checklist item]
- [ ] Existing tests pass
- [ ] New tests added, if applicable
```

---

#### Stage 2 - Codex Implementation
Codex receives Claude's Task Spec and implements it.

- Do not modify code outside the Task Spec scope
- Follow the AGENTS.md guidelines, especially Surgical Changes and Simplicity First
- Report a short summary of the changes after implementation

---

#### Stage 3 - Claude Review
Claude reviews Codex's result.

**Review checklist**
- [ ] Task Spec completion criteria are satisfied
- [ ] No unnecessary changes were made
- [ ] No excessive abstraction or complexity was introduced
- [ ] Existing code style remains consistent
- [ ] Tests pass
- [ ] No side effects or potential bugs were introduced

**Result**: Approve or Request Changes, with feedback

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
