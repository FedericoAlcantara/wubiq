---
name: review-issue
description: >
  Verify ONE section of an enriched-spec-v2 GitHub issue in Wubiq after
  code-issue implements it. Use this skill when the user says "review issue
  <n>", "review issue <n> <SEC-ID>", "review the section", "review the
  implementation", "check completeness", or right after code-issue finishes.
  Verifies each AC by evidence, proves the section's documentation scope and
  CHANGELOG entry, runs the full quality gate (gate.sh + suite.sh), checks
  the project rules the gate cannot see (language level, test shape, build
  output, dependencies, i18n, application boundary), maintains the one idempotent review table on the issue, and writes the
  issue-level completion phrase when every section is COMPLETED. Always run
  after code-issue, before pr-from-issue.
---

# Skill: review-issue

Close the loop on one section: prove its acceptance criteria are met, run the
gate `code-issue` deliberately does not run, and record the verdict in the
**review table** — the single, idempotent ground truth of review state. When
every section's row reads `COMPLETED`, this skill — and nothing else — writes
the issue-level phrase `Issue is completed and ready to be closed.`

This skill **assumes the spec is right**. It never judges an AC for being
thin, odd, or wrong — spec defects are `code-issue`'s channel
(`<!-- spec-defect -->` comments), not this skill's. An AC this skill cannot
confirm is reported as unmet and handed back to `code-issue`, which will
raise the spec defect itself if the AC is unexecutable as written.

## Non-negotiables

1. **The table and the phrase are the only ground.** Review state lives in
   the `<!-- review-table #<n> -->` comment (one per issue, edited in place,
   never duplicated) and the completion phrase in the issue body. No
   other body block, no per-run report comments, no scores.
2. **Never trust another skill's runs.** The gate (`gate.sh`) and the full
   suite (`suite.sh`) execute fresh in this run, every run — a receipt from
   `code-issue` (or an earlier review) is never honored
   (`../shared/gate-receipt.md`).
3. **Evidence-first, tick-trusting.** An AC fails only on *contradicting*
   evidence found by the light search of Step 2. No evidence found + ticked
   by `code-issue` = trusted. Absence of evidence is never a failure.
4. **The spec is read-only** except AC checkbox characters
   (`- [ ]` → `- [x]`), ticked for ACs proven met that `code-issue` missed —
   a local edit of the working copy through `spec-store tick`, never a write to
   `master`. Never untick.
5. **Never touch labels.** `approved` / `amends-required` are human-only
   gates (house-conventions H-6).
6. **Fix small, loop big.** A non-behavioral house-conventions hit is fixed
   on the branch and reported (Step 4). Anything touching behavior — including
   every blocking project flag of Step 4 — goes into the table as a failure
   note and back to `code-issue`; this skill never implements. One non-behavioral finding is deliberately excepted and
   loops instead: a Step 2 documentation-scope violation. Its "fix" would be
   to delete the offending edit, and that destroys the only evidence that
   the application's `DOC` section was incomplete.
7. **Every halt emits a terminal `STATUS: BLOCKED — <slug>` line**
   (`../shared/escalation.md`). Exit means exit: after the status line,
   write nothing and read nothing further.
8. **Never write a spec or a body you did not just fetch.** The only spec edit is
   `spec-store <n> tick` (Step 2): a local, checkbox-only edit of the working copy that
   travels to `master` in the section's own PR — never edit
   `specs/issue-<n>-enriched-spec.md` directly, never write it to `master`. Every `gh issue edit` this
   skill still makes — the completion phrase, and only that — is preceded, in
   the same block, by a `gh issue view` that succeeded and returned a non-empty
   body. Never append to or write back a scratch file from an earlier step or
   an earlier run: it may hold a different issue's body, and `--body-file`
   replaces the whole body with no undo. Scratch paths are namespaced per
   skill, issue, and section for the same reason.
9. **Turn discipline.** Follow `../shared/turn-discipline.md`: batch
   independent tool calls, never poll a background job, one edit per file per
   turn, no narration-only turns.

## Step 0 — Preflight

Read-only, cheap, complete. Check **every** row before reporting any.
Allowed costs: local `git` plumbing, one `gh issue view` (captured to a
file), `gh issue comment` listing, `gh pr list`, `grep`. Never a build or
test command, never a source file.

**When the invoking prompt carries a `SECTION_STATE:` block** — only
`epic-orchestrator` ever emits one — skip the fetch below entirely: row 1
passes on the block's say-so, row 4 reads its `Newest section-log:` line, row
5 reads its `PR:` line instead of probing. Every row runs in full, exactly as
written below — including the fetch — on any invocation without that block.

