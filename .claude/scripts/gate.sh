#!/usr/bin/env bash
# The gate: compiles every application a diff affects, sources and tests, one stage per
# application, so a failure is reported as its own STAGE with only that stage's last 40 log
# lines. Which applications exist, how each compiles and with which JDK is in apps.tsv.
#
# Affected = the applications owning a changed file, plus the ones depending on them. What an
# affected application depends on is compiled first, because it compiles against those classes.
#
# Usage: gate.sh [--all]     --all compiles every application, whatever changed
#
# Output contract (the only thing ever printed to stdout):
#   success                 -> `EXIT: 0`
#   failure                 -> `STAGE: <APP>:compile` (or `STAGE: pipeline:syntax`), then the
#                              log's last 40 lines, then `EXIT: <code>`
#   nothing to compile      -> `SKIPPED: no application affected`, then `EXIT: 0`
#
# Full stage-by-stage output always lands in /tmp/gate-<branch>.log. Everything printed to
# stdout is also written to /tmp/gate-<branch>.verdict, which `wait-verdict.sh gate` blocks on;
# the verdict is deleted at the start of each run, so its existence with a last line of
# `EXIT: <code>` means this run ended.
set -uo pipefail

# shellcheck source=/dev/null
. "$(dirname "$0")/apps.sh"
cd "$WUBIQ_ROOT" || exit 66

branch=$(git branch --show-current 2>/dev/null | tr '/' '-')
branch=${branch:-detached}
log="/tmp/gate-${branch}.log"
verdict="/tmp/gate-${branch}.verdict"
: >"$log"
rm -f "$verdict"

# Prints to stdout and appends to the verdict file, so a waiter sees the same lines.
emit() {
  printf '%s\n' "$1" | tee -a "$verdict"
}

fail() {
  emit "STAGE: $1"
  emit "$(tail -n 40 "$log")"
  emit "EXIT: $2"
  exit "$2"
}

# Three separate git calls rather than parsing `--porcelain`, which mishandles renames.
changed_files=$(
  {
    git diff --name-only origin/master...HEAD 2>/dev/null
    git diff --name-only HEAD 2>/dev/null
    git ls-files --others --exclude-standard 2>/dev/null
  } | sort -u
)

# Markdown and specs cannot change a compile or a test, wherever they sit. Anything else inside
# an application counts, images and text files included: they can be test fixtures.
code_files=$(printf '%s\n' "$changed_files" |
  grep -vE '(^|/)[^/]+\.md$|(^|/)LICENSE$|^specs/|^$')

if [ "${1:-}" = "--all" ]; then
  apps="$APP_CODES"
else
  touched=""
  while IFS= read -r file; do
    [ -n "$file" ] || continue
    code=$(app_of_path "$file") || continue
    case " $touched " in *" $code "*) ;; *) touched="$touched $code" ;; esac
  done <<EOF
$code_files
EOF
  apps=""
  # shellcheck disable=SC2086
  [ -n "$touched" ] && apps=$(apps_closure $touched)
fi

# The pipeline's own scripts have no application; a syntax error in one breaks every later run.
scripts=$(printf '%s\n' "$code_files" | grep -E '^\.claude/.*\.sh$')
if [ -n "$scripts" ]; then
  echo "=== pipeline: bash -n ===" >>"$log"
  while IFS= read -r script; do
    [ -f "$script" ] || continue
    bash -n "$script" >>"$log" 2>&1 || fail "pipeline:syntax" 1
  done <<EOF
$scripts
EOF
fi

if [ -z "$apps" ]; then
  emit "SKIPPED: no application affected"
  emit "EXIT: 0"
  exit 0
fi

for code in $apps; do
  command=$(app_field "$code" compile)
  [ "$command" = "-" ] && continue
  echo "=== $code: $command ===" >>"$log"
  app_run "$code" "$command" >>"$log" 2>&1
  exit_code=$?
  [ "$exit_code" -ne 0 ] && fail "$code:compile" "$exit_code"
done

emit "EXIT: 0"
