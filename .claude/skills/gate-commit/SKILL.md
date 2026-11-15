---
name: gate-commit
description: Group the working tree into conventional commits and commit them. Refuses to run unless the gate is currently clean.
---

Group the staged changes into conventional commits. This skill is gated: it
will not commit unless the gate currently passes.

Follows `../shared/turn-discipline.md` throughout: batch independent tool
calls, never poll, no narration-only turns.

## 1. Gate

This skill does not commit onto a red tree. There are two ways to establish that
it is green, and the receipt path exists only because `gate` has *just* run the
identical command.

**Receipt path.** If the invoking prompt carries a `CONSISTENCY_VERIFIED: <sha>`
line, compute `tree_hash` per `../shared/gate-receipt.md` and compare all 40
characters. On an exact match, skip the gate and say so:

```
Trusting CONSISTENCY_VERIFIED receipt for tree <short-sha> — skipping the gate.
```

Then go to step 2. Anything less than an exact match — no receipt line, a
malformed one, a different hash — falls through to the full run below. A
near-miss is a miss.

**Full path.** Otherwise — and this is always the case when a user invokes this
skill directly — run:

```
.claude/scripts/gate.sh
```

Run it per `../shared/background-long-commands.md` — `run_in_background`, then
one foreground `.claude/scripts/wait-verdict.sh gate`. A verdict of
`SKIPPED: docs/spec-only changes` then `EXIT: 0` is a pass.

If exit code is non-zero, abort with:

> the gate is failing. Run the `gate` skill (or `gate-fix`
> directly) before committing.

Do not stage, do not commit, do not retry.

The refusal guarantee in this skill's description is unchanged: invoked on its
own, with no receipt, it still runs the gate and still refuses on red.

## 2. Grouping rules

A **group** is a set of changed files that share one intent and become exactly
**one** commit. Assign every changed file to a group using the table below,
**first match wins** (check rows top to bottom). Each group is one commit whose
type is the `Type` column.

| # | A file belongs here when… | Type | Message shape |
|---|---|---|---|
| 1 | it is under `.claude/` (skills, agents, scripts, hooks, settings) | `chore` | `chore: <what changed>` |
| 2 | it is CI config (`.github/workflows/**`) | `ci` | `ci: <what changed>` |
| 3 | it is build or dependency config (`build.xml`, `*.gradle.kts`, `gradle/**`, `gradle.properties`, a jar under `lib/` or `libs/`, `docker-compose.yml`) | `build` | `build(<scope>): <what changed>` |
| 4 | it is a test file (anything under `tests-src/`, `app/src/test/`, `lib-test/`) | `test` | `test(<scope>): <details>` |
| 5 | it is `CHANGELOG.md`, or a README that documents the change | `docs` | `docs(<scope>): <what is documented>` |
| 6 | it adds new user-facing behavior | `feat` | `feat(<scope>): <why>` |
| 7 | it fixes a bug | `fix` | `fix(<scope>): <why>` |
| 8 | it improves performance without changing behavior | `perf` | `perf(<scope>): <why>` |
| 9 | it restructures code without changing behavior | `refactor` | `refactor(<scope>): <why>` |
| 10 | it changes only formatting/whitespace/style, no code meaning | `style` | `style(<scope>): <what changed>` |
| 11 | it is other maintenance/tooling (not covered above) | `chore` | `chore: <what changed>` |
| 12 | none of the above, or a file could plausibly fit two groups | — | **STOP and ask the user how to split.** |

`<scope>` is the application the group's files belong to — `common`, `witness`, `client`,
`server`, `installer`, `printerjob`, `android` (`../README.md` § Applications). Files of
two applications are two groups, one commit each; a group with no single application
(rows 1, 2, 11) takes no scope. A `CHANGELOG.md` hunk takes the scope of the
`### Wubiq-<module>` block it is in.

Rules that override the table:

- Test files (row 4) are **never** grouped with non-test files; one `test(<scope>):`
  group per application.
- Build output and rebuilt jars are never committed: `bin/`, `dist/`, `build/`,
  `build-tests/`, and `wubiq-android-gradle/app/libs/wubiq-*.jar` unless the task was to
  update them. One showing up in `git status` is a stop, not a group.
- `CHANGELOG.md` hunks are split so each commit carries its own lines when the file
  documents more than one group's change (`git add -p`).
- `specs/issue-<n>-enriched-spec.md` carrying only `- [ ]` → `- [x]` flips
  (a `spec-store tick`) travels in the section's own code commit group — it is
  part of the section's change, not a separate intent.
- When in doubt between two groups, do not guess — use row 12 and ask.

## 3. Stage and commit

For each group:

1. `git add <specific file>` — explicitly, one file at a time. Never
   `git add -A` / `git add .`.
2. Commit with a conventional message that describes the *why*, not the
   *what*. Pass the message via HEREDOC to preserve formatting.

If there is nothing to commit after the gate passes, report that in a short note
and return to the caller.

Every commit message ends with the `Co-Authored-By` trailer the harness
specifies (never invent one).

## Returning to the caller

This skill is normally invoked by another skill (`gate`), never on its own. The
closing report — commits created, or nothing to commit — is a **handoff, not a
conclusion**. Emit it and continue in the **same turn** with whatever step the
caller has next. Do not end your turn and do not wait for the user. Only the
outermost skill in the chain decides when the turn is over.

The one deliberate stop is row 12 of the grouping table: an unassignable or
ambiguous file requires the user's decision, so ask and wait. Every other path
returns.

## Forbidden

- Ending the turn after committing instead of returning to the caller.
- `--no-verify` on any git command.
- `git push --force` (this skill does not push).
- `git add -A` / `git add .`.
- Amending an existing commit.
- Committing while the gate is failing.
- Honouring a `CONSISTENCY_VERIFIED` receipt whose hash does not match the current
  tree, or matching it on a prefix rather than all 40 characters.
- Skipping the gate on anything other than an exact receipt match — a
  caller's assurance in prose is not a receipt.
