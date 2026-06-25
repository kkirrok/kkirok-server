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
- Resolves all design/content decisions itself — the Task Spec must not leave open choices for Codex to interpret
- Writes a **Task Spec** to hand off to Codex, including:
  - Implementation goal (What)
  - Target files to modify (Where)
  - Exact before/after text for each change (How)
  - Completion criteria (verification checks), scoped to what the change actually requires
- Specifies which items from AGENTS.md should be applied

**Token-efficiency rules for the spec** (so Codex doesn't burn tokens re-deriving what Claude already knows):
- Give the exact file path and an anchor (line number or unique surrounding text) for every change. Codex should not need to search or grep to locate it.
- Provide literal old → new text (a diff-like block) instead of prose descriptions whenever the change is concrete text/code. Prose forces Codex to re-derive exact wording and risks drifting from what Claude intended.
- Do not ask Codex to "explore", "check the codebase for X", or "decide" — do that research yourself before writing the spec.
- Only include verification steps that apply to the change (e.g. omit `./gradlew build`/test steps for non-code resource or text file edits).
- Keep the spec to what changed — do not restate unrelated existing rules/content unless they must change.

**Output**: a Task Spec in the following format

```
## Task Spec

### Goal
[One-sentence description of the task]

### Files to Modify
- `path/file.ext` - [one-line summary of the change]

### Exact Changes
`path/file.ext` (around line N)
- Before:
  ```
  [exact existing text]
  ```
- After:
  ```
  [exact replacement text]
  ```

### Completion Criteria
- [ ] [Verifiable checklist item]
- [ ] Existing tests pass (omit if not applicable to this change)
- [ ] New tests added, if applicable
```

---

#### Stage 2 - Codex Implementation
Codex receives Claude's Task Spec and implements it.

- Apply the **Exact Changes** blocks verbatim; do not re-explore the file or second-guess the wording
- If the file content doesn't match the "Before" block, stop and report the mismatch instead of guessing
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
