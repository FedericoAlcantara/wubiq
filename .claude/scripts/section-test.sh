#!/usr/bin/env bash
# Runs a targeted test of one application for `code-issue` and counts it against a per-spawn
# budget, so a long debugging session ends in a checkpoint instead of growing to 500k of
# context. A spawn cannot see its own context size; it can be made to run every targeted test
# through here, and this script counts them.
#
# The application and what it depends on are compiled first (a compile error counts as a run:
# it is the red the test was after).
#
# Usage: section-test.sh [--reset] [<APP> <test>...]
#   --reset alone   deletes the counter                 -> `BUDGET: 0/<budget>` only, exit code 0
#   --reset + args  deletes the counter, then runs the tests below
#   <APP>           a code from apps.tsv (CLI, WIT, AND, …)
#   <test>          Ant applications: fully qualified JUnit class names
#                   Gradle applications: a `--tests` filter (class, class.method, or a pattern)
#   no <test>       refuses: the full suite is not a targeted run
#
# Output contract (the test output is streamed unchanged; the LAST TWO lines are):
#   under budget  -> `BUDGET: <k>/<budget>`,                                              `EXIT: <exit code>`
#   at/over it    -> `BUDGET: EXHAUSTED — checkpoint now (code-issue Step 6 § Checkpoint)`, `EXIT: <exit code>`
#   refused       -> `EXIT: 64`
#
# The budget is counted per branch in /tmp/section-budget-<branch, / -> ->; it is reset by
# `code-issue` at the start of each spawn, so a re-spawn gets a fresh one.
set -uo pipefail

# The only place the number lives; tune it when a measurement says so.
budget=6

# shellcheck source=/dev/null
. "$(dirname "$0")/apps.sh"

branch=$(git branch --show-current 2>/dev/null | tr '/' '-')
branch=${branch:-detached}
counter="/tmp/section-budget-${branch}"

reset_requested=false
if [ "${1:-}" = "--reset" ]; then
  reset_requested=true
  shift
  rm -f "$counter"
fi

if [ "$#" -eq 0 ] && [ "$reset_requested" = true ]; then
  echo "BUDGET: 0/${budget}"
  exit 0
fi

refuse() {
  echo "$1"
  echo "EXIT: 64"
  exit 64
}

[ "$#" -ge 2 ] || refuse "section-test.sh runs a targeted test: section-test.sh <APP> <test>..."
code="$1"
shift
case " $APP_CODES " in *" $code "*) ;; *) refuse "Unknown application '$code'; apps.tsv has: $APP_CODES" ;; esac
single=$(app_field "$code" single)
[ "$single" != "-" ] || refuse "$code has no targeted test command in apps.tsv."

exit_code=0
for dep in $(apps_closure "$code"); do
  # The closure also holds dependents; only the application and what it depends on are needed.
  [ "$dep" = "$code" ] || case ",$(app_field "$code" deps)," in *",$dep,"*) ;; *) continue ;; esac
  command=$(app_field "$dep" compile)
  [ "$command" = "-" ] && continue
  app_run "$dep" "$command" || { exit_code=$?; break; }
done

if [ "$exit_code" -eq 0 ]; then
  tests=$(printf '%q ' "$@")
  app_run "$code" "${single//\{\}/$tests}"
  exit_code=$?
fi

runs=$(cat "$counter" 2>/dev/null || echo 0)
case "$runs" in
  '' | *[!0-9]*) runs=0 ;;
esac
runs=$((runs + 1))
echo "$runs" >"$counter"

if [ "$runs" -ge "$budget" ]; then
  echo "BUDGET: EXHAUSTED — checkpoint now (code-issue Step 6 § Checkpoint)"
else
  echo "BUDGET: ${runs}/${budget}"
fi
echo "EXIT: ${exit_code}"
exit "$exit_code"
