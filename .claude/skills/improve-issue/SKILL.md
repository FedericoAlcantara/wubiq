---
name: improve-issue
description: >
  Partition and enrich a GitHub issue into an ordered set of self-contained,
  atomic sections — one Documentation section and one or more per application
  the issue changes — each deliverable as its own PR, for Wubiq (remote
  printing: several Java applications in one repository, built with Ant and
  Gradle). Use whenever the user says "work on issue", "improve
  issue", "enrich issue", "clarify requirements", or pastes an issue URL/number.
  Always runs FIRST, before code-issue — it produces the sectioned spec that
  code-issue implements one section at a time. Presents the spec for approval
  and writes nothing until a human approves it.
---

# Skill: improve-issue

Turn a raw GitHub issue into a sectioned spec that the `normal` coder executes without guessing.
Each section is coded on its own branch and merged through its own PR.

A state machine. One run executes the steps below top to bottom — steps 1–4 once, steps 5–12
once per pass.

Wubiq is several applications in one repository — shared code, a desktop print client, a
server, an installer, an Android app and their test tools — each in its own directory with its
own build. `.claude/scripts/apps.tsv` is the registry of them: code, directory, toolchain and
what each depends on. Every section belongs to exactly one application (6 · Partition).

## Glossary

Each term has one meaning in this file. No synonym is used.

| Term | Meaning |
|---|---|
| **spec** | `specs/issue-<n>-enriched-spec.md` on `master` — the only authoritative copy |
| **draft** | `/tmp/improve-issue-<n>-spec.md` — the text this run builds and a human approves |
| **working text** | the spec text this run is editing; it becomes the draft at Present |
| **mirror** | the pointer and Section Map copy in the issue body; regenerated, no authority |
| **store** | write the draft to the spec through `spec-store` (branch → PR → merge), which also regenerates the mirror and posts the resolution comment |
| **section** | one unit of delivery: one `<SEC-ID>`, one branch, one PR |
| **fence** | the `<!-- section:<SEC-ID>:start -->` / `:end -->` line pair that delimits a section |
| **enrichment** / **re-enrichment** | a run with no spec / a run that edits an existing spec |
| **defect**, **feedback**, **resolution comment** | defined in `../shared/improve-issue/comment-protocol.md` |
| **scope** | the sections this run drafts or changes |

## Invariants

1. Nothing is stored, and no comment is posted, until a human approves the draft's exact bytes.
   This holds on every run, including a one-word re-enrichment.
2. This skill edits no file in the repository — no documentation, no code. It writes only
   `/tmp/improve-issue-<n>-*` files; a sub-skill it invokes writes its own `/tmp` files, and the
   spec reaches the repository only through `spec-store`'s spec PR. `DOC-1` describes
   documentation edits; `code-issue` applies them.
3. This skill never creates, splits or closes an issue.
4. No question survives into the spec. A question is never answered by assumption. The spec has
   no "Open Questions" section.
5. The spec's depth is fixed at what the `normal` coder needs, at every level. The spec records
   the level, never a model name.
6. A section whose PR is merged is never changed. A section whose PR is open may be changed — and
   the user is told, because the open branch's local AC ticks will then conflict with the rewrite
   (`spec-store` § Re-enrichment while sections are in flight).
7. A `<SEC-ID>` is never renumbered and never re-used.
   Why: it is a branch name, a PR title, a review-table row, a section-log marker and a fence.
8. Facts come from GitHub and the repository, never from chat context. An existing spec is read
   through `spec-load`, never from the issue body.
9. This skill spawns no subagents. Grounding, drafting, checking and revision run inline,
   sequentially, in this run's own context. Being spawned as an agent by a caller is
   unaffected.

## Machine

`R` is this run's working record. **Steps communicate only through `R`.**