**Only when no `SECTION_STATE:` block was supplied:**

```bash
gh issue view <n> --json body --jq '.body' > /tmp/review-issue-<n>-body.md || {
  echo "⚠️ Could not read issue #<n>. Nothing was run."
  echo "STATUS: BLOCKED — preflight: issue-unreadable"; exit 1
}
[ -s /tmp/review-issue-<n>-body.md ] || {
  echo "⚠️ Issue #<n> returned an empty body. Refusing to guess."
  echo "STATUS: BLOCKED — preflight: issue-unreadable"; exit 1
}
gh api "repos/{owner}/{repo}/issues/<n>/comments" --paginate \
  --jq '.[] | {id: .id, body: .body}' > /tmp/review-issue-<n>-comments.json
```

With `SECTION_STATE:` present, neither file is written here — Step 1 fetches
`/tmp/review-issue-<n>-comments.json` itself, once, when it actually needs
comment bodies (the review-table text and the section-log's evidence table),
not just the timestamps and STATE cell `SECTION_STATE:` already carried.

1. **Issue readable.** The block above succeeded. **With `SECTION_STATE:`
   present, this row passes without a fetch** — `epic-orchestrator` read the
   issue this same pass to build the block. Slug: `issue-unreadable`.
2. **Enriched spec v2 present.**
   invoke `spec-load <n>`; a `STATUS: BLOCKED — spec-missing` fails this row. The
   spec lives at `specs/issue-<n>-enriched-spec.md`, not in the body. A v1-only
   body fails the same way. Slug: `enriched-spec-v2-missing`.
3. **Target section resolves.**
   - `<SEC-ID>` given: it must appear as a Section Map row and as a
     `<!-- section:<SEC-ID>:start -->` fence — the same exact marker Step 1
     extracts with, not a heading prefix. Slug: `section-unknown`.
   - No argument: auto-select the **first** Section Map row (map order) that
     has a `<!-- section-log #<n> <SEC-ID> -->` comment and whose review-table
     row is not `COMPLETED` (no table yet = no row is `COMPLETED`). If no row
     qualifies, report why each was skipped — **unless every row already
     reads `COMPLETED` with a merged PR, which is closing mode (Step 6)**.
     Slug: `no-reviewable-section`.
4. **Section log present.** A `<!-- section-log #<n> <SEC-ID> -->` comment
   exists for the target — `code-issue` finished this section. When several
   exist, the newest wins. **With `SECTION_STATE:` present**, this row reads
   its `Newest section-log:` line: anything but `none` is presence, no fetch
   needed. Slug: `section-log-missing`.
5. **Section not already delivered.** Same exact `--head` probe as
   `code-issue`, against the branch in the Section Map row:
   ```bash
   gh pr list --head "<branch-from-Section-Map>" --state all \
     --json number,state --jq '.[] | "\(.state) #\(.number)"'
   ```
   **With `SECTION_STATE:` present**, skip the probe: read its `PR:` line instead
   (`<state> #<num>` or `none`).

   `MERGED` hit → `section-delivered`. `OPEN` hit → `section-in-review`, a
   failure **unless** the newest `<!-- section-log #<n> <SEC-ID> -->` comment
   is newer than the review table's `updated_at` — then `code-issue` re-coded
   the section on its open PR after a review round (the amend loop:
   `address-pr-review` reported, `improve-issue` re-specced, `code-issue`
   implemented), and this run verifies it like any other section-log.

   **With `SECTION_STATE:` present, skip that comparison too — trust `OPEN`
   as passing.** `epic-orchestrator` spawns `review-issue` against an `OPEN`
   PR only via its Classify class `coded` or `amend-routing.md` row b, both of which fire
   precisely when the newest section-log is newer than the review table's
   `updated_at`; a spawn carrying `SECTION_STATE:` never reaches this row on
   a stale open PR. A manual invocation with no block still runs the fetch
   and the comparison above.

   **Never probe with `gh pr list --search "… in:title"`.** GitHub's search is
   a stemmed token AND, so `CLI-BE-1` matches `CLI-BE-10`; `--head` is an exact ref
   filter. `code-issue`'s row 5 carries the full reasoning.
6. **Section branch checks out.** The branch named in the Section Map row
   exists (locally or on origin); `git checkout <branch>` (fetch first).
   Slug: `branch-missing`.
7. **Complexity level resolves.** Read `**Complexity:**` from
   `/tmp/spec-<n>.md` and resolve this skill's model by the precedence in
   `../shared/coder-model.md`. A positional argument is a **one-off override** —
   announced, never written back. An unrecognised value fails (`unknown-level`);
   a v2 spec with no `**Complexity:**` line predates this contract and fails too
   (`complexity-missing`) — re-run `improve-issue` rather than assuming
   `normal`.

**On any failure**, emit the full table — passing rows included, each with
its evidence — then `STATUS: BLOCKED — preflight: <slug>[, <slug>]`, then
stop. On success:

```
Level: <level> (<source>) · Spec/Review: <model+effort>
Preflight: 7/7 preconditions pass — reviewing <SEC-ID>.
```

## Step 1 — Extract

Extract the section **to its own file** by its fence, then read that file.
Substitute the literal `<SEC-ID>`; the single quotes keep the shell away from
the `!` in the marker:

```bash
spec-load <n> <SEC-ID>     # writes /tmp/spec-<n>-<SEC-ID>.md
```

An **empty** result means the spec predates the fence contract. Halt:
`STATUS: BLOCKED — section-fence-missing`, re-run `improve-issue`. Never fall
back to a heading- or `---`-bounded guess — a heading bound prefix-matches
`CLI-BE-1` against `CLI-BE-10`.

`/tmp/review-issue-<n>-comments.json` may not exist yet — preflight skipped
writing it under `SECTION_STATE:` (Step 0). If the file is missing, fetch it
now with the same `gh api` call Step 0 would otherwise have run; this is the
first point in the run that genuinely needs comment bodies, not just the
presence/timestamp facts `SECTION_STATE:` already carried.

Then take its Section Map row, and the existing review table (from
`/tmp/review-issue-<n>-comments.json`, the comment whose body starts
`<!-- review-table #<n> -->`), if any. From the newest section-log comment:
the per-AC evidence table.

**Read the section file, never the whole spec** — sibling sections are other
runs' concern, and reading `/tmp/spec-<n>.md` wholesale defeats the fence.

## Step 2 — Verify every AC (evidence-first, tick-trusting)

For **each** AC in the target section, ticked or not, produce one verdict:

- **Red-tested AC** (has a section-log row citing `test file:line`): open the
  cited location; the test must exist and assert the AC's observable outcome
  (its Given/When/Then). A log row claiming `new`/`amended` with no such test,
  or a test asserting something else, is a contradiction.
- **`(mechanical — …)` AC**: run the AC's own stated check (its grep/command)
  now; a failing check is a contradiction.
- **`(visual|manual — …)` AC**: confirm the stated means exists (e.g. the
  named screen, command or test page). The actual check stays with the human
  — it may need a printer, a device or a running server — so record the
  verdict as met-deferred-to-human; a missing means is a contradiction.

Then, per AC:

- **Evidence confirms → met.** If its checkbox is unticked, add it to this
  run's **tick list**. Do not write yet.
- **No evidence found, but ticked → trusted.** The light search above is the
  whole search; do not escalate into an investigation.
- **Contradiction → unmet.** Write a failure note for the table, one line,
  factual: `AC-k: <what is missing / what contradicted>` — e.g.
  `AC-2: Not implemented, no test asserts the retry`. Never untick the box; the
  table row is the signal.

### The documentation-scope and CHANGELOG check (every section, every run)

A section id is `<APP>-<TYPE>-<k>` (`CLI-BE-1`, `AND-UI-2`, `COM-DOC-1`): `<APP>` is the
application's code in `.claude/scripts/apps.tsv`, `<TYPE>` is `DOC`, `BLD`, `BE` or `UI`. Read
both from the id, and the application's directory from the registry:

```bash
APP=${SEC%%-*}; TYPE=$(printf '%s' "$SEC" | cut -d- -f2)
DIR=$(awk -F'\t' -v c="$APP" '$1 == c { print $2 }' .claude/scripts/apps.tsv)
```

Every rule in this skill that names a type keys on `$TYPE`, never on a literal id.

Only a `DOC` section edits documentation, and only its own application's. Its
**documentation set** is `CHANGELOG.md` and every `.md` file under `$DIR/`. Prove
scope from the diff rather than trusting the spec, once per run, on the checked-out section branch:

```bash
git diff --name-only origin/master...HEAD \
  > /tmp/review-issue-<n>-<SEC-ID>-files.txt
git diff --name-only HEAD >> /tmp/review-issue-<n>-<SEC-ID>-files.txt   # uncommitted work
```

The spec file `specs/issue-<n>-enriched-spec.md` is exempt from the tables below: every
section's diff carries its AC ticks there. Instead, its diff must show only `- [ ]` → `- [x]`
flips inside this section's fence (note `Scope: spec file edited beyond ticks` otherwise).

| Target type | Requirement | Note on violation |
|---|---|---|
| `DOC` | every path is `CHANGELOG.md` or a `.md` file under `$DIR/` | `Scope: <path> is outside the documentation set` |
| `BLD`, `BE`, `UI` | no path is `CHANGELOG.md` or ends in `.md` | `Scope: edits <path>, not prescribed` |

```bash
F=/tmp/review-issue-<n>-<SEC-ID>-files.txt
if [ "$TYPE" = DOC ]; then
  sort -u "$F" | grep -vE "^CHANGELOG\.md$|^$DIR/.*\.md$|^specs/issue-<n>-enriched-spec\.md$"
else
  sort -u "$F" | grep -E '(^|/)[^/]+\.md$' | grep -v '^specs/issue-<n>-enriched-spec\.md$'
fi
```

Either prints nothing on a clean section. For a `DOC` section this is the same check its own AC-2
names — run it once and read it twice, for that AC's verdict and for this check.

**CHANGELOG (house convention H-4).** `DOC` sections only: `CHANGELOG.md` is in the diff, its
added lines are `-` bullets under the `## Wubiq X.Y.Z` heading the section names, all below the
`### Wubiq-<module>` subheading of this section's application and no other, and none carries an
issue link (`#[0-9]+`). Otherwise note `Changelog: <what is missing or wrong>` — a missing entry is a
gap, and it loops.

**A violation is a table note, never a Step 4 reviewer fix.** Reverting the
edit would delete the only evidence that the application's `DOC` section was
incomplete, and a documentation change another section turns out to need is a spec defect, which
only `code-issue` may raise. Note it, loop, and let the coder decide.

**Then one write, after every AC has a verdict.** Invoke
`spec-store <n> tick <SEC-ID> <AC-list>` **once**, carrying every AC on the tick
list — never one call per AC. An empty tick list means no call at all.

`spec-store` edits a copy of the working copy of the spec in this checkout, flips only those
checkbox characters inside that section's fence, runs the "diff shows only checkbox flips" guard,
and writes the copy back — no API call, no PR. The tick stays in the working tree, uncommitted
like the rest of this run's edits, and reaches `master` inside the section's own PR when
`gate-commit` commits it. Branch on its terminal `STATUS:` line: `STATUS: OK ticked <k>` continues;
`STATUS: BLOCKED — spec-missing` or `spec-tick-failed` stops the run with that line.


## Step 3 — Gate (fresh, full)

On the section branch:

```bash
.claude/scripts/gate.sh
```

Run per `../shared/background-long-commands.md` — `run_in_background`, then
one foreground `.claude/scripts/wait-verdict.sh gate`. `gate.sh` works out
which applications the branch's diff against `origin/master` (plus any
uncommitted or untracked work) affects — the ones owning a changed file and
the ones depending on them, per `.claude/scripts/apps.tsv` — and compiles each
from empty output directories, sources and tests. A red gate prints
`STAGE: <APP>:compile` (or `STAGE: pipeline:syntax` for a broken pipeline
script). There is no formatter, linter or static analysis in this repository:
the gate is compilation.

