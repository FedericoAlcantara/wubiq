# Reads the application registry (apps.tsv). Sourced by gate.sh, suite.sh and section-test.sh.
# Compatible with the bash 3.2 shipped with macOS.
#
# Sets:
#   WUBIQ_ROOT     the working tree root
#   WUBIQ_SCRIPTS  this directory (exported; registry commands refer to it)
#   APP_CODES      registry codes, space-separated, in dependency order
#
# Functions:
#   app_field <code> <column>   column is one of dir toolchain jdk deps compile test single
#   app_of_path <path>          the code of the application that owns a repo-relative path
#   apps_closure <codes...>     the codes plus everything depending on them, plus what all of
#                               those depend on, in registry order
#   app_java_home <code>        the JDK home for the application's jdk column (`-`: none needed)
#   app_run <code> <command>    runs a registry command in the application's directory with
#                               JAVA_HOME set; returns the command's exit code

WUBIQ_ROOT=$(git rev-parse --show-toplevel)
WUBIQ_SCRIPTS="$WUBIQ_ROOT/.claude/scripts"
export WUBIQ_SCRIPTS
APPS_TSV="$WUBIQ_SCRIPTS/apps.tsv"

APP_CODES=$(grep -v '^#' "$APPS_TSV" | cut -f1 | tr '\n' ' ')

app_field() {
  local index
  case "$2" in
    dir) index=2 ;; toolchain) index=3 ;; jdk) index=4 ;; deps) index=5 ;;
    compile) index=6 ;; test) index=7 ;; single) index=8 ;;
    *) return 64 ;;
  esac
  awk -F'\t' -v code="$1" -v i="$index" '$1 == code { print $i }' "$APPS_TSV"
}

app_of_path() {
  local code dir
  for code in $APP_CODES; do
    dir=$(app_field "$code" dir)
    case "$1" in "$dir"/*) echo "$code"; return 0 ;; esac
  done
  return 1
}

apps_closure() {
  local wanted=" $* " changed code dep other
  # Everything that depends on a wanted application is wanted too.
  changed=true
  while [ "$changed" = true ]; do
    changed=false
    for code in $APP_CODES; do
      case "$wanted" in *" $code "*) continue ;; esac
      for dep in $(app_field "$code" deps | tr ',' ' '); do
        case "$wanted" in *" $dep "*) wanted="$wanted$code "; changed=true; break ;; esac
      done
    done
  done
  # What a wanted application depends on has to be compiled before it.
  changed=true
  while [ "$changed" = true ]; do
    changed=false
    for code in $APP_CODES; do
      case "$wanted" in *" $code "*) ;; *) continue ;; esac
      for dep in $(app_field "$code" deps | tr ',' ' '); do
        [ "$dep" = "-" ] && continue
        case "$wanted" in *" $dep "*) ;; *) wanted="$wanted$dep "; changed=true ;; esac
      done
    done
  done
  for other in $APP_CODES; do
    case "$wanted" in *" $other "*) printf '%s ' "$other" ;; esac
  done
}

app_java_home() {
  local jdk home major
  jdk=$(app_field "$1" jdk)
  if [ "$jdk" = "android" ]; then
    # The Android Gradle plugin needs a newer JDK than the Ant modules.
    for home in "${WUBIQ_ANDROID_JAVA_HOME:-}" "/Applications/Android Studio.app/Contents/jbr/Contents/Home"; do
      if [ -n "$home" ] && [ -x "$home/bin/java" ]; then echo "$home"; return 0; fi
    done
    jdk=newest
  fi
  if [ -z "${SELECTED:-}" ]; then
    # shellcheck source=/dev/null
    . "$WUBIQ_ROOT/wubiq-client/jdk-homes.sh"
  fi
  if [ "$jdk" = "newest" ]; then
    printf '%s\n' "$SELECTED" | tail -n 1 | cut -d'|' -f2
    return 0
  fi
  while IFS='|' read -r major home _; do
    if [ "$major" = "$jdk" ] && [ -x "$home/bin/javac" ]; then echo "$home"; return 0; fi
  done <<EOF
$SELECTED
EOF
  return 1
}

app_run() {
  local code="$1" command="$2" java_home=""
  if [ "$(app_field "$code" jdk)" != "-" ]; then
    java_home=$(app_java_home "$code") || {
      echo "No JDK $(app_field "$code" jdk) with javac was found for $code (set WUBIQ_JDKS to list JDK homes)."
      return 69
    }
  fi
  (
    cd "$WUBIQ_ROOT/$(app_field "$code" dir)" || exit 66
    if [ -n "$java_home" ]; then
      export JAVA_HOME="$java_home"
      export PATH="$JAVA_HOME/bin:$PATH"
    fi
    eval "$command"
  )
}