| Field | Content |
|---|---|
| `n` | the issue number |
| `mode` | `interactive` \| `non-interactive` |
| `decisions` | every `DECISION:` line of the prompt |
| `changes` | the text of a change request, or none |
| `level_arg` | the level the prompt asked for, or none |
| `level` | `normal` \| `high` \| `max` |
| `obs` | `/tmp/improve-issue-<n>-issue.json`, `/tmp/improve-issue-<n>-comments.jsonl`, the mentioned issues, `spec: /tmp/spec-<n>.md \| missing`, `prs`: per Section Map row, its PR's number and state, or none |
| `inputs` | unresolved defect and feedback comments: `id`, kind, `<SEC-ID>`, defect `Class:` |
| `scope` | `all` on an enrichment; otherwise the ids this run changes or adds — it grows as the run edits |
| `answered` | the count of questions the user answered in this run |
| `aids` | the reading aids Present built, or none |
| `unchanged` | set when the working text is byte-identical to the spec |
| `verdict` | `approved` \| `changes` \| `rejected`, or unset |
| `stored` | the merge-commit sha `spec-store` returned, or unset |
| `halt` | `{kind, payload}` — first writer wins |

Each step opens with a **pre-flight** that yields one verdict:

| Verdict | Meaning |
|---|---|
| **continue** | do the step |
| **skip** | nothing for this step this pass — next step |
| **stop** | set `R.halt` — next step |

**Universal pre-flight, before a step's own:** `R.halt` set → **skip**. Step 12 alone is exempt.
A step's `Loads` file is read only on **continue**, at most once per run.

| `halt.kind` | Payload |
|---|---|
| `relay` | a sub-skill's terminal output, verbatim |
| `decision` | one question, its options, and the facts already established |
| `blocked` | a slug, and the facts behind it |

### Question rule

A **question** is any point where two readings, two designs or two names remain after the
repository and the documentation have been read. Every step applies this rule at the point
where the question appears:

| Condition | Action |
|---|---|
| a line in `R.decisions` answers it | take that answer; continue |
| `mode = interactive` | `AskUserQuestion`; take the answer; `answered += 1`; continue |
| `mode = non-interactive` | **stop** `decision`, with the same options in the same order as the interactive question |
| the user cannot answer it | **stop** `blocked` `unresolved-question` |

Always a question, never a judgement of this skill:

- a design that removes, bypasses or makes unreachable an existing guard, validation or
  constraint;
- a change to the wire protocol between client and server that an already released client or
  server would not understand;
- a change that needs new `wubiq-common` or `wubiq-client` code inside the Android app, which
  ships them as tracked jars;
- a raise of an application's language level, `minSdk`, or the JDK it builds with;
- a new third-party jar or Gradle dependency the issue does not name;
- a capability specced for one application when the issue's wording covers several and does
  not say which;
- the CHANGELOG version heading, when the topmost version is already tagged;
- documentation content that contradicts an existing rule;
- a symbol or a capability claim that stays unclassified after searching.

---

## 1 · Boot

- **Pre-flight:** continue.
- **Loads:** `../shared/turn-discipline.md`
- **Do:** parse the prompt.

  | Input | Sets |
  |---|---|
  | issue URL, `#123`, a number, or a pasted issue body carrying either | `n` |
  | the line `NON_INTERACTIVE: true` | `mode = non-interactive`; absent → `interactive` |
  | each `DECISION:` line | `decisions` |
  | a `CHANGES:` line, under `non-interactive` only | `changes`; under `interactive` the line is ignored |
  | a positional level, else a `LEVEL:` line | `level_arg` |

- **Writes:** `n`, `mode`, `decisions`, `changes`, `level_arg`.

## 2 · Load

- **Pre-flight:** continue.
- **Do**, batched in one turn:
  1. Issue: `gh issue view <n> --json title,body,state,labels`. Save it as
     `/tmp/improve-issue-<n>-issue.json`.
  2. Comments, with ids:
     ```bash
     gh api "repos/FedericoAlcantara/wubiq/issues/<n>/comments" --paginate \
       --jq '.[] | {id, body}' > /tmp/improve-issue-<n>-comments.jsonl
     ```
  3. Spec: `spec-load <n>`. `STATUS: OK <sha>` → `obs.spec = /tmp/spec-<n>.md`.
     `STATUS: BLOCKED — spec-missing` → `obs.spec = missing`; this run is an enrichment.

  Then, in one more turn: fetch the title, body and state of every issue the body or a comment mentions as `#<k>` or
  by URL, one level deep;
  and, for each Section Map row of the spec, record its PR in `obs.prs`:
  `gh pr list --head <branch> --state all --json number,state`.

  The **working text** the run edits:

  | Condition | Working text |
  |---|---|
  | `changes` set and the draft file exists | the draft |
  | otherwise, `obs.spec` is a path | that file |
  | otherwise | none — drafted from nothing |

