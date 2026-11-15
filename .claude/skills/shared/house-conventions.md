# House conventions

Standing corrections the user has given, which no lint check enforces. They are
small, they recur, and each one that slips through costs a full pipeline round
trip — which is the entire reason this file exists.

`improve-issue` checks its working text against this list in its **Check**
step, before presenting the draft. `review-issue` sweeps the branch diff against
it in its **Step 4** light-fix pass. In `review-issue`, a hit here is
**REVIEWER-FIXED** unless it changes behaviour: the reviewer corrects it and
reports it, rather than spending a `code-issue` iteration on a word.

The `H-` numbers are stable identifiers other skills cite; a gap in the sequence is a retired
rule, never a slot to reuse.

---

## H-1 · Cite documentation by section, never by line

In anything written into an issue, a spec, a plan, a README, or a code comment,
reference `README.md` files and `CHANGELOG.md` by their **section or heading name**:

- ✅ `wubiq-client/README.md § Dot-matrix tests`
- ❌ `wubiq-client/README.md:45`

Documentation moves; a line number captured in a spec is stale within a few merges and
then points confidently at the wrong paragraph.

**Code `file:line` references are the opposite — required, and verifiable by
grep.** `improve-issue`'s citations demand them. The restriction is
documentation files only. For a build or config file the issue is itself about to edit,
cite the named entry (the Ant target, the Gradle block) rather than its line.

## H-3 · No tests for stubs or placeholders

Never spec, write, or demand a test for:

- a method that unconditionally returns a hardcoded value (`null`, an empty list) until
  real behaviour exists;
- a screen or page that shows only a static placeholder with no real content.

Tests land when the real behaviour lands. A test that confirms a stub returns its
hardcoded result verifies nothing.

Consequence for `improve-issue`: an AC whose red-test row would only observe a
placeholder is not an AC — drop it, or defer it to the issue that builds the
real thing. Consequence for `review-issue`: never raise missing coverage
against a stub.

## H-4 · Every feature and fix carries a CHANGELOG entry, without an issue link

`CHANGELOG.md` is one file for every application. A feature or fix gets a `-` bullet under
the `### Wubiq-<module>` subheading of the version being prepared (`## Wubiq X.Y.Z`); the
spec's `DOC-1` names both headings. The new entry has **no** issue reference (no `(#22)`);
older entries keep theirs — do not copy it, do not strip it.

`improve-issue` prescribes the entry verbatim in `DOC-1`; `code-issue` transcribes it;
`review-issue` raises a missing entry as a gap. A section outside `DOC-1` never edits the file.

## H-6 · Never touch the human review labels

`approved` and `amends-required` are human-only gates on a PR. Never add or
remove either — not when CI is green, not when the work was self-reviewed, not
when asked to "finish" or "unblock" a PR. An `amends-required` label with no
review comments is not feedback to go chase.

**Writing is what this forbids; reading is the point of the labels.** They exist
to be acted on: `amends-required` sends a section back through the pipeline, and
`approved` is the sole authority under which `merge-pr` merges a PR (a spec PR,
recognised by its fixed name and sole file, is the one exemption). Acting on a
label a human wrote is not touching it. Writing one — to unblock yourself, to
record your own approval, or to clear an `amends-required` you believe you have
satisfied — is, and stays forbidden in every skill including `merge-pr`.

## H-7 · A README states what is true now

A `README.md` holds current facts and the steps to do something. It carries no history
("previously", "used to", "as of 2.7"), no issue or PR references, and no explanation of
approaches that were rejected. What changed belongs in `CHANGELOG.md`; why belongs in the
issue.

## H-8 · No test asserts a version string

Never spec, write or demand a test whose assertion is a version number
(`Labels.VERSION`, `versionName`, `versionCode`). It fails on every release and verifies
nothing about behaviour. Version numbers are release work, not section work.
