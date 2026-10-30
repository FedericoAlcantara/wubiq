## wubiq-client

This module contains all the relevant code for connecting clients.

### Running

```
java -jar wubiq-client.jar -h http://SERVER:8080 -u UUID
```

The client runs on Java 6 and later. Start it with `-jar`: its manifest opens the JDK packages it uses by reflection (`Add-Opens: java.base/java.lang java.desktop/javax.print java.desktop/sun.print`, kept in the `client.add.opens` property of `build.xml`). Java 9 and later apply it only to the jar given to `-jar`; with `-cp`, pass the same packages as `--add-opens PACKAGE=ALL-UNNAMED`.

### Dot-matrix tests

When a print request carries a `Destination`, the client prints in ESC/P "fast" mode instead of through the JDK print pipeline. `ClientPrintDirectUtils` wraps the job in `PageableStreamWrapper` → `PrintableStreamWrapper` → `ESCPrinter` (in wubiq-common), and these write the ESC/P bytes to the destination.

`tests-src/net/sf/wubiq/dotmatrix` checks that path without a printer:
1. It prints a set of fixture jobs through the wrappers.
2. It feeds the bytes to the virtual printer in `../wubiq-dotmatrix-witness`.
3. It checks that every string lands where the fixture drew it.

The fixtures are:
- a ruler;
- an invoice with right-aligned amounts;
- every font size;
- bold, italic and underline, with Spanish accented characters;
- every Java2D text call;
- translated and scaled graphics;
- a three-page job.

Each fixture runs through four drivers:
- `direct`: the wrappers on an image graphics.
- `direct-300dpi`: the same, with a printer-like device scale.
- `jdk-print`: the JDK print pipeline through the PostScript stream service. Its `job.ps` (and `job.pdf`, when Ghostscript is installed) shows what a laser printer would print.
- `reference`: a correct encoder that is not Wubiq code. It always passes.

#### Current JDK

```
ant test-dotmatrix
```

This compiles wubiq-common, the client, the witness and the tests into `bin` and `build-tests/classes`, without building jars. Run it with a JDK 8. The report goes to `build-tests/dotmatrix-report/java-VERSION/index.html`, with one page per case: the sheet images, text grid, decoded commands and check results.

System properties, passed with `-D` (for example `ant test-dotmatrix -Dwubiq.dotmatrix.strict=true`):

| Property | Effect |
|---|---|
| `wubiq.dotmatrix.strict=true` | layout and golden-file mismatches fail the build (default: report only) |
| `wubiq.dotmatrix.updateGolden=true` | rewrites the golden files in `tests-src/net/sf/wubiq/dotmatrix/golden` from the `direct` driver |
| `wubiq.dotmatrix.only=invoice-direct,ruler-jdk-print` | runs only these cases (fixture-driver) |
| `wubiq.dotmatrix.images=false` | skips the PNG sheets |
| `wubiq.dotmatrix.report=DIR` | report directory |
| `wubiq.dotmatrix.golden=DIR` | golden files directory |

#### Every installed JDK

```
./test-dotmatrix-jdks.sh [--strict]
```

It runs the tests on every installed JDK and compares the bytes each one produces.

1. **Finds the JDKs**, one per major version:
   - the ones `/usr/libexec/java_home -V` lists;
   - `/Library/Java/JavaVirtualMachines` and `~/Library/Java/JavaVirtualMachines`;
   - Homebrew's `openjdk*`;
   - `/usr/lib/jvm`.

   To use your own list, set `WUBIQ_JDKS` to space-separated JDK homes. The script notes any of Java 6, 8, 11, 17 and 21 it doesn't find (for example `brew install --cask corretto@21`).
2. **Compiles once with JDK 8** (`ant compile-tests`), at source and target 1.6. A JDK 8 with `javac` is required.
3. **Runs the reference self-check and every case on each JDK**, headless. Each JDK writes to `build-tests/dotmatrix-report/java-MAJOR/`, and its console output goes to `java-MAJOR.log`.
4. **Converts each `job.ps` to `job.pdf`** when `gs` (Ghostscript) is installed.
5. **Prints a matrix** of each fixture-driver case against each JDK, and writes it to `build-tests/dotmatrix-report/index.html`, linking every cell to its report.