- **Writes:** `obs`.

## 3 · Gate

- **Pre-flight:** continue.
- **Loads:** `../shared/coder-model.md`
- **Do:**
  1. Resolve the level, first match: `level_arg` · the spec's `**Complexity:**` line ·
     `normal`. An unrecognised value → **stop** `blocked`
     `unknown-level`. On an enrichment, propose the level from `coder-model.md` § Choosing a
     level; the user's `level_arg` overrides it.
  2. Announce, before any other output, in the format and with the `<source>` that file
     defines: `Level: <level>[ (<source>)] · Spec/Review: <model+effort>`. A halt's output
     follows this line.

- **Writes:** `level`.

## 4 · Inputs

- **Pre-flight:** continue.
- **Loads:** `../shared/improve-issue/comment-protocol.md`, only when a line of the comments file
  matches `<!-- spec-defect #` or `<!-- review-feedback #`.
- **Do:** with that file loaded, list the unresolved defects and feedback as it defines them;
  each one is handled as it prescribes, in this run. Otherwise `inputs` is empty.
  When `obs.spec` is a path, compare every requirement in the issue body and its comments with
  the spec's ACs. A requirement no AC covers is **owed**: add the section it belongs to, or note
  it as new work for Partition.
- **Writes:** `inputs`; `scope`:

  | Condition | `scope` |
  |---|---|
  | `obs.spec = missing` | `all` |
  | `changes` set and naming no `<SEC-ID>` | every section whose PR in `obs.prs` is not merged. When there is none, `changes` is new work for Partition |
  | otherwise | the `<SEC-ID>` of every input, of every owed requirement, and every `<SEC-ID>` `changes` names. It may be empty |

## 5 · Ground

- **Pre-flight:** `scope` empty and no new work noted → skip.
- **Do:** ground every name the sections in `scope` will use. Read the registry and the
  documentation before the code — they are the specification:

  | Read | For |
  |---|---|
  | `.claude/scripts/apps.tsv` | the applications: code, directory, toolchain, what each depends on, how each compiles and tests |
  | `CHANGELOG.md`, top entries | the version being prepared, the `### Wubiq-<module>` headings, the entry style |
  | the `README.md` of each application in scope | the documented behaviour, commands and vocabulary the issue changes |
  | the application's build file — `build.xml`, or `app/build.gradle.kts` | its language level, classpath, targets or tasks, and for Android `minSdk` / `targetSdk` |
  | the source files the issue names, and what they call | the classes, methods, constants and keys the sections will cite |
  | the tests next to them, in the application's test sources | existing coverage, fixtures and the way a test reaches the code |

  On a re-enrichment, re-ground the working text of every section in `scope` against the
  current repository.

  **Grounding rules:**

  1. **Verify every name at its definition.** This covers classes, methods and their parameter
     types, constants, protocol keys and parameter names, i18n keys, string resource names,
     manifest entries, Ant targets, Gradle tasks and third-party API calls. Open the declaration
     and copy the name verbatim. A call site, a grep hit, memory and a subagent's report are not
     definitions. A name the section itself introduces is prescribed as new.
  2. **Write every file-editing instruction with that file open at the edit site.** A report is
     evidence only for the lines it quotes.
  3. **Sweep every consumer of what the issue changes**, not only the classes it edits. For a
     method, its callers and its overriders; for a constant or a protocol key, every read of it
     in every application directory — `wubiq-common` is compiled into the client, the server and
     the installer. The spec never hand-rolls a check something downstream already performs.
  4. **An inventory is extracted, never recalled.** A statement of what a construct contains — an
     enum's constants, an interface's methods, a bundle's keys, a build file's classpath — is
     produced by extracting the block and reading the extraction.
  5. **Classify every symbol and every capability claim** as `existing` (cited) or `new` (with
     the search terms that returned nothing). A claim such as "the client already retries a
     failed connection" is verified in code. One that cannot be classified is a question.
  6. **Grounding never defers.** The spec may tell the coder to copy from a named, verified
     source. It never tells the coder to discover whether a method, a key or a target exists.

  Budget: 15–20 targeted searches plus a handful of file reads per section, the same for every
  section. The budget is a floor: it never excuses an unproven claim.

