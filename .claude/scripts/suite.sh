#!/usr/bin/env bash
# Runs the FULL suite: the headless tests of every application in apps.tsv that has any, with
# the same log/verdict shape as gate.sh, so it can be started with `run_in_background` and
# waited on with `wait-verdict.sh suite` instead of being polled. A targeted run goes through
# section-test.sh.
#
# Each application is compiled first, with what it depends on, so the suite also runs in a
# fresh worktree.
#
# Usage: suite.sh     (any argument is refused)
#
# Output contract (the only thing ever printed to stdout; also written to
# /tmp/suite-<branch>.verdict, which is deleted at the start of each run):
#   success   -> one `<APP>: <summary>` line per application tested, then `EXIT: 0`
#   failure   -> `STAGE: <APP>:compile` or `STAGE: <APP>:test`, the log's last 60 lines,
#                then `EXIT: <code>`
#   refused   -> `EXIT: 64`
#
# Full output always lands in /tmp/suite-<branch>.log.
set -uo pipefail

# shellcheck source=/dev/null
. "$(dirname "$0")/apps.sh"
cd "$WUBIQ_ROOT" || exit 66

branch=$(git branch --show-current 2>/dev/null | tr '/' '-')
branch=${branch:-detached}
log="/tmp/suite-${branch}.log"
verdict="/tmp/suite-${branch}.verdict"
: >"$log"
rm -f "$verdict"

emit() {
  printf '%s\n' "$1" | tee -a "$verdict"
}

fail() {
  emit "STAGE: $1"
  emit "$(tail -n 60 "$log")"
  emit "EXIT: $2"
  exit "$2"
}

if [ "$#" -gt 0 ]; then
  emit "suite.sh runs the full suite; run a targeted test through section-test.sh <APP> <test>."
  emit "EXIT: 64"
  exit 64
fi

tested=""
for code in $APP_CODES; do
  [ "$(app_field "$code" test)" = "-" ] || tested="$tested $code"
done

# shellcheck disable=SC2086
for code in $(apps_closure $tested); do
  # A dependent with no tests of its own is in the closure but is not needed to run the tests.
  needed=false
  for other in $tested; do
    [ "$other" = "$code" ] && needed=true
    case ",$(app_field "$other" deps)," in *",$code,"*) needed=true ;; esac
  done
  [ "$needed" = true ] || continue

  command=$(app_field "$code" compile)
  if [ "$command" != "-" ]; then
    echo "=== $code: $command ===" >>"$log"
    app_run "$code" "$command" >>"$log" 2>&1
    exit_code=$?
    [ "$exit_code" -ne 0 ] && fail "$code:compile" "$exit_code"
  fi
done

summaries=""
for code in $tested; do
  command=$(app_field "$code" test)
  start=$(wc -l <"$log")
  echo "=== $code: $command ===" >>"$log"
  app_run "$code" "$command" >>"$log" 2>&1
  exit_code=$?
  [ "$exit_code" -ne 0 ] && fail "$code:test" "$exit_code"
  # JUnit prints `OK (N tests)`; a Gradle run prints no count.
  summary=$(tail -n +"$((start + 1))" "$log" | grep -E 'OK \([0-9]+ tests?\)' | tail -n 1 | sed -E 's/.*(OK \([0-9]+ tests?\)).*/\1/')
  summaries="$summaries$code: ${summary:-green}
"
done

emit "$(printf '%s' "$summaries")"
emit "EXIT: 0"