When the diff holds no application code — documentation, images, specs —
`gate.sh` prints `SKIPPED: no application affected` and exits 0 without
compiling anything. Treat that as this step's entire outcome — log
`Gate: skipped — no application affected` on the section's table row and do
**not** also run the suite below: a diff with no code cannot change the
suite's outcome any more than the gate's, so a second command re-answering
the same already-known question is pure cost, not verification.

Only when `gate.sh` does not skip, also run:

```bash
.claude/scripts/suite.sh
```

per the same rule (`run_in_background`, then one foreground
`.claude/scripts/wait-verdict.sh suite`). It runs the headless tests of every
application that has any, whatever the diff touched, and prints one
`<APP>: <summary>` line per application before `EXIT: 0`; a red suite prints
`STAGE: <APP>:compile` or `STAGE: <APP>:test`. Both must be green. This is the
independent run the section's green-tests block assigns to this skill —
`code-issue` ran only targeted tests. A red result that Step 4 can fix
mechanically is fixed there, verified per Step 4's own loop, and this full
gate re-run once more at Step 4's boundary — not re-run here, immediately. Any
other red becomes a note on the section's table row:
`Gate: <STAGE line — one-line excerpt>`.

**Why the skip is safe.** `origin/master` is always green — `merge-pr` only
ever merges a PR whose gate already passed, so any branch cut from it starts
from content that already compiled and passed the full suite, and a diff with
no code cannot regress either. No test reads `CHANGELOG.md` or a `README.md`.
A `DOC` section — the section shape this is almost always — is also the first
in its application's order (`DOC` → `BLD` → `BE` → `UI`), so it never rides on
that application's not-yet-gated work either.

