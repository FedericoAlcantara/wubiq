## wubiq-dotmatrix-witness

A virtual Epson ESC/P (9-pin) and ESC/P 2 (24-pin) dot-matrix printer, so dot-matrix output can be checked without a printer. It has no dependencies, compiles for Java 6, and runs on any later JDK.

It does not depend on any other Wubiq module. Any client that produces ESC/P bytes can be tested with it: the 2.x Java client, Android, or a Wubiq 3 agent written in another language.

### What it does

It follows the print head through the byte stream and records every character cell it prints, at its real position and pitch. It reports anything a real printer would do differently from what the stream seems to intend:
- text past the right margin, which the printer wraps;
- a page length shorter than the paper, which makes form feeds advance only a few lines;
- UTF-8 bytes sent to a single-byte character table;
- a missing `ESC @` at the start or a missing form feed at the end;
- unknown commands.

Each job produces a report directory:

| File | Content |
|---|---|
| `job.escp` | raw bytes received |
| `dump.txt` | decoded command listing with byte offsets, plus warnings |
| `grid.txt` | text picture of each sheet on a 10 cpi column grid; good for golden files |
| `lines.json` | printed text runs (sheet, x, y, baseline, cpi, styles, text) for checks written in any language |
| `sheet-N.png` | rendered sheet: dot-matrix text in blue, an optional reference rendering in red underneath, a 1/10 in column grid |
| `check.txt` | layout check result, when expectations were given (Java API) |
| `index.html` | all of the above on one page |

Positions are in inches. `x` is measured from the left edge of the paper. `y` is the print head position (top of the character cell), measured from the top of the sheet. The baseline is `y` plus about 7/72 in on 9-pin printers and 18/180 in on 24-pin.

It does not model:
- bit-image graphics (skipped, but they still move the head);
- proportional spacing;
- double height;
- international character sets other than USA and Latin America (`ESC R`);
- registered character tables other than Italic, PC437, PC850, PC860, PC863 and PC865 (`ESC ( t`).

All of these produce a warning instead.

### Build

```
ant create-witness-jar     # dist/wubiq-dotmatrix-witness.jar
ant test                   # witness unit tests
```

Compile with JDK 8, since javac 12 and later can't target Java 6. The jar runs on Java 6 and later.

### Command line

```
java -jar dist/wubiq-dotmatrix-witness.jar render [options] FILE|-
java -jar dist/wubiq-dotmatrix-witness.jar listen [options]

  --pins 9|24       printer type (default 9)
  --paper WxH       sheet size in inches (default 8.5x11)
  --carriage IN     printable line width (default 8.0; 13.6 for 136 columns)
  --dpi N           png resolution (default 100)
  --out DIR         report directory (render: witness-report, listen: witness-jobs)
  --port N          listen port (default 9100)
  --bind ADDRESS    listen address (default 127.0.0.1; 0.0.0.0 to accept LAN clients)
  --strict          render: exit 1 when the printer reports warnings
```

`listen` behaves like a raw (JetDirect, port 9100) network printer. Each connection is one job, and `index.html` in the output directory lists every job received.

### Using it as a printer

- **Any program that writes to a socket:** `nc 127.0.0.1 9100 < job.escp`.
- **Phones and other machines on the LAN:** start with `--bind 0.0.0.0` and add a raw/9100 network printer at this machine's IP address.
- **macOS or Linux print queue:** see the next section.

### Testing wubiq-client end to end

These steps print through a real wubiq-server and wubiq-client into the witness, on one machine. Commands run from the repository root.

#### 1. Create the CUPS queue

wubiq-client only sees printers installed in the operating system, so the witness needs a CUPS queue. A raw queue (one with no driver) passes the job bytes to the listener unchanged. CUPS itself listens on port 631 (`http://localhost:631` shows its web interface); the queue forwards each job to the witness on port 9100.

1. Build the witness and start the listener. Keep it running while you test.

   ```
   (cd wubiq-dotmatrix-witness && ant create-witness-jar)
   java -jar wubiq-dotmatrix-witness/dist/wubiq-dotmatrix-witness.jar listen --out wubiq-dotmatrix-witness/witness-jobs
   ```

   Add `--pins 24` for a 24-pin printer. Don't run `ant create-witness-jar` or `ant clean` here while `wubiq-client/test-dotmatrix-jdks.sh` is running, because they delete the classes it uses.

2. Create the queue. Your user must be an administrator (in the `lpadmin` group on macOS); otherwise use `sudo`.

   ```
   lpadmin -p Witness_9-pin -E -v socket://127.0.0.1:9100 -D "Wubiq witness 9-pin" -L "virtual ESC/P printer"
   lpadmin -p Witness_9-pin -o printer-is-shared=false
   ```

   - `-D`: on macOS, Java names the print service after this description, not after the queue name (`-p`). Wubiq picks the printer type from that name, so the description must contain `9-pin` for a dot-matrix printer, or `24-pin` for a 24-pin (high-quality) one. Avoid `jet`, `laser` and `photo`, which Wubiq checks first.
   - `printer-is-shared=false`: CUPS shares new queues on the network by default, and this one should stay local.

   Apple's Java 6 doesn't list raw queues. To test with Java 6, add a second queue that uses the Generic PostScript driver. CUPS then converts regular jobs to PostScript, but `-o raw` jobs still pass through unchanged:

   ```
   lpadmin -p Witness_PS_9-pin -E -v socket://127.0.0.1:9100 -m drv:///sample.drv/generic.ppd -D "Wubiq witness PS 9-pin"
   lpadmin -p Witness_PS_9-pin -o printer-is-shared=false
   ```

