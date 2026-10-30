## wubiq-dotmatrix-witness

A virtual Epson ESC/P (9-pin) and ESC/P 2 (24-pin) dot-matrix printer, so dot-matrix output can be checked without a printer. It has no dependencies, compiles for Java 6, and runs on any later JDK.

It does not depend on any other Wubiq module, so it can test any program that produces ESC/P bytes, in any language.

### What it does

It follows the print head through the byte stream and records every character cell it prints, at its real position and pitch, and every bit-image dot (`ESC K`, `L`, `Y`, `Z`, `ESC *` with 8, 24 or 48 dots, and 9-dot `ESC ^`). So it shows text jobs and jobs rendered as graphics by a printer driver. It reports anything a real printer would do differently from what the stream seems to intend:
- text past the right margin, which the printer wraps, and graphics past it, which the printer drops;
- a page length shorter than the paper, which makes form feeds advance only a few lines;
- UTF-8 bytes sent to a single-byte character table;
- a missing `ESC @` at the start or a missing form feed at the end;
- unknown commands.

Each job produces a report directory:

| File | Content |
|---|---|
| `job.escp` | raw bytes received |
| `dump.txt` | decoded command listing with byte offsets, plus warnings |
| `grid.txt` | text picture of each sheet on a 10 cpi column grid, with a count of graphics dots; good for golden files |
| `lines.json` | printed text runs (sheet, x, y, baseline, cpi, styles, text) and graphics totals per sheet, for checks written in any language |
| `sheet-N.png` | rendered sheet: dot-matrix text in blue, graphics dots in black, an optional reference rendering in red underneath, a 1/10 in column grid |
| `check.txt` | layout check result, when expectations were given (Java API) |
| `index.html` | all of the above on one page |

Positions are in inches. `x` is measured from the left edge of the paper. `y` is the print head position (top of the character cell), measured from the top of the sheet. The baseline is `y` plus about 7/72 in on 9-pin printers and 18/180 in on 24-pin.

It does not model:
- raster graphics (`ESC .`, used by ESC/P 2 inkjets);
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

Compile with JDK 8. The jar runs on Java 6 and later.

### Command line

```
java -jar dist/wubiq-dotmatrix-witness.jar render [options] FILE|-
java -jar dist/wubiq-dotmatrix-witness.jar listen [options]
java -jar dist/wubiq-dotmatrix-witness.jar compare [options] BASE OTHER

  --pins 9|24       printer type (default 9)
  --paper WxH       sheet size in inches (default 8.5x11)
  --carriage IN     printable line width (default 8.0; 13.6 for 136 columns)
  --dpi N           png resolution (default 100)
  --out DIR         report directory (render: witness-report, listen: witness-jobs)
  --port N          listen port (default 9100)
  --bind ADDRESS    listen address (default 127.0.0.1; 0.0.0.0 to accept LAN clients)
  --strict          render: exit 1 when the printer reports warnings
  --tolerance IN    compare: largest accepted shift or size change (default 1/60 in across, 1/72 in down)
```

`listen` behaves like a raw (JetDirect, port 9100) network printer. Each connection is one job, and `index.html` in the output directory lists every job received.

`compare` checks that OTHER prints like BASE, a known good job, and exits with 1 when it doesn't. For each sheet it reports:
- the position and size of the printed area, measured on the graphics dots, or on the text when the sheet has no graphics, with the shift in inches;
- the share of BASE's dots that OTHER prints in the same place once that shift is undone (at least 90% passes);
- every text line that differs in content or position.

```
$ java -jar dist/wubiq-dotmatrix-witness.jar compare java6/job.escp java8/job.escp
DIFFERENT: java8/job.escp against java6/job.escp
  PASS  sheets: 1 sheet(s)
  FAIL  sheet 1 position: shifted -0.117 in horizontally, +0.000 in vertically; printed area at (0.417, 0.542) in, 6.000 x 7.431 in, expected (0.533, 0.542) in
  PASS  sheet 1 size: 6.000 x 7.431 in
  PASS  sheet 1 dots: 99.1% of 32884 dots in place once the shift is undone (32955 dots printed)
```

### Using it as a printer

- **Any program that writes to a socket:** `nc 127.0.0.1 9100 < job.escp`.
- **Phones and other machines on the LAN:** start with `--bind 0.0.0.0` and add a raw/9100 network printer at this machine's IP address.
- **macOS or Linux print queue:** see the next section.

