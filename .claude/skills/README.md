# GitHub issue skills (Wubiq)

A pipeline that turns a raw GitHub issue into tested, reviewed code. The GitHub
issue is the source of truth throughout — every skill reads its state from the
spec file on `master` and from GitHub (PRs, labels, issue comments), never from chat
context, so any step can be re-run in a fresh session.

## Applications

Wubiq is one repository holding several applications. `.claude/scripts/apps.tsv` is the
registry the pipeline reads — nothing else lists them:

| Code | Directory | Toolchain | Commit and PR scope | Depends on | Headless tests |
|---|---|---|---|---|---|
| `COM` | `wubiq-common` | `java-ant` | `common` | — | none (tested from `wubiq-client/tests-src`) |
| `WIT` | `wubiq-dotmatrix-witness` | `java-ant` | `witness` | — | `tests-src` |
| `CLI` | `wubiq-client` | `java-ant` | `client` | `COM`, `WIT` | `tests-src` |
| `SRV` | `wubiq-server` | `java-ant` | `server` | `COM`, `CLI` | none |
| `INS` | `wubiq-installer` | `java-ant` | `installer` | `COM`, `CLI` | none |
| `PJB` | `wubiq-printerjob` | `java-ant` | `printerjob` | — | none |
| `AND` | `wubiq-android-gradle` | `android-gradle` | `android` | — | `app/src/test/java` |

`wubiq-test`, `wubiq-android-test` and `wubiq-assets` are not applications: nothing
compiles or tests them, and a change there affects no gate.

Registry columns, tab-separated, `-` for none; rows are in dependency order:

