# improve-issue · Documentation sections

Read by `improve-issue` **Draft** when a `DOC` section is in scope. Holds every rule about a
`DOC` section's content, once. Whether an application has one, and what depends on it, is
**Partition**'s.

A `DOC` section is one application's documentation for the issue: `<APP>-DOC-1`, `<APP>` the
application's registry code.

## Rules

1. `<APP>-DOC-1` edits only that application's documentation: its bullets in `CHANGELOG.md`,
   and the Markdown files under its directory (its `README.md`). Javadoc is code: it belongs
   to the section that edits the class, never to a `DOC` section.
2. `<APP>-DOC-1` prescribes every documentation edit the issue owes for that application. Every
   documentation file another section of the application cites as changed has a block here.
3. One `##### <path>` block per file edited, the path verbatim as the heading. A file the issue
   does not edit has no block.
4. Block order: `CHANGELOG.md`, then every other path alphabetically.
5. Inside a block: numbered edits, each anchored `§ Section name`, never a line number (H-1).
6. Every edit is complete and verbatim. `code-issue` transcribes it; it never composes.
7. `CHANGELOG.md` (H-4). The file is `## Wubiq X.Y.Z`, then `### Wubiq-<module>`, then `-`
   bullets. The block inserts this application's bullets under its own `### Wubiq-<module>`
   heading, adding that heading when the version has none, in the style of the entries already
   there. It touches no other application's heading: that is the other application's `DOC`
   section. An entry carries no issue reference.
8. The version heading. Take the topmost `## Wubiq <version>` and run `git tag -l v<version>`.
   No output → the version is still being prepared: the entry goes under it. A tag printed →
   that version is released: which heading the entry goes under is a question (Question rule).
   Every `DOC` section of the issue uses the same version heading.
9. What the common targets owe:

   | File | The block carries |
   |---|---|
   | `CHANGELOG.md` | the entry, complete, under the version heading rule 8 gives and the application's `### Wubiq-<module>` |
   | `<application>/README.md` | the command, option, table row or step the change makes stale or new |

10. A README states only current facts and steps: no history, no issue reference, no
    explanation of what was not done (H-7).
11. Never invent a name. When a documented name is wrong, prescribe the doc change and state the
    reason in the edit.
12. Doc content that contradicts an existing rule is a question (Question rule). It is never
    specced around.
13. Prescribed text carries no `file.md:NNN` reference (H-1).
14. AC-1 and AC-2 are fixed: verbatim, in that position, in every `DOC` section. AC-3 onward
    are issue-specific, at least one per `#####` block other than `CHANGELOG.md`.
15. No red tests. Every AC ends `(mechanical — no red test; verified by <command>)` and names
    the command.
16. An application has no `DOC` section only when its change in this issue is not user-visible
    (a chore, a build change with no effect on what ships, a refactor with no behaviour
    change). The Overview then says so in one sentence.
17. `Depends on:` is the previous application's `DOC` section in the Section Map, or `none` for
    the first: they all edit `CHANGELOG.md`.

## Template

`<…>` is a placeholder. The `#####` file blocks and AC-3 onward are examples of shape, not a
fixed set.

````markdown
<!-- section:<APP>-DOC-1:start -->
### <APP>-DOC-1 — Documentation · <application directory>
Depends on: <the previous application's DOC section | none>

#### Documentation references
<the § anchors this section edits or builds on>

#### Implementation details

##### CHANGELOG.md
1. § `## Wubiq <version>` › `### Wubiq-<module>` — insert verbatim:
   ```
   - <the entry text>
   ```

##### <application>/README.md
1. § <Section> — <exact edit>

##### Acceptance criteria
- [ ] AC-1: the CHANGELOG entry sits under `## Wubiq <version>` › `### Wubiq-<module>` and
      carries no issue reference (mechanical — no red test; verified by
      `git diff origin/master...HEAD -- CHANGELOG.md | grep -E '^\+.*\(#[0-9]+\)'` returning nothing)
- [ ] AC-2: nothing but this application's documentation modified, apart from this issue's
      spec file (its AC ticks) (mechanical — no red test; verified by
      `git diff --name-only origin/master...HEAD | grep -vE '^CHANGELOG\.md$|^<application directory>/.*\.md$|^specs/issue-<n>-enriched-spec\.md$'`
      returning nothing)
- [ ] AC-3: `<application>/README.md § <Section>` reads as prescribed (mechanical — no red test;
      verified by `<the grep>`)

##### Green checks
1. `.claude/scripts/gate.sh` clean (code-issue); `.claude/scripts/suite.sh` — full suite green
   (review-issue runs the suite)
<!-- section:<APP>-DOC-1:end -->
````