### Testing wubiq-client end to end

These steps print through a real wubiq-server and wubiq-client into the witness, on one machine. Commands run from the repository root.

#### 1. Create the CUPS queue

wubiq-client only sees printers installed in the operating system, so the witness needs a CUPS queue. CUPS itself listens on port 631 (`http://localhost:631` shows its web interface); the queue forwards each job to the witness on port 9100.

The witness comes with its own drivers, `cups/witness-9pin.ppd` and `cups/witness-24pin.ppd`. They are the CUPS Epson 9-pin and 24-pin drivers with the manufacturer and model names changed to "Wubiq Witness", and they print through the stock `rastertoepson` filter. With one of them the queue behaves like an installed Epson dot-matrix printer:
- jobs sent with `-o raw` reach the witness unchanged;
- any other job (PDF, PostScript, an application's print) is rendered by the driver into ESC/P bit images, which the witness draws dot by dot.

1. Build the witness and start the listener. Keep it running while you test.

   ```
   (cd wubiq-dotmatrix-witness && ant create-witness-jar)
   java -jar wubiq-dotmatrix-witness/dist/wubiq-dotmatrix-witness.jar listen --out wubiq-dotmatrix-witness/witness-jobs
   ```

   Add `--pins 24` for a 24-pin printer. Don't run `ant create-witness-jar` or `ant clean` here while `wubiq-client/test-dotmatrix-jdks.sh` is running.

2. Create the queue. Your user must be an administrator (in the `lpadmin` group on macOS); otherwise use `sudo`.

   ```
   lpadmin -p Witness_9-pin -E -v socket://127.0.0.1:9100 -P wubiq-dotmatrix-witness/cups/witness-9pin.ppd -D "Wubiq witness 9-pin" -L "virtual ESC/P printer"
   lpadmin -p Witness_9-pin -o printer-is-shared=false
   ```

   - `-P`: the witness driver. Use `witness-24pin.ppd` (and `--pins 24` on the listener) for a 24-pin printer. Apple's Java 6 lists the queue only when it has a driver.
   - `-D`: on macOS, Java names the print service after this description, not after the queue name (`-p`). Wubiq picks the printer type from that name, so the description must contain `9-pin` for a dot-matrix printer, or `24-pin` for a 24-pin (high-quality) one. Avoid `jet`, `laser` and `photo`, which Wubiq checks first.
   - `printer-is-shared=false`: CUPS shares new queues on the network by default, and this one should stay local.

3. Check it. The job should appear in `wubiq-dotmatrix-witness/witness-jobs/` within a few seconds.

   ```
   lpstat -v Witness_9-pin                   # device for Witness_9-pin: socket://127.0.0.1:9100
   lp -d Witness_9-pin -o raw some-job.escp  # any ESC/P file
   ```

Remove the queue when you are done: `lpadmin -x Witness_9-pin`.

#### 2. Start wubiq-server

The server runs in Tomcat under Docker (see `wubiq-server/README.md`). It needs Ant, Docker and a JDK 8.

```
(cd wubiq-server && ant wubiq-server)
docker compose up -d
```

`ant wubiq-server` also builds wubiq-common and wubiq-client. Both of those builds copy their jars into `wubiq-android-gradle/app/libs/`; don't commit those copies by accident.

The console is at `http://localhost:8080/wubiq-server` (user `test`, password `12345`).

#### 3. Configure wubiq-client

1. Make the witness the default printer while you test, or pause your real printers, so nothing prints on paper by mistake.

   ```
   lpstat -d                      # note the current default
   lpoptions -d Witness_9-pin     # restore later with lpoptions -d <previous default>
   ```

2. Start the client with the JDK you want to test.

   ```
   java -jar wubiq-client/dist/wubiq-client.jar -h http://localhost:8080 -u client-test -v
   ```

   Start it with `-jar`: on Java 9 and later, the jar's manifest opens the JDK packages the client uses by reflection (`Add-Opens`), and `-cp` skips the manifest.

   On Apple Silicon, start Apple's Java 6 with `-Xint -XX:+UseMembar -Djava.awt.headless=true`:

   ```
   /Library/Java/JavaVirtualMachines/1.6.0.jdk/Contents/Home/bin/java -Xint -XX:+UseMembar -Djava.awt.headless=true -jar wubiq-client/dist/wubiq-client.jar -h http://localhost:8080 -u client-test -v
   ```

   When it registers its printers, the log shows `Wubiq witness 9-pin -> DOT_MATRIX`. If the printer shows as `UNDEFINED`, its description doesn't contain a dot-matrix marker. Fix the description, or add the name with `-Dwubiq.printers.dotmatrix=NAME`, a comma-separated list matched as substrings.

3. In the server console, `client-test` now lists `Wubiq witness 9-pin`.

#### 4. Print and inspect the output

The test page can be printed in two ways, and they exercise different client code.

**Regular printing**

Press the printer's test page button in the console. The server sends the test page PDF, and the client hands it to the CUPS queue. The witness driver renders it into ESC/P bit images, and the sheet image shows the page the way an Epson dot-matrix printer would print it. This checks the queue, the client and the driver, not Wubiq's text-mode encoder.

You can produce the same output without the client, which is handy to compare:

```
cupsfilter -p wubiq-dotmatrix-witness/cups/witness-9pin.ppd -m application/vnd.cups-raster page.pdf > page.ras
PPD=wubiq-dotmatrix-witness/cups/witness-9pin.ppd /usr/libexec/cups/filter/rastertoepson 1 user title 1 "" page.ras > page.escp
java -jar wubiq-dotmatrix-witness/dist/wubiq-dotmatrix-witness.jar render --out page-report page.escp
```

**ESC/P stream path**

Copy the test page button's link and add two parameters:
- `TP=true` makes the server send the page as a Pageable;
- `TPT` is the `Destination` the client writes ESC/P bytes to. Use an absolute file path on the client machine, URL-encoded, not a `file:` URI.

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
- `sheet-N.png`: each sheet as the printer would print it, text in blue and graphics dots in black;
- `grid.txt`: the text on a 10 cpi column grid, with the vertical position and pitch of each line (driver-rendered jobs have no text, only a graphics count);
- `dump.txt`: every command with its byte offset, and the warnings at the end (text past the margin, a short page length, UTF-8 bytes, a missing reset or form feed, unknown commands);
- `lines.json`: the printed text runs, for checks in other tools;
- `job.escp`: the raw bytes.

Compare the sheets with the test page PDF to see where the text should be. To check another JDK against a known good job, use `compare` on the two `job.escp` files. `wubiq-client/test-dotmatrix-e2e.sh` runs these steps on every installed JDK and compares each job with the Java 6 one.

### Testing a Windows client in a UTM virtual machine

On a Mac, a Windows virtual machine runs the Windows client and the Windows printer driver. The witness and wubiq-server stay on the Mac. The Windows printer sends its ESC/P bytes over the virtual network to the witness, so each job is what a real dot-matrix printer would receive from Windows.

The Windows driver renders pages differently from the CUPS driver. Compare Windows jobs with a Windows Java 6 job, not with the macOS golden file.

#### 1. Create the virtual machine

1. Install UTM from `https://mac.getutm.app` (free; the Mac App Store version is the same app, paid).
2. Download the Windows 11 Arm64 ISO from `https://www.microsoft.com/software-download/windows11arm64`.
3. In UTM, choose **Create a New Virtual Machine** → **Virtualize** → **Windows**, check **Install drivers and SPICE tools**, and select the ISO. Give it at least 4 GB of memory and 64 GB of disk.
4. Install Windows following UTM's guide, `https://docs.getutm.app/guides/windows/`. It covers setup without a network connection and the guest tools. Windows works unactivated for testing.
5. Keep the network mode at **Shared Network**. The Mac is the virtual machine's default gateway; `ipconfig` in the virtual machine shows its address, usually `192.168.64.1`. The steps below use that address.

#### 2. Download the JDKs in the virtual machine

Windows 11 on Arm runs x64 and x86 programs under emulation, so the regular Windows installers work. Install every version you want to test; each one goes to its own folder.

| Java | Download | File |
|---|---|---|
| 6 | `https://www.oracle.com/java/technologies/javase-java-archive-javase6-downloads.html` (Oracle account) | `jdk-6u45-windows-x64.exe` |
| 7 | `https://www.oracle.com/java/technologies/javase/javase7-archive-downloads.html` (Oracle account) | `jdk-7u80-windows-x64.exe` |
| 8 (Oracle builds) | `https://www.oracle.com/java/technologies/javase/javase8-archive-downloads.html` (Oracle account) | `jdk-8u112-windows-x64.exe`, or the build your users run |
| 8, 11, 17, 21, 25 | `https://aws.amazon.com/corretto/` | Windows x64 `.msi` |
| 11, 17, 21, 25 (native Arm64) | `https://learn.microsoft.com/java/openjdk/download` | Windows AArch64 `.msi` |

Run each JDK by its full path, for example `"C:\Program Files\Java\jdk1.8.0_112\bin\java.exe"`. That way the version under test doesn't depend on `PATH` or `JAVA_HOME`. `java -version` confirms the build.

#### 3. Add the witness printer in Windows

The printer is added on a port created beforehand, so Windows doesn't search for a device or need the witness running.

1. In PowerShell, as administrator, create the port:

   ```
   Add-PrinterPort -Name "witness-9100" -PrinterHostAddress "192.168.64.1" -PortNumber 9100
   ```

   This is a Standard TCP/IP port in raw mode: Windows sends the driver's bytes to the witness unchanged.

2. Open the Add Printer wizard:

   ```
   rundll32 printui.dll,PrintUIEntry /il
   ```

   If it starts searching for printers, choose **The printer that I want isn't listed**.

3. Choose **Add a local printer or network printer with manual settings**.
4. Choose **Use an existing port** and pick `witness-9100 (Standard TCP/IP Port)`.
5. In the driver list, press **Windows Update** and wait for the list to reload. Pick the ESC/P driver to test, for example **Epson** → an LX-350 or FX-890 9-pin model. `Generic / Text Only` sends only text, so graphics such as the server test page don't print through it.
6. Name the printer `Wubiq witness 9-pin`. Java on Windows names the print service after the printer name, and Wubiq picks the printer type from it, so keep `9-pin` (or `24-pin`) in it.
7. Don't share the printer and skip the test page.

Make it the default printer while you test, so nothing goes to another printer by mistake. In **Settings** → **Bluetooth & devices** → **Printers & scanners**, turn off **Let Windows manage my default printer** first.

#### 4. Start the witness and wubiq-server on the Mac

1. Start the virtual machine first: the `192.168.64.1` address exists only while it runs. Then start the listener on that address. Keep the jobs of each JDK in their own folder.

   ```
   java -jar wubiq-dotmatrix-witness/dist/wubiq-dotmatrix-witness.jar listen --bind 192.168.64.1 --out wubiq-dotmatrix-witness/witness-jobs-windows/java-6
   ```

   If the macOS firewall asks, allow `java` to accept incoming connections.

2. Build and start wubiq-server as in [2. Start wubiq-server](#2-start-wubiq-server). `ant wubiq-server` packages the current wubiq-client into the server, which serves it at `http://192.168.64.1:8080/wubiq-server/wubiq-client.jar`. After a rebuild, restart the container with `docker compose restart` so the server serves the new jar.

#### 5. Print with each JDK

1. In the virtual machine, download `http://192.168.64.1:8080/wubiq-server/wubiq-client.jar` into a folder, for example `C:\wubiq`.
2. Start the client with the JDK under test, with a uuid that names it:

   ```
   cd C:\wubiq
   "C:\Program Files\Java\jdk1.6.0_45\bin\java.exe" -jar wubiq-client.jar -h http://192.168.64.1:8080 -u windows-java-6 -v
   ```

   Start it with `-jar` on every version: on Java 9 and later, the jar's manifest opens the JDK packages the client uses by reflection.

3. On the Mac, open the server console, find `windows-java-6` and press the test page button of `Wubiq witness 9-pin`. The job appears in `wubiq-dotmatrix-witness/witness-jobs-windows/java-6/`.
4. Stop the client (Ctrl+C) and the listener.

For each other JDK, start the listener with a new `--out` folder (`java-8`, `java-17`...) and repeat steps 2 to 4 with that JDK's `java.exe` path and uuid.

#### 6. Compare the jobs

Compare each JDK's job with the Java 6 one:

```
java -jar wubiq-dotmatrix-witness/dist/wubiq-dotmatrix-witness.jar compare wubiq-dotmatrix-witness/witness-jobs-windows/java-6/job-*/job.escp wubiq-dotmatrix-witness/witness-jobs-windows/java-8/job-*/job.escp
```

`SAME` means that JDK prints like Java 6 through that driver. `DIFFERENT` lists the shift, size, dots and text that changed. Each job's `index.html` shows its sheets.

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
