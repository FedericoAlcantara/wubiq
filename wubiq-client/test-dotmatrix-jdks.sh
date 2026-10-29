#!/bin/bash
# Runs the dot-matrix witness suite on every installed JDK and compares the results.
#
#   ./test-dotmatrix-jdks.sh [--strict]
#
# - Compiles once with a JDK 8 (javac 12+ can't target Java 6).
# - Runs DotMatrixWitnessTest on Java 6, 8, 11, 17, 21 and any Homebrew OpenJDK found,
#   or on the JDK homes listed in WUBIQ_JDKS (space separated).
# - Writes build-tests/dotmatrix-report/index.html with a fixture x JDK matrix and
#   converts job.ps (what a laser printer would print) to PDF when Ghostscript is installed.
# - --strict: exit 1 when a layout check fails or a JDK's bytes differ from Java 8's.
#   Without it the script only reports (exit 0).
#
# Compatible with the bash 3.2 shipped with macOS.

set -u
cd "$(dirname "$0")"
BASE="$(pwd)"
COMMON="$BASE/../wubiq-common"
WITNESS="$BASE/../wubiq-dotmatrix-witness"
REPORT="$BASE/build-tests/dotmatrix-report"
STRICT=false
[ "${1:-}" = "--strict" ] && STRICT=true

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
for home in $(candidate_homes); do
	[ -x "$home/bin/java" ] || continue
	version="$(java_version "$home")"
	[ -n "$version" ] || continue
	m="$(major "$version")"
	case "$SEEN" in *" $m "*) continue ;; esac
	SEEN="$SEEN$m "
	SELECTED="$SELECTED$m|$home|$version
"
done
SELECTED="$(printf '%s' "$SELECTED" | sort -t'|' -k1,1n)"

COMPILE_HOME=""
while IFS='|' read -r m home version; do
	if [ "$m" = "8" ] && [ -x "$home/bin/javac" ]; then COMPILE_HOME="$home"; fi
done <<EOF
$SELECTED
EOF
if [ -z "$COMPILE_HOME" ]; then
	echo "A JDK 8 (with javac) is needed to compile for Java 6." >&2
	exit 2
fi

for wanted in 6 8 11 17 21; do
	case "$SEEN" in *" $wanted "*) ;; *) echo "Note: no Java $wanted found, skipped." ;; esac
done

echo "Compiling with $COMPILE_HOME"
JAVA_HOME="$COMPILE_HOME" ant -q -f "$BASE/build.xml" compile-tests || exit 2

CP="$BASE/bin:$BASE/i18n:$COMMON/bin:$COMMON/i18n:$BASE/build-tests/classes:$WITNESS/bin"
for jar in "$BASE"/lib/*.jar "$COMMON"/lib/*.jar "$COMMON"/lib-nondeploy/*.jar "$WITNESS"/lib-test/*.jar; do
	case "$jar" in */wubiq-common.jar) continue ;; esac
	CP="$CP:$jar"
done

TEST=net.sf.wubiq.dotmatrix.DotMatrixWitnessTest
SELF_CHECK=net.sf.wubiq.dotmatrix.ReferenceEncoderTest

run_java() {
	"$JAVA_BIN" -Djava.awt.headless=true $EXTRA -Dwubiq.dotmatrix.report="$OUT" -cp "$CP" "$@"
}

self_check() {
	run_java org.junit.runner.JUnitCore "$SELF_CHECK" && touch "$OUT/self-check.ok"
}

# Runs the command up to 5 times until the file exists (Java 6 crashes at random).
retry() {
	local done_file="$1"
	shift
	local attempt
	for attempt in 1 2 3 4 5; do
		"$@" >> "$LOG" 2>&1
		[ -f "$done_file" ] && return 0
	done
	return 1
}

rm -rf "$REPORT"
mkdir -p "$REPORT"
RUNS=""
while IFS='|' read -r m home version; do
	[ -n "$m" ] || continue
	OUT="$REPORT/java-$m"
	LOG="$REPORT/java-$m.log"
	JAVA_BIN="$home/bin/java"
	EXTRA=""
	mkdir -p "$OUT"
	echo "Java $version ($home)"
	if [ "$m" = "6" ]; then
		# Apple's Java 6 under Rosetta crashes at random: no JIT, no memory serialize page, and one JVM per case.
		EXTRA="-Xint -XX:+UseMembar"
		retry "$OUT/self-check.ok" self_check || echo "  reference self-check FAILED"
		missing=0
		for name in $("$COMPILE_HOME/bin/java" -Djava.awt.headless=true -cp "$CP" $TEST list 2>/dev/null); do
			retry "$OUT/$name/result.tsv" run_java -Dwubiq.dotmatrix.only="$name" org.junit.runner.JUnitCore $TEST \
				|| { missing=$((missing + 1)); echo "  $name: no result after 5 attempts"; }
		done
		retry "$OUT/summary.tsv" run_java $TEST index
		echo "  one JVM per case, $missing case(s) without a result"
	else
		run_java org.junit.runner.JUnitCore $SELF_CHECK $TEST > "$LOG" 2>&1
		status=$?
		grep -E '^(OK|Tests run)' "$LOG" | sed 's/^/  /'
		[ $status -gt 1 ] && echo "  (JVM exited with $status)"
	fi
	if [ -f "$OUT/summary.tsv" ]; then
		RUNS="$RUNS$m|$version