## 6 · Partition

- **Pre-flight:** `obs.spec` is a path and no new work is noted → skip.
- **Do:** build the Section Map. Ground every row added here under the Grounding rules. On a re-enrichment, existing rows keep their ids; new work
  appends ids. A row is removed only when its section has no section-log comment and no PR of
  any state.

  ### Types

  The applications are the rows of `.claude/scripts/apps.tsv`, read in Ground. This file never
  lists them: a row added to the registry is a type this step can use.

  | Type | Id | One section per |
  |---|---|---|
  | Documentation | `DOC-1` | issue — exactly one, carrying every documentation edit the issue owes, and its CHANGELOG entry |
  | an application — the Type cell is its directory name | `<APP>-k`, `<APP>` its registry code | unit of change inside that application that can merge alone |

  A section belongs to exactly one application and edits files only under that application's
  directory. An issue may span applications: each one it changes gets one or more sections. An
  application the issue does not change has none. `DOC-1` is absent only when the issue has no
  user-visible change (`documentation.md` rule 16); the Overview then says so in one sentence.

  ### Ids

  Unique per application, numbered per application, appended in Section Map order. A gap left
  by a removed section stays a gap.

  ### Order and dependencies

  1. Map order is Documentation, then the applications in registry order — the registry lists
     an application after everything it depends on. Map order is execution order.
  2. Every section carries `Depends on:` — sibling ids, or `none`.
  3. A section depends on every section of this issue in an application its own application
     depends on (registry column `deps`), when it uses what that section creates or changes. A
     dependency points backward across registry order, or sideways inside an application. The
     graph is a DAG.
  4. `DOC-1` depends on nothing. Every other section's dependency chain reaches `DOC-1`: a
     section with no other dependency carries `Depends on: DOC-1`. With no `DOC-1`, those
     sections carry `none`.
  5. A section starts only when every dependency's PR is merged. Independent sections may run in
     parallel.
  6. A dependency on another issue's section is written `#<n>·<SEC-ID>`. Use it when this section
     builds on code that section creates or changes; never duplicate that work.
  7. `DOC-1` is the only section that prescribes a documentation edit.
  8. Two sections that edit the same region of one code file declare a dependency: the later
     depends on the earlier.
  9. An application with no `deps` that ships another application's code as tracked jars
     (`wubiq-android-gradle/app/libs/`) depends on no section: sections never rebuild those jars.
     An issue that needs new common or client code inside that application is a question
     (Question rule).

  ### Atomicity

  Every section passes this test: *on its own branch from `origin/master`, with its dependencies
  merged, `.claude/scripts/gate.sh` passes — every affected application compiles from empty
  output directories — and `.claude/scripts/suite.sh` passes.* When a section fails it, move
  content between sections until it passes.

  A section that changes a signature or a constant in one application owns every site in that
  application that would not compile without the change. A dependent application's sites that
  would not compile make the change non-atomic: keep the old member until the dependent
  application's section has merged, and remove it in a later section of the first application.

  ### Scope separation

  An issue is never oversized; more work is more sections. Propose a separation only when the
  issue bundles parts with distinct goals, or a part that cannot be specced until another
  part's output exists. The proposal names each part's goal, its contents, and why it cannot be
  specced now. It is a question (Question rule). When the user separates, enrich the retained
  scope only and record the rest under `### Out of Scope`.

  ### Branch and PR title

  | Item | Form | Example |
  |---|---|---|
  | Branch | `<type>/<n>-<sec>-<slug>`, `<type>` a conventional-commit type, `<sec>` lowercased | `docs/360-doc-1-changelog`, `feat/360-cli-2-paper-size` |
  | PR title | `<type>(<scope>): … (#<n> · <SEC-ID>)`; `DOC-1` has no scope | `docs: … (#360 · DOC-1)`, `feat(client): … (#360 · CLI-2)` |

  `<scope>` is the application's short name: its directory without the `wubiq-` prefix, with
  `dotmatrix-witness` shortened to `witness` and `android-gradle` to `android`.

  Every branch is unique within the issue.
  Why: `code-issue` and `review-issue` find a section's PR by an exact `gh pr list --head` probe.
  The PR title is never used to detect state: GitHub's title search matches `CLI-10` for `CLI-1`.