Each cell reads `STATUS ok/failed same|DIFF`:
- `STATUS`: `PASS` or `FAIL`, the layout check on that JDK;
- `ok/failed`: the number of passed and failed check items;
- `same`/`DIFF`: whether that JDK produced the same bytes as Java 8. When Java 8 isn't available, the first JDK run is the baseline.

Exit codes:
- `0`: done. Without `--strict` this is the result even when cases fail.
- `1`: with `--strict`, a Wubiq case failed, or its bytes differ from the baseline.
- `2`: no JDK 8 was found, or the compilation failed.

Java 6 runs with `-Xint -XX:+UseMembar` and one JVM per case, retrying each case up to five times; a case with no result after five attempts shows as `missing`. The script finds Java 6 in `/Library/Java/JavaVirtualMachines/1.6.0.jdk`.

#### End to end, through wubiq-server

```
./test-dotmatrix-e2e.sh [--update-golden]
```

It prints wubiq-server's test page on every installed JDK build, through a real wubiq-client, into the dot-matrix witness, and compares each job with `tests-src/net/sf/wubiq/dotmatrix/golden/test-page-9pin.escp`, the job printed by Java 6.

It needs:
- wubiq-server running at `http://localhost:8080` (`WUBIQ_SERVER` to change it);
- the `Witness_9-pin` CUPS queue with the witness driver (see `../wubiq-dotmatrix-witness/README.md`);
- port 9100 free: the script starts and stops its own witness listener.

For each JDK build (every distinct version, so 1.8.0_112 and 1.8.0_362 both run), it:
1. starts the witness listener and a client on that JDK, with the uuid `witness-java-VERSION`. The client runs from `build-tests/wubiq-client-launcher.jar` (`ant create-launcher-jar`): the compiled classes with the manifest of `wubiq-client.jar`;
2. sends the test page from the server console link of that client's witness printer;
3. waits for the job, stops the client and the listener;
4. compares the job with the golden file (`compare` in the witness).

While it runs, `Witness_9-pin` is the default printer; the previous default is restored on exit. Each build writes to `build-tests/dotmatrix-e2e/java-VERSION/`: the client and listener logs, the job (`jobs/job-*/index.html`) and `compare.txt`. `build-tests/dotmatrix-e2e/summary.txt` lists every build as `SAME`, `DIFFERENT` (with the first difference) or `NOT PRINTED` (with the client's error).

`--update-golden` prints with Java 6 only and stores its job as the golden file.

Exit codes:
- `0`: every build prints like Java 6.
- `1`: a build prints differently, or doesn't print.
- `2`: setup error (no server, no queue, port 9100 in use, no JDK 8 to compile).

#### End to end on Windows

```
.\test-dotmatrix-e2e-windows.ps1 -Jdks JDK6_HOME,JDK8_HOME,... [-CompileHome JDK8_HOME] [-Server URL] [-Printer NAME]
```

The same test through a Windows printer driver, in PowerShell. Java 6 prints first, and every other JDK's job is compared with its job.

It needs:
- wubiq-server running at `http://localhost:8080` (`-Server` to change it);
- a printer named `Wubiq witness 9-pin` (`-Printer` to change it) with an ESC/P driver, on a Standard TCP/IP port to `127.0.0.1:9100` (see `../wubiq-dotmatrix-witness/README.md`);
- port 9100 free;
- `ant` on the `PATH`.

`-Jdks` takes the JDK homes, or `WUBIQ_JDKS` with the homes separated by `;`, and must include a Java 6. `-CompileHome` defaults to the first Java 8 among them. Each build writes to `build-tests/dotmatrix-e2e-windows/java-VERSION/`, and `build-tests/dotmatrix-e2e-windows/summary.txt` lists every build as `REFERENCE` (Java 6), `SAME`, `DIFFERENT` or `NOT PRINTED`. The exit codes are the same as the macOS script's.

The GitHub Actions workflow `.github/workflows/dotmatrix-windows.yml` runs it on a Windows runner, on pull requests that change the client, the server or the witness, and on demand:
- Zulu builds of Java 6, 7, 8u112, 8, 11, 17, 21 and 25;
- wubiq-server on Tomcat 7, with the files `docker-compose.yml` mounts;
- the Epson LX-350 ESC/P driver from Epson's site, on a printer port to the witness.

The jobs and the Tomcat logs are uploaded as the `dotmatrix-e2e-windows` artifact, and the run's summary shows the result of every JDK.