## Step 4 — Project checks and light fixes (REVIEWER-FIXED)

Two passes over this branch's diff, in order. Scope both to what changed:

```bash
git fetch origin master --quiet
CHANGED=$( { git diff --name-only origin/master...HEAD; git diff --name-only HEAD; } | sort -u )
```

Every path cited in a note **must** appear in `$CHANGED`, or be a test file that exercises code in
`$CHANGED`. A finding that cannot be tied to a changed file is dropped: no pre-existing code, no
paths from memory.

### 4a — Project checks (blocking; loop, never fixed here)

The gate proves the affected applications compile, and nothing more. **Do not re-flag a compile
error by inspection** — the gate was green or Step 3 already noted it. This pass is what
compilation cannot see. For each check, run the exact command, cite `file:line`, and emit nothing
when it returns nothing. Every hit becomes a `Flag:` note on the section's table row
(`Flag: <path>:<line> — <issue>; expected <behaviour>`), because each changes behaviour or what
ships.

Define the helper once; a plain `rg PATTERN` with no path arguments silently searches the whole
tree, so every filtered search goes through it:

```bash
# scoped '<pattern>' [path-regex]      — search only $CHANGED files whose path matches
# scoped_not '<pattern>' <path-regex>  — search only $CHANGED files whose path does NOT match
_scoped_run() { local pattern="$1"; shift; [ "$#" -eq 0 ] && return 0; rg -n "$pattern" "$@" || true; }
scoped()     { local p="$1" f="${2:-.}"; _scoped_run "$p" $(printf '%s\n' $CHANGED | rg    "$f" || true); }
scoped_not() { local p="$1" f="$2";      _scoped_run "$p" $(printf '%s\n' $CHANGED | rg -v "$f" || true); }
```