- **Writes:** `scope` (every row added or re-scoped).

## 7 · Draft

- **Pre-flight:** `scope` empty → skip.
- **Loads:** `../shared/house-conventions.md`; `../shared/improve-issue/templates/documentation.md`
  when `DOC-1` is in `scope`; `../shared/improve-issue/templates/<toolchain>.md` for each
  registry toolchain that has a section in `scope` — and no other.
- **Do:** write the working text: the skeleton below, and every section in `scope` from its
  template under that template's rules. Sections outside `scope` are copied unchanged.
  Untick every AC whose text this run changed.
  Why: `code-issue` writes a red test for the new words and `review-issue` re-proves them.

  ### Skeleton

  ```markdown
  <!-- enriched-spec:start v2 -->
  ## Enriched Spec

  **Complexity:** <R.level>

  ### Overview
  <2–3 short sentences: what this issue delivers, and which applications it changes>

  ### Section Map
  | ID | Type | Scope | Depends on | Branch | PR title |
  |---|---|---|---|---|---|
  | DOC-1 | Documentation | <file § section, …> | none | docs/<n>-doc-1-<slug> | docs: … (#<n> · DOC-1) |
  | CLI-1 | wubiq-client | <capability> | DOC-1 | feat/<n>-cli-1-<slug> | feat(client): … (#<n> · CLI-1) |

  A section starts only when every dependency is **merged**. Independent
  sections may run in parallel. Status is derived from GitHub, never recorded
  here.

  <one fenced section per Section Map row, in map order>

  ---

  ### Out of Scope
  - <what this issue explicitly does NOT include>
  <!-- enriched-spec:end -->
  ```

  The fence is the only section delimiter. Every section is wrapped in one; every fence id is a
  Section Map row; no id is fenced twice; nothing sits between one section's `:end` line and the
  next section's `:start` line.
  Why: `spec-load` extracts a section by matching the two fence lines whole. A heading match
  would take `CLI-10` for `CLI-1`.

  ### Writing rules

  1. **Register.** Numbered sequential steps. Short plain sentences; noun phrases, imperatives
     and tables. One instruction, one outcome. No motivating narrative, no rationale, no
     restating of the problem. A sentence the coder can lose without acting differently is
     deleted.
  2. **No optionality.** "or", "consider" and "if appropriate" never appear in an instruction.
  3. **Citations.** Every existing symbol is cited by a signature that identifies exactly one
     definition. A line number is used only when no named anchor exists. Every new symbol is
     marked `new`, with its search evidence.

     | Cited thing | Signature |
     |---|---|
     | Method | `<path>.java` + `Class#method(<parameter types>)` |
     | Field or constant | `<path>.java` + `Class.NAME` |
     | Ant target | `<application>/build.xml` + the target name |
     | Gradle setting | `app/build.gradle.kts` + the block and the property |
     | i18n key / string resource | the bundle or `strings.xml` file + the key |
     | Manifest entry | `AndroidManifest.xml` + the element and its `android:name` |
     | Documentation | `§ Section name` |

  4. **Strings.** Every user-visible string is fixed: exact text, a key in the application's
     `i18n/` bundle or a string resource, with the text for every locale the application has.
  5. **Contract changes.** A section that changes a method signature, a protocol key, a
     parameter name or a serialized shape lists every current caller, implementer and reader
     with its new handling — in every application, since `wubiq-common` is compiled into the
     client, the server, the installer and the jars the Android app ships.
  6. **Renames and removals.** A section that renames or removes a key, a string, a resource
     name or a command-line option searches for it across every application directory, and
     lists every hit with its new handling.
  7. **Language level.** A section states the level its application's build file sets, and
     prescribes nothing above it (`java-ant.md` rule 4, `android-gradle.md` rule 4).
  8. **Standing project rules** are restated only where the section touches them:
     - a section edits files under one application directory only;
     - no Ant default target and no `create-*-jar` target; no `bin/`, `dist/`, `build/`,
       `build-tests/` or jar is committed;
     - the tracked jars in `wubiq-android-gradle/app/libs/` are never rebuilt or replaced;
     - names both ends of the wire use are constants in `wubiq-common`;
     - no new third-party jar and no new Gradle dependency unless the section names it;
     - `versionCode`, `versionName` and every other version string are never edited;
     - user-visible text goes through the i18n bundle or string resources, every locale.
  9. **House conventions.** `../shared/house-conventions.md` applies to every sentence of the
     spec.
  10. **Edge cases.** Every section of an application with a suite states at least one error or
      degraded path as an AC. The recurring ones: a printer or print service that is absent; a
      stream that ends early or cannot be read; a setting that is missing or malformed; a
      server that does not answer; a permission the user denied; misconfiguration that must
      surface loudly rather than be swallowed.

  ### Test rules

  1. **Placement**, in order of preference, stated on every red-test row:

     | Placement | When | Row says |
     |---|---|---|
     | amend an existing test | a test already exercises the port | `amend <file> <testMethod>` |
     | add to an existing file | the class under test has a test class | `add to <file>` |
     | new file | the class under test has none | `new file`, with the search that found none |

  2. **Location.** JUnit 4, in the test sources of an application that has a suite:
     `wubiq-common/tests-src` (`COM`), `wubiq-dotmatrix-witness/tests-src` (`WIT`),
     `wubiq-client/tests-src` (`CLI`), `wubiq-android-gradle/app/src/test/java` (`AND`). A
     section's tests are in its own application. `SRV`,
     `INS` and `PJB` have no suite.
  3. **Setup.** Every row states how the test reaches the port: the objects it builds, the
     fixture or golden file it reads, the stream it writes into. A test is headless, needs no
     printer, device, server or network, and never sleeps.
  4. **Targeted run.** Every row's test runs through
     `.claude/scripts/section-test.sh <APP> <test>` — a fully qualified class name for a
     `java-ant` application, a Gradle `--tests` filter for `AND`. The `Green tests` block gives
     the exact command.
  5. **Every AC of an application section has a red-test row, or ends**
     `(<mechanical|manual> — no red test; verified by <means>)`. `mechanical` names a command;
     `manual` names the steps and the hardware.
  6. **Never a version string.** No test asserts a version number or a build identifier.

