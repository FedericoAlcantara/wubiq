#!/bin/bash
# Prints wubiq-server's test page through a real client into the dot-matrix witness, and compares
# each job with tests-src/net/sf/wubiq/dotmatrix/golden/test-page-9pin.escp, the job printed by
# Java 6. It prints with three clients:
# - legacy-java-VERSION: ../wubiq-server/WebContent/wubiq-client.jar, the 2.7.4 jar wubiq-server
#   serves to the installed clients, on every installed JDK build (1.8.0_112 and 1.8.0_362 both run);
# - source-java-VERSION: build-tests/wubiq-client-launcher.jar (ant create-launcher-jar), the
#   compiled classes with the manifest of wubiq-client.jar, on the JDK 25 that compiled them;
# - packaged: the jars of an installed package, DIR of --packaged-app, on the package's own
#   runtime, JAVA of --packaged-java.
# The source and packaged jobs also get a row named ROW-extents: the witness measures the width of
# the job's text lines against tests-src/net/sf/wubiq/dotmatrix/golden/test-page.extents, the text
# extents of the test page PDF (extents in ../wubiq-dotmatrix-witness/README.md). The row reads
# NOT CHECKED when there is no extents file.
#
#   ./test-dotmatrix-e2e.sh [--update-golden] [--packaged-java JAVA --packaged-app DIR]
#
# Needs:
# - wubiq-server running (WUBIQ_SERVER, default http://localhost:8080);
# - the Witness_9-pin CUPS queue with the witness driver (../wubiq-dotmatrix-witness/README.md);
# - port 9100 free: the script starts and stops its own listener;
# - a JDK 25 with javac.
#
# While it runs, the witness queue is the default printer; the previous default is restored on exit.
# --update-golden prints with the 2.7.4 jar on Java 6 only and stores its job as the golden file.
# Exit codes: 0 every row prints like Java 6 and no extents row is EXTENTS DIFFERENT, 1 a row prints
# differently, doesn't print, or its text extents differ, 2 setup error (no server, no queue, port
# 9100 in use, no JDK 25 to compile, no 2.7.4 jar, --packaged-java without --packaged-app, no
# packaged client at JAVA and DIR, an unknown option).
#
# Compatible with the bash 3.2 shipped with macOS.

set -u
cd "$(dirname "$0")"
BASE="$(pwd)"
WITNESS="$BASE/../wubiq-dotmatrix-witness"
SERVER="${WUBIQ_SERVER:-http://localhost:8080}"
QUEUE="${WUBIQ_E2E_QUEUE:-Witness_9-pin}"
REPORT="$BASE/build-tests/dotmatrix-e2e"
GOLDEN="$BASE/tests-src/net/sf/wubiq/dotmatrix/golden/test-page-9pin.escp"
LEGACY_JAR="$BASE/../wubiq-server/WebContent/wubiq-client.jar"
EXTENTS="$BASE/tests-src/net/sf/wubiq/dotmatrix/golden/test-page.extents"
UPDATE=false
PACKAGED_JAVA=""
PACKAGED_APP=""
while [ $# -gt 0 ]; do
	case "$1" in
		--update-golden) UPDATE=true ;;
		--packaged-java) PACKAGED_JAVA="${2:-}"; [ $# -gt 1 ] && shift ;;
		--packaged-app) PACKAGED_APP="${2:-}"; [ $# -gt 1 ] && shift ;;
		*)
			echo "Unknown option: $1" >&2
			exit 2
			;;
	esac
	shift
done
if { [ -n "$PACKAGED_JAVA" ] && [ -z "$PACKAGED_APP" ]; } || { [ -z "$PACKAGED_JAVA" ] && [ -n "$PACKAGED_APP" ]; }; then
	echo "--packaged-java and --packaged-app go together." >&2
	exit 2
fi
if [ -n "$PACKAGED_JAVA" ] && { [ ! -x "$PACKAGED_JAVA" ] || [ ! -d "$PACKAGED_APP" ]; }; then
	echo "No packaged client at $PACKAGED_JAVA and $PACKAGED_APP." >&2
	exit 2
fi

. "$BASE/jdk-homes.sh"
if [ -z "$COMPILE_HOME" ]; then
	echo "A JDK 25 (with javac) is needed to compile the client." >&2
	exit 2
fi
if [ ! -f "$LEGACY_JAR" ]; then
	echo "No 2.7.4 jar at $LEGACY_JAR." >&2
	exit 2
fi
if [ "$(curl -s -o /dev/null -w '%{http_code}' "$SERVER/wubiq-server/")" != "200" ]; then
	echo "wubiq-server doesn't answer at $SERVER/wubiq-server/." >&2
	exit 2
