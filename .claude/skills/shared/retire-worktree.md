# Retiring a section worktree

Single source of truth for removing a section worktree. Referenced from `merge-pr`
(`../merge-pr/SKILL.md` Step 2 step 4), its only caller; never duplicated into it.
`epic-orchestrator` retires a section worktree only by invoking `merge-pr`.

This is the mechanism only. Deciding *when* a worktree is due for retirement, and
*finding* its path, are the caller's job: `merge-pr` locates it by matching
`git worktree list --porcelain`'s `branch refs/heads/<branch>` against the PR it just
confirmed `MERGED`. It retires only **after** a merge is confirmed, never before — that
invariant is the caller's to keep, not this doc's to enforce.

Section worktrees live at `.claude/worktrees/section-<n>-<sec>` inside the main
checkout (gitignored), so they are found from `git worktree list` and never by
guessing a path. A spec PR (`spec/<n>-enriched-spec`) has no worktree; for it the
removal below is a no-op.

```bash
if [ -d "$WT" ]; then
  git worktree remove "$WT"        # never --force
fi
```

- **Nothing outlives the directory.** A section's build output and its copied
  `local.properties` are inside the worktree and ignored by git, so removing the worktree
  removes them. No database, port or cache is allocated per section.
- **A missing `$WT` is a no-op.** No worktree ever existed for this branch, or a previous
  run already retired it.
- **A refused removal is reported and skipped, never forced.** `git worktree remove`
  refuses a tree with modified or untracked files that git does not ignore, or a lock
  naming a live pid; the caller reports the path and moves on. `--force` can discard
  uncommitted work sitting in that worktree — never used here.