- **Writes:** `scope` (every section this step edited).

## 8 · Check

- **Pre-flight:** `scope` empty → skip.
- **Do:** for every section in `scope`, confirm each rule under each heading below. Fix every
  failure in the working text; never note one and move on. When `R.changes` states that this
  file gained a rule after the spec was stored, check every section whose PR is not merged.

  | Check | Home of the rule |
  |---|---|
  | Atomicity | 6 · Partition § Atomicity |
  | Map complete; ids unique per application; every `Depends on:` id exists; DAG; nothing points forward | § Ids, § Order and dependencies |
  | One `DOC-1`, or the Overview's one-sentence absence; every chain reaches it | § Types, § Order and dependencies 4 |
  | `DOC-1` is the only documentation writer; shared code regions declare a dependency | § Order and dependencies 7–8 |
  | Every file a section edits sits under its application's directory | § Types |
  | Branch unique; PR title form | § Branch and PR title |
  | Fences | 7 · Draft § Skeleton |
  | Register; no optionality | § Writing rules 1–2 |
  | Citations; new symbols carry search evidence | § Writing rules 3 |
  | Strings; contract changes; renames and removals | § Writing rules 4–6 |
  | Language level; build output; tracked jars; dependencies; no version edit | § Writing rules 7–8, the loaded template's `## Rules` |
  | Edge-case ACs | § Writing rules 10 |
  | Placement; location; setup; every AC tested or marked | § Test rules |
  | Names verified at their definition; inventories extracted; capability claims verified; no coding-time existence check | 5 · Ground § Grounding rules |
  | Per-type rules | the loaded template's `## Rules` |
  | No guard removed without a recorded decision; zero questions remain | § Question rule, Invariant 4 |
  | No model name; `**Complexity:**` is the one level mention | Invariant 5 |
  | CHANGELOG entry in `DOC-1`, with no issue link; H-1 (no `file.md:NNN`), H-3 (no AC that only observes a stub), and the rest of the list | `house-conventions.md` |

  **Hedge grep**, case-insensitive, over the sections in `scope`. `|` separates alternatives; the
  spaces around it are not part of a pattern:

  ```
  confirm with | before writing | before editing | if it does not |
  identify it with | verify .* exists | say which | consider | if appropriate | \bor\b
  ```

  Every hit is removed, or justified. A `\bor\b` hit inside a quoted string is justified by that
  fact; a hit in instruction prose is never justified. The grep and its output never appear in
  the spec or the issue.