3. Check it. The job should appear in `wubiq-dotmatrix-witness/witness-jobs/` within a few seconds.

   ```
   lpstat -v Witness_9-pin                   # device for Witness_9-pin: socket://127.0.0.1:9100
   lp -d Witness_9-pin -o raw some-job.escp  # any ESC/P file
   ```

Remove the queues when you are done: `lpadmin -x Witness_9-pin` and `lpadmin -x Witness_PS_9-pin`.

#### 2. Start wubiq-server

The server runs in Tomcat under Docker (see `wubiq-server/README.md`). It needs Ant, Docker and a JDK 8.

```
(cd wubiq-server && ant wubiq-server)
docker compose up -d
```

`ant wubiq-server` also builds wubiq-common and wubiq-client. Both of those builds copy their jars into `wubiq-android-gradle/app/libs/`; don't commit those copies by accident.

The console is at `http://localhost:8080/wubiq-server` (user `test`, password `12345`).

#### 3. Configure wubiq-client

1. Make sure nothing real prints by mistake. On macOS, a Java print job was once seen going to the default printer instead of the printer it asked for. While you test, make the witness the default printer, or pause your real printers.

   ```
   lpstat -d                      # note the current default
   lpoptions -d Witness_9-pin     # restore later with lpoptions -d <previous default>
   ```

2. Start the client with the JDK you want to test. Issue #23 is about Java 11, 17 and 21; Java 6 and 8 are the baseline.

   ```
   java -cp wubiq-client/dist/wubiq-client.jar net.sf.wubiq.clients.LocalPrintManager -h http://localhost:8080 -u client-test -v
   ```

   On Apple Silicon, Apple's Java 6 runs under Rosetta and the JVM crashes within seconds. Start it with `-Xint -XX:+UseMembar -Djava.awt.headless=true` (no JIT, no memory serialize page, no AWT window). With these options it stays up:

   ```
   /Library/Java/JavaVirtualMachines/1.6.0.jdk/Contents/Home/bin/java -Xint -XX:+UseMembar -Djava.awt.headless=true -cp wubiq-client/dist/wubiq-client.jar net.sf.wubiq.clients.LocalPrintManager -h http://localhost:8080 -u client-test -v
   ```

   When it registers its printers, the log shows `Wubiq witness 9-pin -> DOT_MATRIX` (Java 6 shows `Wubiq_witness_PS_9_pin -> DOT_MATRIX`). If the printer shows as `UNDEFINED`, its description doesn't contain a dot-matrix marker. Fix the description, or add the name with `-Dwubiq.printers.dotmatrix=NAME`, a comma-separated list matched as substrings.

3. In the server console, `client-test` now lists `Wubiq witness 9-pin`.

#### 4. Print and inspect the output

The test page can be printed in two ways, and they exercise different client code.

**Regular printing**

Press the printer's test page button in the console. The server sends the test page PDF, and the client hands it to the CUPS queue. The witness receives what the operating system produced (probably the PDF itself, starting with `%PDF`), not ESC/P, and reports it as unknown commands. This checks the queue and the client, not the dot-matrix encoder.

**ESC/P stream path**

This is the code issue #23 is about. Copy the test page button's link and add two parameters:
- `TP=true` makes the server send the page as a Pageable;
- `TPT` is the `Destination` the client writes ESC/P bytes to. Use an absolute file path on the client machine, URL-encoded. A `file:` URI doesn't work, because the client opens the string with `FileOutputStream`.

```
http://localhost:8080/wubiq-server/wubiq-print-test.do?command=t1&printServiceName=...&uuid=client-test&TP=true&TPT=%2FUsers%2Fme%2Fwitness.escp
```

The ESC/P bytes go to that file, not through the queue, so render them yourself:

```
java -jar wubiq-dotmatrix-witness/dist/wubiq-dotmatrix-witness.jar render --out wubiq-dotmatrix-witness/witness-jobs/test-page /Users/me/witness.escp
```

The client may also send the page to the queue through the JDK print pipeline. Those bytes show up as a separate job.

**Reading the output**

Open `wubiq-dotmatrix-witness/witness-jobs/index.html`. It lists every job the listener received, with links to each one. Each job, and each directory written by `render`, holds:
- `index.html`: everything below on one page;
- `sheet-N.png`: each sheet as the printer would print it;
- `grid.txt`: the text on a 10 cpi column grid, with the vertical position and pitch of each line;
- `dump.txt`: every command with its byte offset, and the warnings at the end (text past the margin, a short page length, UTF-8 bytes, a missing reset or form feed, unknown commands);
- `lines.json`: the printed text runs, for checks in other tools;
- `job.escp`: the raw bytes.

Compare the sheets with the test page PDF to see where the text should be. Running the same steps with each JDK, and comparing `job.escp` (for example with `cmp`), shows whether the output changes between Java versions.

### Java API

```java
PrinterProfile profile = PrinterProfile.ninePin();
EscpResult result = new EscpInterpreter(profile).interpret(bytes);
CheckResult check = new LayoutCheck(profile).check(result, expectations, pages);
new WitnessReport(profile).write(directory, "my job", bytes, result, referenceImages, check);
```

`ExpectedText` describes a string, its page, and where the application drew it: the x of its left edge and its baseline. `LayoutCheck` requires each string to be printed once:
- the baseline within half a 6 lpi line (1/12 in);
- the first character within one character cell of the intended x;
- matching bold, italic and underline.

A pitch mismatch is a note, unless strict pitch is enabled.

`wubiq-client/tests-src/net/sf/wubiq/dotmatrix` uses this API to check the 2.x client's ESC/P path on every installed JDK (`wubiq-client/test-dotmatrix-jdks.sh`).
