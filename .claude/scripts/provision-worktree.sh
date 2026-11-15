#!/usr/bin/env bash
# Create ONE section worktree at .claude/worktrees/section-<n>-<sec>.
#
#   provision-worktree.sh <issue-number> <SEC-ID> <branch>
#
# The path is built here from <n> and <sec> only; the branch never takes part in it, so
# a branch name cannot leak into the path. Every argument is validated and the result
# is verified before success.
#
# Files a build needs that git does not track are copied from this checkout
# (UNTRACKED_FILES below); a missing source is skipped.
#
# Last line of stdout: `STATUS: OK <absolute-path>` or `STATUS: BLOCKED — <reason>`.

set -u

issue_number="${1:-}"
section_id="${2:-}"
branch="${3:-}"

blocked() {
  echo "STATUS: BLOCKED — $1"
  exit 1
}

[[ "$issue_number" =~ ^[0-9]+$ ]] || blocked "bad-issue-number"
[[ "$section_id" =~ ^[A-Za-z]+-[A-Za-z]+-[0-9]+$ ]] || blocked "bad-section-id"
[[ "$branch" =~ ^[A-Za-z0-9._/-]+$ ]] || blocked "bad-branch"

section_slug="$(printf '%s' "$section_id" | tr '[:upper:]' '[:lower:]')"
repo_root="$(git rev-parse --show-toplevel)" || blocked "not-a-git-checkout"
worktree_path="$repo_root/.claude/worktrees/section-$issue_number-$section_slug"

if git worktree list --porcelain | grep -Fxq "worktree $worktree_path"; then
  echo "STATUS: OK $worktree_path"
  exit 0
fi

git fetch origin --quiet

if git rev-parse --verify --quiet "refs/heads/$branch" >/dev/null; then
  git worktree add "$worktree_path" "$branch" >&2 || blocked "worktree-add-failed"
elif git rev-parse --verify --quiet "refs/remotes/origin/$branch" >/dev/null; then
  git worktree add --track -b "$branch" "$worktree_path" "origin/$branch" >&2 || blocked "worktree-add-failed"
else
  git worktree add --detach "$worktree_path" origin/master >&2 || blocked "worktree-add-failed"
fi

actual_root="$(git -C "$worktree_path" rev-parse --show-toplevel 2>/dev/null)"
[ "$actual_root" = "$worktree_path" ] || blocked "worktree-entry-unverified"

# local.properties holds the Android SDK path; without it no Gradle task runs.
UNTRACKED_FILES="wubiq-android-gradle/local.properties"
for file in $UNTRACKED_FILES; do
  if [ -f "$repo_root/$file" ] && [ ! -e "$worktree_path/$file" ]; then
    cp "$repo_root/$file" "$worktree_path/$file" || blocked "worktree-copy-failed"
  fi
done

echo "STATUS: OK $worktree_path"