## 9 · Verify

- **Pre-flight:** `scope` empty → skip.
- **Do:** prove the working text, section by section over `scope`, in two parts. This step
  answers one question: can the `normal` coder execute this text without guessing and without
  interpreting it? The bar does not move with `R.level`.

  ### Part 1 — falsification

  Walk the written text, not the memory of researching it. Every claim is false until a command
  run during this step proves it.

  | Claim | Proof |
  |---|---|
  | an `existing` symbol | open its definition now; compare name, parameter types, modifiers and field names character by character. A paraphrase an instruction is derived from fails. |
  | a `new` symbol | run the recorded search terms verbatim now. A hit falsifies the claim. |
  | a contract-change table | re-run the caller search now; every hit has a row, no row is missing |
  | a language or API level | open the build file now; the `source` / `target`, or `compileOptions` / `minSdk`, read what the section states |
  | a red-test row | `amend` — the named test exists; `add to` — the file exists; `new file` — it does not. The assertion sketch states nothing the cited source disproves. |
  | a documentation edit in `DOC-1` | the file exists; the `§` anchor is in the current doc; the text to change is there verbatim; the CHANGELOG version heading exists and `git tag -l v<version>` prints nothing |
  | an instruction naming a method or call site to edit | open that site now; it contains what the instruction says it contains |
  | a rename or removal of a key, a string or a resource name | re-run the search for it across every application now; the section names every hit with its new handling |
  | the section as a whole | no two statements disagree; no instruction disagrees with the citation it leans on |

  **Ledger.** Build a table `claim | command run | output excerpt | verdict`, one row per claim.
  A claim with no command fails. Session memory, an earlier turn's output and a subagent's report
  are not commands. The ledger is working material: it never reaches the draft, the spec, the
  issue or this run's output.

  A failed claim → re-ground it (Grounding rules), rewrite the sentence, walk the section again.

  ### Part 2 — comprehension walk

  Skip for a section that is in `scope` only through a defect of `Class:` `mechanical` or
  `oversight`. Otherwise, for every numbered instruction under `#### Implementation details` and
  every red-test row, write one checklist line from the spec text alone: the file the coder
  opens, the exact change, what it runs, the outcome it expects. Then ask of each line:

  | Question | Failure |
  |---|---|
  | Did writing the line need an inference? | yes |
  | Do the same words fit a second target, value, file or approach? | yes |
  | Does the approach break a rule of the section's template, or a standing project rule (Writing rule 8)? Quote the rule. | the approach breaks it |

  A failed line → rewrite the instruction into the one reading that survives, citing the fact or
  rule that decided it; re-ground first when the fix turns on an unverified fact; ask the three
  questions of the rewritten line. A section is presented only when every line survives
  every part that applies to it.

## 10 · Present

- **Pre-flight:** `obs.spec` is a path and the working text is byte-identical to it → set
  `unchanged`; skip.
- **Do:**
  1. Write the working text to the draft, replacing any file at that path, from `<!-- enriched-spec:start v2 -->` through
     `<!-- enriched-spec:end -->`.
  2. `R.inputs` non-empty → write the resolution comment to
     `/tmp/improve-issue-<n>-resolution.md`. Empty → delete that path.
  3. When `obs.spec` is a path, build the **reading aids**: one change line per section
     (`COM-1 unchanged · CLI-2 rewritten (defect 2481937461) · AND-1 new`), then the feedback item
     lines exactly as the resolution comment carries them. When any Section Map row has an open
     PR (`obs.prs`), add one line naming those sections: their branches' local ticks will conflict
     with this rewrite.
  4. `mode = non-interactive` → nothing more; no verdict is set.
  5. `mode = interactive` → emit the draft verbatim in chat — never a summary, an outline or
     "unchanged sections omitted" — then the reading aids. Call `ExitPlanMode` with that same
     text, prefixed by the line `Store this enriched spec for issue #<n>.`

     | Result | `verdict` |
     |---|---|
     | approved | `approved` |
     | rejected, with text | `changes`; `R.changes` = that text |
     | rejected, no text | `rejected` |