`rg` has no look-around without `--pcre2`; use `scoped_not` for exclusions.

`<dir>` below is the directory of the section's application — `$DIR` from Step 2, the `dir`
column of the registry row the section ID's first part names (`CLI-BE-2` → `wubiq-client`). Test
paths are `tests-src/` in a `java-ant` application and `app/src/test/` in the `android-gradle` one;
`TESTS='(^|/)tests-src/|/src/test/'` matches both.

**1. Language level.** A module compiles at the level its build file sets, and the build file is
the source of truth: `source=` / `target=` on the `<javac>` elements of `<dir>/build.xml`,
`sourceCompatibility` in `wubiq-android-gradle/app/build.gradle.kts`. The gate compiles with a
newer JDK and does not catch an API that level lacks. Read the level first:

```bash
rg -n 'source="|target="|sourceCompatibility|targetCompatibility' <dir>/build.xml <dir>/app/build.gradle.kts 2>/dev/null
```

While that level is 1.6 — every `java-ant` module today — each of these must return nothing on the
application's own `.java` files; drop the list for a module whose build file sets a higher level,
and check against that level instead:

- Lambdas and method references: `scoped '\) ?->|\w ?-> ?[\w({]|\w::\w' '^<dir>/.*\.java$'`
- `var` declarations: `scoped '^\s*(final )?var \w+ ?=' '^<dir>/.*\.java$'`
- try-with-resources: `scoped '\btry ?\(' '^<dir>/.*\.java$'`
- Diamond: `scoped 'new [\w.]+<>' '^<dir>/.*\.java$'`
- APIs added after 6: `scoped 'java\.util\.stream|java\.util\.function|java\.util\.Optional|java\.time\.|java\.nio\.file\.|java\.util\.Objects' '^<dir>/.*\.java$'`
- A `switch` on a `String`: read each `switch (` the diff adds (`scoped 'switch ?\(' '^<dir>/.*\.java$'`)
  and confirm the selector is an `int`, `char` or enum.

A `->` inside a comment or a string is not a hit; read the line before flagging it.

**2. Test shape.** On the changed test files:

- JUnit 4 only: `scoped 'org\.junit\.jupiter|junit\.framework|extends TestCase' "$TESTS"` returns
  nothing, and every changed test class imports `org.junit.Test`.
- No sleeping: `scoped 'Thread\.sleep|TimeUnit\.\w+\.sleep' "$TESTS"` returns nothing.
- Headless and self-contained — no server, socket, printer or device:
  `scoped 'new (Server)?Socket\(|HttpURLConnection|URL\(|localhost|127\.0\.0\.1|PrintServiceLookup|DriverManager' "$TESTS"`
  returns nothing. `suite.sh` runs with no display and nothing listening.
- No assertion on a version string:
  `scoped 'assert.*(VERSION|[Vv]ersionName|versionCode|"[0-9]+\.[0-9]+\.[0-9]+")' "$TESTS"` returns
  nothing.

**3. No build output.** Compiled classes and packaged artifacts are never committed:

```bash
git diff --name-only origin/master...HEAD | grep -E '(^|/)(bin|dist|build|build-tests)/|\.(jar|aar|apk|aab|class)$'
```

returns nothing, for every section type. Two things are never allowed, whatever the spec says:
anything under `bin/`, `dist/`, `build/` or `build-tests/`, and a rebuilt `wubiq-*.jar` — the
ones under `wubiq-android-gradle/app/libs/` are the usual offender, because the default targets
of the common and client builds overwrite them. The one allowed hit is a vendored third-party
jar (or `.aar`) in a `BLD` section whose spec names that file.

**4. Dependencies change only in a `BLD` section.**

```bash
printf '%s\n' $CHANGED | grep -E '(^|/)libs?(-[a-z]+)?/'
git diff origin/master...HEAD -- '*.gradle.kts' '*.gradle' | grep -E '^[+-].*(implementation|api|compileOnly|runtimeOnly|classpath|testImplementation|androidTestImplementation)\b'
```

In a `BE` or `UI` section both return nothing; any hit is a flag, because adding or upgrading a
dependency belongs to the application's `BLD` section. In a `BLD` section every hit is a
dependency its spec names, at the version it names; one the spec does not name is a flag.

**5. Internationalised strings.** A string the user reads comes from a bundle, never from a
literal in code. For every key the diff adds to a bundle, each locale file that bundle already has
must carry it:

```bash
printf '%s\n' $CHANGED | grep -E '(^|/)i18n/.*\.properties$|/res/values[^/]*/strings\.xml$'
```

For each file listed, take the keys on its added lines (`git diff origin/master...HEAD -- <file>`)
and `rg -c '<key>'` across the sibling locale files (same base name with another `_<locale>`
suffix; the other `values-*` directories). A locale missing the key is a flag. Then read the new
string literals in the changed UI code (Swing, JSP, Android activities and layouts): a new
user-visible literal that is not a bundle lookup is a flag.

**6. Application boundary.** Every path in `$CHANGED` is inside `<dir>/`, or is
`specs/issue-<n>-enriched-spec.md` (ticks only — Step 2 checks its diff):

```bash
printf '%s\n' $CHANGED | grep -vE '^<dir>/|^specs/issue-<n>-enriched-spec\.md$'
```

returns nothing. On a `DOC` section the requirement is Step 2's documentation-scope table
instead, already checked (it also admits `CHANGELOG.md`). A change another application needs belongs to that application's own section; a file
outside every application directory that the section does not name is a flag.

**Integrity (read the diff; each failure is a `Flag:` note):**
- A test added or amended for an AC lives in the application that can run it: `suite.sh` runs
  tests only for the applications whose `test` column in `apps.tsv` is not `-`. A test class placed
  where nothing runs it proves nothing.
- A change to `wubiq-common` that alters a signature other applications call: the dependents
  (`apps.tsv` `deps` column) still compile — the gate proves that — and the section says how the
  Android application, which builds against a tracked jar and not against the sources, is affected.
- Scope discipline: the diff touches nothing unrelated to the spec (refactors, dependency bumps,
  whitespace churn) — non-blocking, mention in the chat summary.

### 4b — House-conventions sweep (REVIEWER-FIXED)