fi
if ! lpstat -v "$QUEUE" 2>/dev/null | grep -q "socket://127.0.0.1:9100"; then
	echo "CUPS queue $QUEUE pointing at socket://127.0.0.1:9100 not found." >&2
	exit 2
fi
if lsof -nP -iTCP:9100 -sTCP:LISTEN > /dev/null 2>&1; then
	echo "Port 9100 is in use; stop the witness listener running there." >&2
	exit 2
fi
if ! $UPDATE && [ ! -f "$GOLDEN" ]; then
	echo "No golden file $GOLDEN; run with --update-golden first." >&2
	exit 2
fi

echo "Compiling with $COMPILE_HOME"
JAVA_HOME="$COMPILE_HOME" ant -q -f "$BASE/build.xml" compile-tests create-launcher-jar > /dev/null || exit 2
# Same manifest (Main-Class, Add-Opens) as wubiq-client.jar, over the compiled classes.
LAUNCHER="$BASE/build-tests/wubiq-client-launcher.jar"
WITNESS_JAVA="$COMPILE_HOME/bin/java -Djava.awt.headless=true -cp $WITNESS/bin net.sf.wubiq.dotmatrix.witness.Main"

LISTENER=""
CLIENT=""
PREVIOUS_DEFAULT="$(lpstat -d 2>/dev/null | sed -n 's/^system default destination: //p')"
cleanup() {
	[ -n "$CLIENT" ] && kill "$CLIENT" 2> /dev/null
	[ -n "$LISTENER" ] && kill "$LISTENER" 2> /dev/null
	if [ -n "$PREVIOUS_DEFAULT" ] && [ "$PREVIOUS_DEFAULT" != "$QUEUE" ]; then
		lpoptions -d "$PREVIOUS_DEFAULT" > /dev/null
	fi
}
trap cleanup EXIT
lpoptions -d "$QUEUE" > /dev/null

# Waits up to $1 seconds for the command in the remaining arguments to succeed.
wait_for() {
	local seconds="$1"
	shift
	local waited=0
	while [ $waited -lt "$seconds" ]; do
		"$@" > /dev/null 2>&1 && return 0
		sleep 1
		waited=$((waited + 1))
	done
	return 1
}

listening() {
	lsof -nP -iTCP:9100 -sTCP:LISTEN
}

job_written() {
	ls "$1"/job-*/index.html
}

# Test page link of the witness printer registered by client $1, from the server console.
test_page_link() {
	curl -s "$SERVER/wubiq-server/" | grep -o "wubiq-print-test.do?[^\"']*" | grep "uuid=$1" | grep -i "printServiceName=[^&]*witness" | head -1
}

has_link() {
	[ -n "$(test_page_link "$1")" ]
}

registered_or_gone() {
	has_link "$1" || ! kill -0 "$CLIENT" 2> /dev/null
}