- **Writes:** `unchanged`, `aids`, `verdict`, `changes`.

## 11 · Store

- **Pre-flight:** `verdict` is not `approved` → skip.
- **Do:** invoke `spec-store <n> write /tmp/improve-issue-<n>-spec.md`, adding
  `/tmp/improve-issue-<n>-resolution.md` when that file exists. `spec-store` drives the whole
  branch → PR → checks → `merge-pr` → mirror sequence and returns one terminal line. The draft is
  stored as approved, byte for byte: nothing is redrafted, reformatted or regenerated after the
  approval. Branch on the terminal line:

  | `spec-store` returns | Action |
  |---|---|
  | `STATUS: OK <sha>` | `stored = <sha>` |
  | `STATUS: BLOCKED — concurrent-write` | the spec moved while this run drafted. Run `spec-load <n>` again; its file becomes the working text; set `verdict = changes`, `R.changes = re-apply this run's changes onto the spec now on master` |
  | `STATUS: BLOCKED — issue-unreadable` | the spec is stored; the mirror or the resolution comment is not. Invoke `spec-store`'s `--finish` repair once more. A repeat → **stop** `relay` |
  | `STATUS: BLOCKED — merged-section-modified` | **stop** `relay`, naming the merged section. The user decides: revert that section's change, or amend shipped history deliberately |
  | `STATUS: BLOCKED — spec-pr-open <url>` / `spec-pr-checks-failing <url>` / `spec-pr-checks-pending <url>` / `spec-merge-failed` | **stop** `relay`. The spec is **not** stored: its PR is still open or was refused. Nothing may be coded against it; the user resolves the PR first |
  | any other `STATUS: BLOCKED` | **stop** `relay` |

  Never trim a draft to fit a size limit. The spec file has room; length is governed by Writing
  rule 1 alone.

- **Writes:** `stored`, `verdict`, `changes`.

## 12 · Settle

- **Pre-flight:** none — always runs. The only step that ends the run or starts another pass.
- **Loads:** `../shared/escalation.md`, only to compose a `NEEDS_DECISION`.
- **Do:** first match wins.

  | # | Condition on `R` | Then |
  |---|---|---|
  | 1 | `halt.kind = relay` | output the payload verbatim; end |
  | 2 | `halt.kind = decision` | output one `NEEDS_DECISION` block; end |
  | 3 | `halt.kind = blocked` | output the facts, then `STATUS: BLOCKED — <slug>` as the last line; end |
  | 4 | `unchanged` | delete the draft file, if any; output `Nothing to change: the spec already says what issue #<n> owes.`; end |
  | 5 | `mode = non-interactive` | output `R.aids`, when set, then `STATUS: SPEC_PENDING` as the last line; end. The draft is not emitted: the launcher reads it at its path |
  | 6 | `verdict = changes` | clear `verdict`; add to `scope` every `<SEC-ID>` `R.changes` names, or every section whose PR is not merged when it names none; go to step 5 |
  | 7 | `verdict = rejected` | output `Nothing stored. Draft kept at /tmp/improve-issue-<n>-spec.md.`; end |
  | 8 | `stored` set | delete the draft file; output the summary below; end |

  ```
  ✅ Sectioned spec stored for issue #<n> — <1|0> Documentation · <per application: count and code, e.g. 2 CLI · 1 AND> sections.
  📄 Documentation edits prescribed in DOC-1: <the files it edits, or "none — the issue owes no doc edit">
  ❓ Questions resolved with the user: <R.answered, or "none">
  ⚠️ <sections with an open PR whose local ticks will conflict with this rewrite, or omit the line>
  👉 Next: code-issue <n> <SEC-ID>  (Coder: <model+effort>) — the first section whose dependencies are merged.
  ```

  Rows 1–5 and 7 store nothing and post nothing.
