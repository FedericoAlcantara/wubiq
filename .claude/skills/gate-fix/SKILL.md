---
name: gate-fix
description: Run the gate and fix issues. Mechanical issues are fixed in place; refactor-class issues produce a plan for user approval instead of being attempted.
---

Run the gate and resolve issues until it passes, OR until the only
remaining issues require a refactor — in which case emit a Refactor Plan and
stop.

Follows `../shared/turn-discipline.md` throughout: never poll a backgrounded
`gate.sh` run, no narration-only turns.

## Background

`.claude/scripts/gate.sh` compiles every application the diff affects — sources and
tests, from empty output directories — in the dependency order of
`.claude/scripts/apps.tsv`. It is fail-fast: one stage per application, and only the
FIRST failing stage is visible per run. Fix that stage, re-run, repeat.

There is no formatter, linter or static analysis in this repository: the gate is
compilation, with the JDK and flags each application's registry row names.

## Terminal status

This skill always **returns** exactly one of these statuses to its caller,
printed on its own line so the orchestrator can branch on it:

- `STATUS: CLEAN` — the gate exited 0.
- `STATUS: PLAN_PENDING` — every mechanical issue you could fix is fixed, but
  one or more refactor-class issues remain. A **Refactor Plan** section was
  printed for the user to approve.
- `STATUS: BLOCKED` — 3-iteration cap reached or an unrecoverable failure.

## 1. Run

```
.claude/scripts/gate.sh
```

Run it per `../shared/background-long-commands.md` — `run_in_background`, then
one foreground `.claude/scripts/wait-verdict.sh gate`. It prints only the failing
stage's last 40 log lines (the full log is `/tmp/gate-<branch>.log`).

If the verdict is `EXIT: 0` — including `SKIPPED: no application affected` (a
diff made only of Markdown and specs cannot change a compile) — print
`STATUS: CLEAN` and return.

## 2. Identify the failing stage

`gate.sh` prints `STAGE: <name>` on the failure — read that line rather than
scanning output. It is `<APP>:compile`, `<APP>` a code from `apps.tsv` (`CLI:compile`,
`AND:compile`), or `pipeline:syntax`.

The failing application may not be the one the diff edited: a change in `wubiq-common`
is compiled against by `wubiq-client`, `wubiq-server` and `wubiq-installer`, and a
signature change shows up as their stage.

## 3. Stage-keyed action table

Apply the action for the failing stage. Then go back to step 1.

| Failing stage | Mechanical action | Refactor-class fallback |
|---|---|---|
| `<APP>:compile` | If every error is one of {missing or unused import, a caller not updated to a signature this diff changed, a missing `@Override` target after a rename in this diff, a checked exception this diff added and a caller must declare or wrap, a typo in an identifier this diff introduced} → fix mechanically, in the file the compiler names. | Any other error → record in Refactor Plan. In particular: syntax or an API above the module's language level (the fix is a different construct, a design choice), a missing class the spec expected to exist, an incompatible type that needs a decision about which side is right. Never change logic to make a compile pass. |
| `<APP>:compile`, the log shows no compiler error | The build itself failed: no JDK found (`No JDK … was found`), a missing `local.properties`, Gradle could not start. Not a code problem → `STATUS: BLOCKED`, quoting the log's last lines. | n/a |
| `pipeline:syntax` | A `.claude/**/*.sh` file in the diff fails `bash -n`. Fix the syntax error the log names. | n/a |

After applying the mechanical action for a stage, go back to step 1.

When the *currently failing* stage yields only refactor-class issues, stop
the loop immediately and go to step 5 — do **not** keep re-running
`gate.sh` hoping a later stage surfaces. The pipeline is fail-fast
and the same stage will keep failing until the user resolves the plan.
Continue collecting issues across re-runs only when a previous run *did*
make mechanical progress and the next run reveals new refactor-class
findings in a different stage.

## 4. Iteration cap

If `gate.sh` has been run 3 times without reaching exit 0:

- If any refactor-class issues were collected → go to step 5 and emit
  `STATUS: PLAN_PENDING`.
- Otherwise → print `STATUS: BLOCKED` with what is left.

## 5. Refactor Plan output

The Refactor Plan IS the proposed coding change. Write it as if the user
will hand it to another engineer (or to `code-issue`) to execute — concrete
enough to act on, not a summary.

When refactor-class issues remain, end with:

```
## Refactor Plan

### <file>:<line> — <short title>
**Stage:** <stage name>
**Tool output (gate.sh's last-40-line excerpt for this stage):**
<paste what gate.sh printed>

**Root cause:** <1-2 sentences naming the underlying design/code issue,
not just the symptom>

**Proposed refactor:**
- <step 1: concrete code change — module/function, what to add/remove/rename>
- <step 2: ...>
- <step N: ...>

**Files touched:** <list of file paths, including new files>
**Tests to add or update:** <test files + what they assert>
**Risk/scope:** <e.g. "protocol key in wubiq-common", "other applications compile against it", "test-only">
**Out of scope (intentionally not changed):** <anything the reader might
expect to be touched but won't be>

### ... (one block per finding)

STATUS: PLAN_PENDING
```

Do not edit code to implement the plan — that is the user's call. After the
user acts on the plan, re-running this skill picks up cleanly.

## Returning to the caller

This skill is normally invoked by another skill (`gate`), never on its own. The
`STATUS:` line is a **handoff, not a conclusion** — print it and continue in the
**same turn** with whatever step the caller has next. Do not end your turn and
do not wait for the user, on any of the three statuses. Only the outermost skill
in the chain decides when the turn is over.

`STATUS: PLAN_PENDING` is the one exception the caller itself handles: `gate`
stops there so the user can approve the Refactor Plan. That stop belongs to
`gate`, not to this skill — still return the status rather than ending the turn
yourself.

## Forbidden

- Ending the turn after printing a `STATUS:` line instead of returning to the caller.
- Backgrounding or polling `gate.sh` outside
  `../shared/background-long-commands.md`.
- Modifying code logic to silence a warning.
- Raising a module's language level, or adding a jar, to make a compile pass.
- Running a default jar target (`create-common-jar`, `create-client-jar`, …) to "refresh" classes: they overwrite tracked jars.
- Emitting `STATUS: CLEAN` while any stage still fails.
- Emitting a Refactor Plan whose body is only a one-line summary.
- Exiting with `STATUS: BLOCKED` when refactor-class issues were collected
  during the run.