# First exception the client logged, or a generic reason.
client_failure() {
	local reason
	reason="$(grep -m1 -E 'Exception|Error:' "$OUT/client.log" | sed 's/^Exception in thread "[^"]*" //')"
	[ -n "$reason" ] || reason="see $OUT/client.log"
	echo "$reason"
}

# Prints the test page with the client command in the arguments after the row name:
# print_with NAME COMMAND [ARG...]. Leaves the job in $OUT/jobs and returns 1 when none arrived.
print_with() {
	local name="$1"
	local uuid="witness-$name"
	shift
	rm -rf "$OUT"
	mkdir -p "$OUT/jobs"
	$WITNESS_JAVA listen --out "$OUT/jobs" > "$OUT/witness.log" 2>&1 &
	LISTENER=$!
	wait_for 15 listening || { echo "  the witness listener didn't start"; return 1; }
	( cd "$OUT" && exec "$@" ) > "$OUT/client.log" 2>&1 &
	CLIENT=$!
	local status=0
	wait_for 120 registered_or_gone "$uuid"
	if ! has_link "$uuid"; then
		if kill -0 "$CLIENT" 2> /dev/null; then
			echo "the client didn't register the witness printer; $(client_failure)" > "$OUT/failure.txt"
		else
			echo "the client stopped: $(client_failure)" > "$OUT/failure.txt"
		fi
		sed 's/^/  /' "$OUT/failure.txt"
		status=1
	else
		curl -s -o "$OUT/server-response.html" "$SERVER/wubiq-server/$(test_page_link "$uuid")"
		if wait_for 240 job_written "$OUT/jobs"; then
			sleep 2
		else
			echo "no job reached the witness; $(client_failure)" > "$OUT/failure.txt"
			sed 's/^/  /' "$OUT/failure.txt"
			status=1
		fi
	fi
	kill "$CLIENT" 2> /dev/null
	wait "$CLIENT" 2> /dev/null
	CLIENT=""
	kill "$LISTENER" 2> /dev/null
	wait "$LISTENER" 2> /dev/null
	LISTENER=""
	return $status
}

# Compares the printed job of row $1 with the golden file (stores it first under --update-golden).
check_row() {
	local name="$1"
	JOB="$(ls -d "$OUT"/jobs/job-* | head -1)"
	if $UPDATE; then
		mkdir -p "$(dirname "$GOLDEN")"
		cp "$JOB/job.escp" "$GOLDEN"
		echo "  golden file updated: $GOLDEN"
	fi
	$WITNESS_JAVA compare "$GOLDEN" "$JOB/job.escp" > "$OUT/compare.txt" 2>&1
	if [ $? -eq 0 ]; then
		echo "$name	SAME	$JOB/index.html" >> "$SUMMARY"
	else
		echo "$name	DIFFERENT	$(grep -m1 'FAIL' "$OUT/compare.txt" | sed 's/^ *FAIL  //')" >> "$SUMMARY"
		FAILED=$((FAILED + 1))
	fi
	sed 's/^/  /' "$OUT/compare.txt"
}

# Checks the text extents of the printed job of row $1 against the extents file.
check_extents() {
	local name="$1"
	if [ ! -f "$EXTENTS" ]; then
		echo "$name-extents	NOT CHECKED	no $EXTENTS" >> "$SUMMARY"
		return 0
	fi
	$WITNESS_JAVA extents --expect "$EXTENTS" "$JOB/job.escp" > "$OUT/extents.txt" 2>&1
	if [ $? -eq 0 ]; then
		echo "$name-extents	EXTENTS OK	$OUT/extents.txt" >> "$SUMMARY"
	else
		echo "$name-extents	EXTENTS DIFFERENT	$(grep -m1 'FAIL' "$OUT/extents.txt" | sed 's/^ *FAIL  //')" >> "$SUMMARY"
		FAILED=$((FAILED + 1))
	fi
}

# Prints row $1 with the client command after the number of attempts $2:
# run_row NAME ATTEMPTS COMMAND [ARG...]. Returns 0 when the job printed.
run_row() {
	local name="$1" attempts="$2"
	shift 2
	OUT="$REPORT/$name"
	local attempt
	for attempt in $(seq 1 "$attempts"); do
		if print_with "$name" "$@"; then
			check_row "$name"
			return 0
		fi
	done
	echo "$name	NOT PRINTED	$(cat "$OUT/failure.txt" 2> /dev/null)" >> "$SUMMARY"
	FAILED=$((FAILED + 1))
	return 1
}

rm -rf "$REPORT"
mkdir -p "$REPORT"
SUMMARY="$REPORT/summary.txt"
: > "$SUMMARY"
FAILED=0
JOB=""
while IFS='|' read -r m home version; do
	[ -n "$m" ] || continue
	$UPDATE && [ "$m" != "6" ] && continue
	name="legacy-java-$(echo "$version" | tr '._+' '---')"
	echo "Java $version ($home)"
	if [ "$m" = "6" ]; then
		# Apple's Java 6 under Rosetta crashes at random: no JIT, no memory serialize page.
		run_row "$name" 3 "$home/bin/java" -Xint -XX:+UseMembar -Djava.awt.headless=true \
			-jar "$LEGACY_JAR" -h "$SERVER" -u "witness-$name" -v
	else
		run_row "$name" 1 "$home/bin/java" -jar "$LEGACY_JAR" -h "$SERVER" -u "witness-$name" -v
	fi
done <<EOF
$ALL_JDKS
EOF

if ! $UPDATE; then
	COMPILE_VERSION="$(java_version "$COMPILE_HOME")"
	name="source-java-$(echo "$COMPILE_VERSION" | tr '._+' '---')"
	echo "Java $COMPILE_VERSION ($COMPILE_HOME), client built from source"
	run_row "$name" 1 "$COMPILE_HOME/bin/java" -jar "$LAUNCHER" -h "$SERVER" -u "witness-$name" -v \
		&& check_extents "$name"
	if [ -n "$PACKAGED_JAVA" ]; then
		echo "Packaged client ($PACKAGED_JAVA, $PACKAGED_APP)"
		run_row packaged 1 "$PACKAGED_JAVA" --add-opens=java.base/java.lang=ALL-UNNAMED \
			--add-opens=java.desktop/javax.print=ALL-UNNAMED --add-opens=java.desktop/sun.print=ALL-UNNAMED \
			-cp "$PACKAGED_APP/*" net.sf.wubiq.clients.LocalPrintManager -h "$SERVER" -u witness-packaged -v \
			&& check_extents packaged
	fi
fi

echo
echo "Against $GOLDEN:"
column -t -s "$(printf '\t')" "$SUMMARY"
[ $FAILED -eq 0 ] && exit 0
exit 1
