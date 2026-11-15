# Provisioning a section worktree

Loaded by `epic-orchestrator`'s Provision, only when the batch holds a section spawn.

One worktree per section, from its first spawn until `merge-pr` retires it. Path:
`.claude/worktrees/section-<n>-<sec>`, `<sec>` lowercased (`CLI-7` → `cli-7`). Gitignored.
Each section compiles into its own checkout's output directories, so parallel sections
never overwrite each other's classes.

**One section at a time.** Spawns are batched; provisioning is not. **Idempotent** — a path
already in `git worktree list` is returned as it is.

## Create

One call — never assemble `git worktree add` by hand. The script builds the path from
`<n>` and `<sec>` alone (the branch never enters it), validates its arguments, verifies from
inside the new worktree that `git rev-parse --show-toplevel` equals the expected path, and
copies the untracked files a build needs (`wubiq-android-gradle/local.properties`):

```bash
.claude/scripts/provision-worktree.sh <n> <SEC-ID> "<branch>"
```

Last line `STATUS: OK <absolute-path>` → use that path as the spawn's `WORKTREE:`. Last line
`STATUS: BLOCKED — <reason>` → set the row's `note: blocked: <reason>`, drop the section
from `M.batch`, carry on with the rest.

| Case | Why |
|---|---|
| detached when no branch exists | `code-issue` owns branch creation, including its `fresh run + branch exists` halt |
| on the branch when one exists | `pr-from-issue` never checks out; it asserts the current branch equals the Section Map's `Branch` cell |

`git worktree add` refuses a branch another worktree holds → set the row's
`note: blocked: branch-checked-out-elsewhere` naming the holder from
`git worktree list --porcelain`, drop the section from `M.batch`, carry on with the rest.

Nothing else is copied. Build output is not: the gate compiles every affected application
from empty output directories (`.claude/scripts/apps.tsv`), so a copied class could only
be stale.

Announce per section: `<SEC-ID> — worktree .claude/worktrees/section-<n>-<sec>`

## Never

- `--force`; creating a section branch; sharing a worktree or a branch between sections.
- A worktree for anything that is not a section — `address-pr-review`, `merge-pr` and
  closing-mode `review-issue` run in this agent's own checkout.
- Copying `bin/`, `build/`, `build-tests/` or `dist/` into a worktree.
- Retiring a worktree, here or anywhere in this agent, before its PR merges.
