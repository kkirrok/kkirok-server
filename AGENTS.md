# AGENTS.md

Behavioral guidelines to reduce common LLM coding mistakes. Merge with project-specific instructions as needed.

**Tradeoff:** These guidelines bias toward caution over speed. Use judgment for trivial tasks.

## 1. Think Before Coding

**Do not assume. Do not hide confusion. Surface tradeoffs.**

Before implementing:
- State your assumptions explicitly. If uncertain, ask.
- If multiple interpretations exist, present them instead of picking silently.
- If a simpler approach exists, say so. Push back when warranted.
- If something is unclear, stop. Name what is confusing. Ask.

## 2. Simplicity First

**Write the minimum code that solves the problem. Nothing speculative.**

- Do not add features beyond what was asked.
- Do not introduce abstractions for single-use code.
- Do not add flexibility or configurability that was not requested.
- Do not add error handling for impossible scenarios.
- If you write 200 lines and it could be 50, rewrite it.

Ask yourself: "Would a senior engineer say this is overcomplicated?" If yes, simplify.

## 3. Surgical Changes

**Touch only what you must. Clean up only your own mess.**

When editing existing code:
- Do not improve adjacent code, comments, or formatting.
- Do not refactor things that are not broken.
- Match the existing style, even if you would do it differently.
- If you notice unrelated dead code, mention it; do not delete it.

When your changes create orphans:
- Remove imports, variables, or functions that your changes made unused.
- Do not remove pre-existing dead code unless asked.

The test: every changed line should trace directly to the user's request.

## 4. Goal-Driven Execution

**Define success criteria. Loop until verified.**

Transform tasks into verifiable goals:
- "Add validation" -> "Write tests for invalid inputs, then make them pass"
- "Fix the bug" -> "Write a test that reproduces it, then make it pass"
- "Refactor X" -> "Ensure tests pass before and after"

For multi-step tasks, state a brief plan:
```
1. [Step] -> verify: [check]
2. [Step] -> verify: [check]
3. [Step] -> verify: [check]
```

Strong success criteria let you work independently. Weak criteria ("make it work") require constant clarification.

---

**These guidelines are working if:** there are fewer unnecessary changes in diffs, fewer rewrites due to overcomplication, and clarifying questions come before implementation rather than after mistakes.

---

## 5. Claude ↔ Codex Collaboration Workflow

> This project runs on a three-stage cycle: **Claude planning -> Codex implementation -> Claude review**.
> Codex must follow the rules below.

### What Codex Must Check Before Working

- Confirm that Claude's **Task Spec** exists (see CLAUDE.md)
- If there is no Task Spec, do not start implementation and ask Claude for one

### Codex Scope Limits

- Modify only the files and changes explicitly listed in the Task Spec
- Do not do extra refactoring "while you're here"
- If you find issues outside the Task Spec, do not implement them; report them as notes

### Codex Completion Report Format

After the work is complete, report in the following format:

```
## Completion Report

### Changed Files
- `path/file.java` - one-line summary of the change

### Completion Checklist
- [x] [completed item]
- [ ] [unfinished item and reason]

### Notes
[side effects, additional review items, etc.]
```

### Claude Review Trigger

When Codex submits its completion report, Claude automatically starts the review stage.
See CLAUDE.md for the review checklist.
