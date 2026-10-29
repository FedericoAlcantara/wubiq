# Finds the installed JDKs, one per major version. Sourced by the dot-matrix test scripts.
#
# Sets:
#   SELECTED      lines of "major|home|version", one per major version, in version order
#   ALL_JDKS      the same, one per distinct version (for example both 1.8.0_112 and 1.8.0_362)
#   SEEN          " 6 8 11 ... " for membership checks
#   COMPILE_HOME  a JDK 8 with javac (empty when there is none)
#
# WUBIQ_JDKS (space-separated JDK homes) replaces the search.
# Compatible with the bash 3.2 shipped with macOS.

major() {
	# "1.6.0_65" -> 6, "11.0.18" -> 11, "25.0.2" -> 25
	local version="$1"
	case "$version" in
		1.*) echo "$version" | cut -d. -f2 ;;
		*) echo "$version" | cut -d. -f1 | cut -d- -f1 | cut -d+ -f1 ;;
	esac
}

java_version() {
	"$1/bin/java" -version 2>&1 | grep -i version | head -1 | sed -E 's/.*version "([^"]+)".*/\1/'
}

candidate_homes() {
	if [ -n "${WUBIQ_JDKS:-}" ]; then
		for home in $WUBIQ_JDKS; do echo "$home"; done
		return
	fi
	/usr/libexec/java_home -V 2>&1 | grep -E '^\s+[0-9]' | grep -v 'Internet Plug-Ins' | awk '{print $NF}'
	for home in /Library/Java/JavaVirtualMachines/*/Contents/Home "$HOME"/Library/Java/JavaVirtualMachines/*/Contents/Home \
			/opt/homebrew/opt/openjdk*/libexec/openjdk.jdk/Contents/Home /usr/local/opt/openjdk*/libexec/openjdk.jdk/Contents/Home /usr/lib/jvm/*; do
		[ -x "$home/bin/java" ] && echo "$home"
	done
}

# One JDK per major version, in version order.
SELECTED=""
SEEN=" "
ALL_JDKS=""
SEEN_VERSIONS=" "
for home in $(candidate_homes); do
	[ -x "$home/bin/java" ] || continue
	version="$(java_version "$home")"
	[ -n "$version" ] || continue
	m="$(major "$version")"
	case "$SEEN_VERSIONS" in *" $version "*) ;; *)
		SEEN_VERSIONS="$SEEN_VERSIONS$version "
		ALL_JDKS="$ALL_JDKS$m|$home|$version
"
	;; esac
	case "$SEEN" in *" $m "*) continue ;; esac
	SEEN="$SEEN$m "
	SELECTED="$SELECTED$m|$home|$version
"
done
SELECTED="$(printf '%s' "$SELECTED" | sort -t'|' -k1,1n)"
ALL_JDKS="$(printf '%s' "$ALL_JDKS" | sort -t'|' -k1,1n -k3,3)"

COMPILE_HOME=""
while IFS='|' read -r m home version; do
	if [ "$m" = "8" ] && [ -x "$home/bin/javac" ]; then COMPILE_HOME="$home"; fi
done <<EOF
$SELECTED
EOF
