#!/usr/bin/env bash
# Compiles one source tree with the javac of JAVA_HOME, for the applications whose Ant build has
# no target that compiles without also packaging and copying jars into other modules.
#
# Usage: javac-check.sh <out-dir | -> <src-dir> <classpath> [<javac flags>...]
#   out-dir   where the classes go, emptied first so no stale class survives; `-` compiles
#             into a scratch directory that is deleted, for an application nothing else
#             compiles against
#   classpath colon-separated, may be empty; `dir/*` entries are expanded by javac
#
# Prints javac's output and exits with javac's exit code.
set -uo pipefail

if [ "$#" -lt 3 ]; then
  echo "usage: javac-check.sh <out-dir | -> <src-dir> <classpath> [<javac flags>...]"
  exit 64
fi

out="$1"
src="$2"
classpath="$3"
shift 3

scratch=""
if [ "$out" = "-" ]; then
  scratch=$(mktemp -d)
  out="$scratch"
else
  rm -rf "$out"
fi
mkdir -p "$out"

sources=$(mktemp)
find "$src" -name '*.java' >"$sources"

"${JAVA_HOME:?JAVA_HOME is not set}/bin/javac" -nowarn -encoding UTF-8 -d "$out" -cp "$classpath" "$@" @"$sources"
exit_code=$?

rm -f "$sources"
[ -n "$scratch" ] && rm -rf "$scratch"
exit "$exit_code"