| Column | Holds |
|---|---|
| `code` | two or three capital letters; the prefix of the application's section ids |
| `dir` | the application's directory |
| `toolchain` | selects the spec template, `shared/improve-issue/templates/<toolchain>.md` |
| `jdk` | a major version found through `wubiq-client/jdk-homes.sh`; `android` (`WUBIQ_ANDROID_JAVA_HOME`, else Android Studio's JDK, else the newest installed); or `-` when the toolchain needs no JDK |
| `deps` | codes it compiles against, comma-separated |
| `compile` | compiles sources and tests from empty output directories; run in `dir` with `JAVA_HOME` set |
| `test` | runs every headless test |
| `single-test` | runs the tests that replace `{}` |

**Adding an application** — in any language — is one registry row, after the rows it
depends on, plus a `templates/<toolchain>.md` when its toolchain is new. `gate.sh`,
`suite.sh` and `section-test.sh` only run what the row says, so a Go application's row
carries `go build ./... && go vet ./...`, `go test ./...` and `go test -run {} ./...`,
with `-` as its `jdk`.

## The sectioned model

`improve-issue` partitions an issue into **sections**, each self-contained and atomic:
merged alone on top of its dependencies, every affected application compiles and the
full suite stays green.

- A section is `DOC-1` (Documentation) or `<APP>-<k>` — `CLI-1`, `AND-2` — and belongs
  to exactly one application: it edits files only under that application's directory.
  An issue that spans applications has sections in each.
- Order follows the registry: `DOC-1` first, then a section depends on this issue's
  sections in the applications its own application depends on (`COM` before `CLI`
  before `SRV` and `INS`).
- Documentation leads structurally. `DOC-1` is one section per issue carrying every doc
  edit it owes — the `CHANGELOG.md` entry and README changes — and its PR merges
  **before** any code section starts.
- Nothing is stored unapproved. `improve-issue` presents the exact spec and stores it
  only after a human approves it — every run, re-runs included. A non-interactive
  spawn cannot approve, so it halts with `STATUS: SPEC_PENDING` and writes nothing.
- One PR per section, on branch `<type>/<n>-<sec>-<slug>` (`feat/52-cli-1-jlink-runtime`),
  titled `<type>(<scope>): … (#<n> · <SEC-ID>)`. A section may not start until every
  dependency's PR is **merged**. Section status is derived from GitHub (the PR on the
  section's branch), never cached in the issue body.
- There is no sizing, no splitting, no child issues. More work = more sections.
- No open questions: every ambiguity is resolved with the user before the spec is
  written.
- Spec depth is fixed at the `normal` coder, whatever complexity level the run is
  invoked at. A `high` or `max` issue buys a stronger coder, never a shallower spec.
- One **complexity level** per issue — `normal` (default), `high`, `max` — set by the
  caller on the first `improve-issue` run and persisted in the spec, so every later
  step resolves the same models in any session. `shared/coder-model.md` is the registry.

## Skills

| Skill | Purpose |
|---|---|
| `spec-load` | Reads an issue's spec — or one section of it — from `specs/issue-<n>-enriched-spec.md` on `master`. The only fence extractor |
| `spec-store` | Stores that spec file through branch → PR → merge (`spec-store-write.sh`) and regenerates the body's Section Map mirror; owns the merged-section guard. Also `tick`: a local checkbox edit in the section worktree |
| `improve-issue` | Partitions and enriches an issue into the sectioned spec; writes only what a human approved, through `spec-store`. **Owns the spec's content** |
| `code-issue` | Implements one section test-first on its own branch. Posts the `section-log` comment |
| `review-issue` | Verifies one section's ACs by evidence, runs the gate and the full suite, maintains the review table. **Owns the review table and the completion phrase** |
| `pr-from-issue` | Opens one reviewed section's PR — owns its branch, title and issue reference; delegates the rest to `do-pr` |
| `do-pr` | Gate, suite, push, and a PR with a generated description; honours gate receipts |
| `address-pr-review` | Captures a section PR's unresolved review threads as a `review-feedback` report on the issue. Changes no code, resolves nothing it captured |
| `merge-pr` | Merges one PR a human labelled `approved` — or one spec PR, which needs no label (see below) — and cleans up after it: refreshes `origin/master` and local `master`, retires the section's worktree, deletes the merged section branch. Reads the review labels, writes none |
| `orchestrate-issue` | Drives an issue's sections end to end, respecting merged-dependency gating; merges an `approved` PR and runs an `amends-required` PR's feedback back through the pipeline |
| `gate` | Runs `gate-fix`, then `gate-commit` if clean |
| `gate-fix` | Runs the gate and fixes mechanical compile errors; emits a Refactor Plan for the rest |
| `gate-commit` | Groups the working tree into conventional commits; refuses unless the gate is clean |
| `sync-pipeline-agents` | User-only: regenerates the pipeline's agents after an edit to the model registry |

## Agents, scripts and the hook

`.claude/agents/epic-orchestrator.md` is the agent `orchestrate-issue` spawns to drive
one wave of sections through code → review → PR → merge. It spawns each step as a
`pipeline-worker-<level>-<group>` agent (`<level>`: `normal`, `high`, `max`; `<group>`:
`spec-review`, `coder`, `ship`). Those nine agents carry the exact model and effort of
one cell of `shared/coder-model.md`; they are generated by
`.claude/scripts/pipeline-agents.sh` and never edited.

| Script | Does |
|---|---|
| `gate.sh` | The gate: compiles every application the diff affects, with its dependents. Prints `STAGE: <APP>:compile` on failure, `SKIPPED: no application affected` for a Markdown- or spec-only diff. `--all` compiles everything |
| `suite.sh` | The full suite: every application's headless tests |
| `section-test.sh <APP> <test>` | One targeted test, counted against a per-branch budget |
| `wait-verdict.sh <gate\|suite\|spec-<n>>` | The one way to wait for a backgrounded gate, suite or spec store |
| `spec-store-write.sh` | Stores a spec: write phase, then `--finish` after `merge-pr` |
| `provision-worktree.sh` | Creates a section's worktree |
| `pipeline-agents.sh <check\|generate>` | Keeps the agents equal to the model registry |
| `apps.sh`, `javac-check.sh` | Registry reader, and the compile used by applications whose Ant build cannot compile without packaging |

`.claude/hooks/guard-long-commands.sh` rejects a shell-backgrounded gate, suite or spec
store, a full Gradle test run outside `suite.sh`, and poll loops.

`shared/` is not a skill. It holds `house-conventions.md`, `escalation.md`,
`gate-receipt.md`, `background-long-commands.md`, `turn-discipline.md`,
`retire-worktree.md`, `coder-model.md` — the model registry: one complexity level maps
to a set of four cells, one per skill group (Coordination, Spec/Review, Coder, Ship) —
and the orchestrator's and `improve-issue`'s own reference files.

## The pipeline

```
/skill improve-issue <n> [level]    # writes the sectioned spec; sets the complexity level
/skill code-issue <n> <SEC-ID>      # implements one section (red tests, code, green)
/skill review-issue <n> <SEC-ID>    # reviews that section
/skill pr-from-issue <n> <SEC-ID>   # opens the section's PR
                                    # — a human reviews it and labels it `approved` —
/skill merge-pr <n> <SEC-ID>        # merges it and cleans up; also takes a bare PR number
```

The label between the last two lines is the only step no skill performs. Every
other transition is derived from GitHub, so the pipeline never has to ask
whether something happened — it looks.

A section's PR references the issue as `Part of #<n> · <SEC-ID>`, never
`Closes`, until `review-issue` has written the completion phrase — otherwise the
first section to merge would close an issue whose remaining sections are not
written yet.

`[level]` is `normal` (default), `high`, or `max`, and is needed only on the
first `improve-issue` run — the later steps read it from the spec block. Passing
it to `code-issue` or `review-issue` is a one-off override, never written back.

Repeat per section, in Section Map order. Sections with no dependency between
them may run in parallel; a section starts only when its dependencies are
merged.

## The review loop

A reviewer who wants changes labels the section's PR `amends-required` — a
human-only label (`shared/house-conventions.md` H-6). From there:

```
/skill address-pr-review <n> <SEC-ID>   # reports the threads on the issue; changes nothing
/skill improve-issue <n>                # absorbs each item into the spec, or declines it on the record
/skill code-issue <n> <SEC-ID>          # amend resume: implements the resolution lines, answers and resolves the threads
/skill review-issue <n> <SEC-ID>        # re-verifies on the open PR
/skill pr-from-issue <n> <SEC-ID>       # refreshes the same PR
```

The reviewer then removes `amends-required` and adds `approved` — both human
acts — and `merge-pr` lands it. `merge-pr` refuses a PR that carries
`amends-required`, even if `approved` is also present; that is how a reviewer
re-blocks a PR they had already approved. (The repository has no approval
workflow; the rule is enforced by the skill, not by a required check.)

`orchestrate-issue` runs this unattended, stopping only for the `improve-issue`
approval; while every section is waiting on something outside the run (a review
label, a re-review, a dependency) it pauses with a Continue / Stop
question rather than ending. It never asks whether a PR was merged — merge state
is derived from `gh pr list`, and a merge the run performs is an act, not a
question. Each **section** gets one git worktree, with its own build output, and it lives until that section's PR
merges — the section's steps hand uncommitted work to one another through that
tree, so retiring it between them would strand the work. `merge-pr` retires the
worktree and deletes the section's local branch together, as its own
after-merge cleanup, once the PR is confirmed merged — so a human who merges
outside the pipeline (standalone `merge-pr`, or a merge done on GitHub's UI)
still gets both, not just the branch. The
thread is answered by the spec and closed by the code:
`address-pr-review` resolves nothing it captured; `code-issue` resolves every
captured thread — implemented or declined — once the work exists. An item is
absorbed into the section whose PR raised it or declined on the record; it
never moves to a sibling, because sections are atomic.

## Marker blocks

**The spec is a file, not a body.** It lives at
`specs/issue-<n>-enriched-spec.md`, version-controlled, because a real spec
outgrows what a GitHub issue body accepts. Two
sub-skills own the transport, and nothing else may bypass them:
`spec-load` reads (always from `master`), `spec-store` writes.

| Block | Written by | Read by |
|---|---|---|
| `specs/issue-<n>-enriched-spec.md` — the spec itself, sections and ACs | `spec-store` (owner) — on behalf of `improve-issue` (whole spec, through a spec PR) and `code-issue` / `review-issue` (AC ticks, local edit in the section worktree) | `spec-load`, and through it every pipeline skill |
| `enriched-spec` body block — pointer + Section Map **mirror** | `spec-store` (owner), regenerated on every write | **nobody.** The mirror is for humans scanning GitHub; a skill that read it could act on a hand-edit |
| `**Complexity:** <level>` (in the spec file) | `improve-issue` (owner) | `code-issue`, `review-issue`, `pr-from-issue`, `epic-orchestrator` |
| `<!-- section:<SEC-ID>:start/end -->` fences (in the spec file) | `improve-issue` (owner) | `spec-load` — the single canonical extractor |
| `<!-- section-log #<n> <SEC-ID> -->` comment | `code-issue` | `review-issue` |
| `<!-- spec-defect #<n> <SEC-ID> -->` comment | `code-issue` | `improve-issue` |
| `<!-- code-issue-gap #<n> <SEC-ID> -->` comment | `code-issue` (owner) — posted on a mid-implementation-defect **keep** decision, and as a checkpoint when a spawn exhausts its targeted-test budget | `code-issue` itself, on the next run (preflight row 8) |
| `<!-- review-feedback #<n> <SEC-ID> -->` comment | `address-pr-review` | `improve-issue`, `code-issue` (thread ids to answer), `epic-orchestrator` |
| `<!-- review-feedback-resolved <id> -->` comment, one `- item k → …` line per item | `improve-issue` | `code-issue` (amend work list, replies), `epic-orchestrator` (`shared/orchestrator/amend-routing.md`) |
| `<!-- review-table #<n> -->` comment (one per issue, edited in place) | `review-issue` (owner) | `code-issue` (gaps resume), `pr-from-issue`, `orchestrate-issue` |
| `Issue is completed and ready to be closed.` (body phrase) | `review-issue` (owner) | `pr-from-issue` (decides `Closes` vs `Part of`), `orchestrate-issue` |

`code-issue` may edit exactly one thing in the spec: the AC checkboxes of the
section it implements; `review-issue` may additionally tick a checkbox it has
proven met. Both go through `spec-store <n> tick`, which is a **local edit** of
the spec file in the section's worktree — no API call, no PR — and permits only
`- [ ]` → `- [x]` flips inside that section's fence. The tick reaches `master` in
the section's own code PR, committed by `gate-commit`. `spec-load` reads `master`
for structure and overlays only the working copy's ticks for the section in
hand. Closing-mode `review-issue` does not tick (the sections are merged and
the merged-section guard would refuse). Everything else belongs to
`improve-issue`. A section whose PR is merged is history — never rewritten.

**Nothing is ever written to `master` directly.** The repository's ruleset requires a
pull request, so a spec is stored in three steps: `spec-store-write.sh` creates branch
`spec/<n>-enriched-spec`, opens a non-draft PR, waits for its checks (none run today, so
it waits only for GitHub to report the PR mergeable) and prints
`STATUS: OK-PR-READY <pr>`; the caller runs `merge-pr <pr>`; then
`spec-store-write.sh <n> --finish <pr>` refreshes local `master`, regenerates the mirror
and posts the resolution comment. Only its `STATUS: OK <sha>` means the spec is stored.
Terminal statuses are listed in `shared/escalation.md`.

The spec file is public, like the rest of the repository: write nothing into a spec
that should not be read by anyone.

### Spec PR convention

`merge-pr` skips its `approved`-label check (Step 0 row 5) for a **spec PR**,
recognised by **all** of:

- head branch matches `^spec/[0-9]+-enriched-spec$`,
- title starts with `docs(spec): enriched spec for issue #`,
- every file in the PR is exactly `specs/issue-<n>-enriched-spec.md`, with the
  same `<n>` as the branch.

If any condition fails the PR is an ordinary PR and needs `approved`. Every
other row still applies (not draft, base `master`, no `amends-required`, no failing or
pending check, mergeable). `merge-pr` still never writes `approved` or
`amends-required` (H-6). The orchestrator ignores `spec/*` heads when it
classifies PRs. If an approval workflow is ever added it needs the same
exemption and must not be a required check without it.
