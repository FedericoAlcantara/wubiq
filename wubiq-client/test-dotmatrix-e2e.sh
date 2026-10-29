#!/bin/bash
# Prints wubiq-server's test page through a real wubiq-client on every installed JDK build into
# the dot-matrix witness, and compares each job with the golden job printed by Java 6.
# Every distinct version runs (1.8.0_112 and 1.8.0_362 are both tested).
#
#   ./test-dotmatrix-e2e.sh [--update-golden]
#
# Needs:
# - wubiq-server running (WUBIQ_SERVER, default http://localhost:8080);
# - the Witness_9-pin CUPS queue with the witness driver (../wubiq-dotmatrix-witness/README.md);
# - port 9100 free: the script starts and stops its own listener.
#
# While it runs, the witness queue is the default printer; the previous default is restored on exit.
# --update-golden prints with Java 6 only and stores its job as the golden file.
# Exit codes: 0 every JDK prints like Java 6, 1 a JDK prints differently or not at all, 2 setup error.
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
UPDATE=false
[ "${1:-}" = "--update-golden" ] && UPDATE=true

. "$BASE/jdk-homes.sh"
if [ -z "$COMPILE_HOME" ]; then
	echo "A JDK 8 (with javac) is needed to compile for Java 6." >&2
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

# Prints the test page with one JDK. Leaves the job in $OUT/jobs and returns 1 when none arrived.
print_with() {
	local m="$1" home="$2" name="$3"
	local uuid="witness-$name"
	local extra=""
	[ "$m" = "6" ] && extra="-Xint -XX:+UseMembar -Djava.awt.headless=true"
	rm -rf "$OUT"
	mkdir -p "$OUT/jobs"
	$WITNESS_JAVA listen --out "$OUT/jobs" > "$OUT/witness.log" 2>&1 &
	LISTENER=$!
	wait_for 15 listening || { echo "  the witness listener didn't start"; return 1; }
	( cd "$OUT" && exec "$home/bin/java" $extra -jar "$LAUNCHER" -h "$SERVER" -u "$uuid" -v ) \
		> "$OUT/client.log" 2>&1 &
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

rm -rf "$REPORT"
mkdir -p "$REPORT"
SUMMARY="$REPORT/summary.txt"
: > "$SUMMARY"
FAILED=0
while IFS='|' read -r m home version; do
	[ -n "$m" ] || continue
	$UPDATE && [ "$m" != "6" ] && continue
	name="java-$(echo "$version" | tr '._+' '---')"
	OUT="$REPORT/$name"
	echo "Java $version ($home)"
	attempts=1
	[ "$m" = "6" ] && attempts=3
	printed=false
	for attempt in $(seq 1 $attempts); do
		if print_with "$m" "$home" "$name"; then
			printed=true
			break
		fi
	done
	if ! $printed; then
		echo "$name	NOT PRINTED	$(cat "$OUT/failure.txt" 2> /dev/null)" >> "$SUMMARY"
		FAILED=$((FAILED + 1))
		continue
	fi
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
done <<EOF
$ALL_JDKS
EOF

echo
echo "Against $GOLDEN:"
column -t -s "$(printf '\t')" "$SUMMARY"
[ $FAILED -eq 0 ] && exit 0
exit 1