One pass over the diff (`git diff origin/master...HEAD` plus uncommitted work) against
`../shared/house-conventions.md`. A hit that does not change behavior — wording, doc-citation
style, an issue link on a CHANGELOG entry (H-4) — is fixed directly on the
branch and listed in the chat summary. After any fix, run **the command of every AC the fix
touches** (Step 2), and for a fix inside a source or test file also the test classes covering it,
through `.claude/scripts/section-test.sh <APP> <test>` — a fix that leaves an AC's own check
failing has moved the problem, not solved it. Do **not** re-run Step 3's full gate inside this loop; that full run happens
once, at the end, below. Guards: never a behavior change, never a spec edit beyond Step 2's
checkboxes, never labels. When in doubt whether a fix changes behavior, it does — table note
instead.

On a `DOC` section's branch this pass carries most of its weight, and two rules bind:

- H-1 (cite documentation by section, never by line) and H-4 are the hits that dominate. Both
  are non-behavioral, so all are fixed here as usual — H-4's fix (dropping an issue-link suffix)
  also restores the section's own AC-1, which is why the AC re-run above matters.
- **A reviewer fix on a `DOC` section's branch stays inside its documentation set.** Creating or editing
  any file outside it breaks that section's AC-2, which Step 2 has already verified. A fix that
  cannot be made within it is a table note.

Step 2's documentation-scope violations and 4a's blocking flags are **not** fixed here. They loop.

Once this pass finds nothing left to fix, run Step 3's full gate **one more
time**, on the resulting tree — the boundary run, not the per-fix one, and the
only place this skill runs the full suite after the loop starts. A red result
here is another fix to make: apply it, re-run the failing test alone
(`.claude/scripts/section-test.sh <APP> <test>`) plus that fix's AC commands,
then come back to this full gate again. It is not a table note
until this loop can no longer make progress on it.