"
	else
		echo "  FAILED to run, see $LOG"
	fi
done <<EOF
$SELECTED
EOF

if command -v gs > /dev/null 2>&1; then
	find "$REPORT" -name job.ps | while read -r ps; do
		gs -q -dBATCH -dNOPAUSE -dSAFER -sDEVICE=pdfwrite -o "${ps%.ps}.pdf" "$ps" > /dev/null 2>&1
	done
fi

BASELINE=8
[ -f "$REPORT/java-$BASELINE/summary.tsv" ] || BASELINE="$(printf '%s' "$RUNS" | head -1 | cut -d'|' -f1)"

# Matrix: one row per fixture-driver, one column per JDK.
HTML="$REPORT/index.html"
{
	echo '<!doctype html><html><head><meta charset="utf-8"><title>Dot-matrix witness matrix</title>'
	echo '<style>body{font-family:sans-serif;margin:16px;background:#fff;color:#111}td,th{border-bottom:1px solid #ddd;padding:3px 8px;font-size:13px;text-align:left}table{border-collapse:collapse}.pass{color:#176b2c}.fail{color:#b00020}.diff{background:#fff1c2}</style></head><body>'
	echo "<h1>Dot-matrix witness matrix</h1><p>Cell: layout check result (passed/failed items) on that JDK, linking to its report. Shaded cells: bytes differ from Java $BASELINE.</p>"
	printf '<table><tr><th>Fixture / driver</th>'
	printf '%s' "$RUNS" | while IFS='|' read -r m version; do printf '<th><a href="java-%s/index.html">Java %s</a></th>' "$m" "$version"; done
	echo '</tr>'
} > "$HTML"

FAILED=0
DIFFERENT=0
printf '\n%-30s' "fixture-driver"
printf '%s' "$RUNS" | while IFS='|' read -r m version; do printf '%-16s' "java-$m"; done
echo
ROWS="$(tail -n +2 "$REPORT/java-$BASELINE/summary.tsv" | cut -f1,2)"
while IFS="$(printf '\t')" read -r fixture driver; do
	[ -n "$fixture" ] || continue
	key="$fixture-$driver"
	base="$(awk -F'\t' -v f="$fixture" -v d="$driver" '$1==f && $2==d {print $7}' "$REPORT/java-$BASELINE/summary.tsv")"
	line="$(printf '%-30s' "$key")"
	cells="<tr><td>$key</td>"
	while IFS='|' read -r m version; do
		[ -n "$m" ] || continue
		row="$(awk -F'\t' -v f="$fixture" -v d="$driver" '$1==f && $2==d' "$REPORT/java-$m/summary.tsv")"
		if [ -z "$row" ]; then
			line="$line$(printf '%-16s' 'missing')"
			cells="$cells<td>missing</td>"
			continue
		fi
		status="$(echo "$row" | cut -f3)"
		ok="$(echo "$row" | cut -f4)"
		failed="$(echo "$row" | cut -f5)"
		sha="$(echo "$row" | cut -f7)"
		same="same"
		class=""
		if [ "$sha" != "$base" ]; then
			same="DIFF"
			class=" diff"
			[ "$driver" != "reference" ] && DIFFERENT=$((DIFFERENT + 1))
		fi
		[ "$status" = "FAIL" ] && [ "$driver" != "reference" ] && FAILED=$((FAILED + 1))
		line="$line$(printf '%-16s' "$status $ok/$failed $same")"
		lower="$(echo "$status" | tr 'A-Z' 'a-z')"
		cells="$cells<td class=\"$lower$class\"><a href=\"java-$m/$key/index.html\">$status</a> $ok/$failed</td>"
	done <<EOF
$RUNS
EOF
	echo "$line"
	echo "$cells</tr>" >> "$HTML"
done <<EOF
$ROWS
EOF
echo '</table></body></html>' >> "$HTML"
echo
echo "Matrix: $HTML"
echo "Failing cases: $FAILED, cases whose bytes differ from Java $BASELINE: $DIFFERENT"

if $STRICT && { [ $FAILED -gt 0 ] || [ $DIFFERENT -gt 0 ]; }; then
	exit 1
fi
exit 0