Once this final gate is green — the last re-run, not the first — follow
`../shared/gate-receipt.md`: write `.test-receipt.json` with
`"consistency": "green"` (the field's name for a green gate) and `"suite": "green"`, and include the
`**Gate receipt:** tree \`<sha>\` — consistency ✓, test ✓` line in the ✅ chat
verdict (chat only — never persisted to the issue). Compute `tree_hash` after
every edit this run makes; a later edit means recompute and rewrite the file,
not reuse the earlier hash.

## Step 5 — Write the review table, then the verdict

The table is **one comment, edited in place** — created on the first review
of the issue, updated by every later run, never posted twice:

```markdown
<!-- review-table #<n> -->
| SECTION | STATE |
|---|---|
| COM-DOC-1 | COMPLETED |
| COM-BE-1 | COMPLETED |
| CLI-DOC-1 | COMPLETED |
| CLI-BLD-1 | Scope: edits wubiq-client/README.md, not prescribed |
| CLI-BE-1 | AC-2: `wubiq-client/src/net/sf/wubiq/clients/LocalPrintManager.java` `processPendingJob` — a failed job is never retried; retry once and assert the second attempt in `wubiq-client/tests-src/net/sf/wubiq/clients/LocalPrintManagerTest.java` |
| AND-UI-1 | PENDING |
```

- One row per Section Map entry, in map order.
- `COMPLETED` — reviewed green (all ACs met/trusted, gate green).
- Failure notes — the `AC-k: …` / `Scope: …` / `Changelog: …` / `Gate: …` / `Flag: …` lines from
  Steps 2–4, `<br>` separated when several. Every note is one factual line that names the file
  path, the function or test to add or change, and the behaviour expected — `code-issue` acts on
  it without re-running the checks. A note that cannot name all three is dropped, not softened.
- `PENDING` — not yet coded/reviewed. Rows other than the target keep their
  existing state; a missing table starts all-`PENDING`.
- A `COMPLETED` row is only ever demoted when this run's own evidence
  contradicts it — then it reverts to failure notes and the completion
  phrase is retracted (below). An amend pass is the ordinary case: the row
  was `COMPLETED`, the spec changed, `code-issue` re-coded it, and this run's
  evidence — never the spec change itself — decides whether it stays
  `COMPLETED`.

```bash
# create (no existing comment):
gh issue comment <n> --body-file /tmp/review-issue-<n>-table.md
# update (comment id from /tmp/review-issue-<n>-comments.json):
gh api -X PATCH "repos/{owner}/{repo}/issues/comments/<id>" \
  -F body=@/tmp/review-issue-<n>-table.md
```

**Verdict — first match wins:**

1. **Target row carries failure notes → 🔄 LOOP.** Retract the completion
   phrase if present (below). Chat:
   ```
   🔄 LOOP — <k> finding(s) on <SEC-ID> (see review table).
   👉 Next: code-issue <n> <SEC-ID>  (Coder: <model+effort>)
   STATUS: LOOP
   ```
2. **Target row `COMPLETED`, other rows not all `COMPLETED` → ✅ section
   approved.** Chat:
   ```
   ✅ DONE — <SEC-ID> approved; gate green (gate ✓, suite ✓).
   <reviewer fixes applied, or "No reviewer fixes.">
   **Pending human verification:** <visual/manual ACs, or "none">
   👉 Next: pr-from-issue <n> <SEC-ID>  (Ship: <model+effort>)
   STATUS: DONE
   ```
3. **Every row `COMPLETED` → issue complete.** Append the phrase this skill
   owns — read verbatim by `pr-from-issue` and the orchestrator, written by
   nothing else:
   **Always fetch the body fresh here.** Step 2's edit file exists only if some
   AC needed ticking; on the common path — `code-issue` ticked everything — it
   was never created, and appending to a missing or stale file would write a
   body that is not this issue's.

   ```bash
   PHRASE='Issue is completed and ready to be closed.'
   BODY=/tmp/review-issue-<n>-phrase.md

   gh issue view <n> --json body --jq '.body' > "$BODY" || {
     echo "❌ Could not read issue <n>. Nothing written."; exit 1
   }
   [ -s "$BODY" ] || { echo "❌ Empty body returned. Refusing to write."; exit 1; }

   grep -qF "$PHRASE" "$BODY" || printf '\n\n%s\n' "$PHRASE" >> "$BODY"
   gh issue edit <n> --body-file "$BODY"
   ```
   Chat: `✅ DONE — issue #<n> complete; all sections COMPLETED.` /
   `STATUS: DONE`.

**Retraction** (verdict 1, or a demoted row): remove the phrase so
`pr-from-issue` cannot proceed on incomplete work:

Same rule: fetch fresh, never reuse an edit file from an earlier step.

```bash
PHRASE='Issue is completed and ready to be closed.'
BODY=/tmp/review-issue-<n>-phrase.md

gh issue view <n> --json body --jq '.body' > "$BODY" || {
  echo "❌ Could not read issue <n>. Nothing written."; exit 1
}
[ -s "$BODY" ] || { echo "❌ Empty body returned. Refusing to write."; exit 1; }

grep -vF "$PHRASE" "$BODY" > "$BODY.tmp"
[ -s "$BODY.tmp" ] || { echo "❌ Retraction would empty the body. Refusing to write."; exit 1; }
mv "$BODY.tmp" "$BODY"
gh issue edit <n> --body-file "$BODY"
```

## Step 6 — Closing mode

Reached only from preflight row 3: every Section Map row reads `COMPLETED`
**and** has a merged PR — the state `code-issue` reports as
`no-runnable-section` at true end of issue. It runs in the orchestrator's own checkout, on a
fresh `origin/master`, with no section branch.

**Closing mode never ticks.** Each section's `code-issue` and `review-issue` ticked its own ACs
locally on its section branch, and those ticks reached `master` inside that section's PR. A tick
written now would edit a **merged** section's fence — a spec change the merged-section guard
refuses, and one that would have to travel through a spec PR (`spec-store` Mode `write`) for a
checkbox. So:

1. Count the unticked ACs of every section on `master`'s spec (`spec-load <n>`). List them in the
   chat summary as `Unticked on master: <SEC-ID> AC-k, …` (merged PR + section log + `COMPLETED` row
   are the evidence they were met) and leave them. They never gate the verdict.
2. Confirm the CHANGELOG (H-4): every `DOC` section's merged PR touched `CHANGELOG.md`, or the
   spec's Overview states the issue owes no documentation edit. Neither → demote the row that should have carried
   it with a `Changelog:` note, retract the phrase, `STATUS: LOOP`.
3. Run the gate once (`.claude/scripts/gate.sh --all`, then `.claude/scripts/suite.sh`, per
   `../shared/background-long-commands.md`) — final integration check on the merged whole.
   `--all` compiles every application: a clean checkout differs from `origin/master` in nothing, so
   the plain gate would skip. Red → demote the offending section's row with a `Gate:` note,
   retract the phrase, `STATUS: LOOP`.
4. Green → write the completion phrase (Step 5, verdict 3):
   `✅ DONE — issue #<n> complete; all <s> sections merged.` / `STATUS: DONE`.

## Output budget

Be direct and specific; the purpose of the review is to make the code better, not to validate
effort. Emit only **failing** rows in any table and list the passing ones as one comma-separated
line. Do not echo the spec back and do not summarise what the implementation does. Keep the chat
report under 150 lines; more than that is over-explaining.
