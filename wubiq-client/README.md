## wubiq-client

This module contains all the relevant code for connecting clients.

### Running

The client runs on Java 25. On a computer that prints, install the native package built by `../wubiq-installer`: it carries its own Java runtime and runs the client as a service, so no Java has to be installed.

From a build, start it with `-jar`:

```
java -jar wubiq-client.jar -h http://SERVER:8080 -u UUID
```

Its manifest opens the JDK packages the client uses by reflection (`Add-Opens: java.base/java.lang java.desktop/javax.print java.desktop/sun.print`, kept in the `client.add.opens` property of `build.xml`). Java applies it only to the jar given to `-jar`; with `-cp`, pass the same packages as `--add-opens PACKAGE=ALL-UNNAMED`.

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

This compiles wubiq-common, the client, the witness and the tests into `bin` and `build-tests/classes`, without building jars. Run it with a JDK 25. The report goes to `build-tests/dotmatrix-report/java-VERSION/index.html`, with one page per case: the sheet images, text grid, decoded commands and check results.

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

It runs the tests on every installed JDK 25 or later and compares the bytes each one produces. Older JDKs are skipped: the client is compiled for Java 25.

1. **Finds the JDKs**, one per major version:
   - the ones `/usr/libexec/java_home -V` lists;
   - `/Library/Java/JavaVirtualMachines` and `~/Library/Java/JavaVirtualMachines`;
   - Homebrew's `openjdk*`;
   - `/usr/lib/jvm`.

   To use your own list, set `WUBIQ_JDKS` to space-separated JDK homes.
2. **Compiles once with JDK 25** (`ant compile-tests`). A JDK 25 with `javac` is required.
3. **Runs the reference self-check and every case on each JDK**, headless. Each JDK writes to `build-tests/dotmatrix-report/java-MAJOR/`, and its console output goes to `java-MAJOR.log`.
4. **Converts each `job.ps` to `job.pdf`** when `gs` (Ghostscript) is installed.
5. **Prints a matrix** of each fixture-driver case against each JDK, and writes it to `build-tests/dotmatrix-report/index.html`, linking every cell to its report.

Each cell reads `STATUS ok/failed same|DIFF`:
- `STATUS`: `PASS` or `FAIL`, the layout check on that JDK;
- `ok/failed`: the number of passed and failed check items;
- `same`/`DIFF`: whether that JDK produced the same bytes as Java 25.

Exit codes:
- `0`: done. Without `--strict` this is the result even when cases fail.
- `1`: with `--strict`, a Wubiq case failed, or its bytes differ from the baseline.
- `2`: no JDK 25 was found, or the compilation failed.

#### End to end, through wubiq-server

```
./test-dotmatrix-e2e.sh [--update-golden] [--packaged-java JAVA --packaged-app DIR]
```

It prints wubiq-server's test page through a real client into the dot-matrix witness, and compares each job with `tests-src/net/sf/wubiq/dotmatrix/golden/test-page-9pin.escp`, the job printed by Java 6. It prints with three clients:

| Row | Client | Java |
|---|---|---|
| `legacy-java-VERSION` | `../wubiq-server/WebContent/wubiq-client.jar`, the 2.7.4 jar wubiq-server serves to the installed clients | every installed JDK build (1.8.0_112 and 1.8.0_362 both run) |
| `source-java-VERSION` | `build-tests/wubiq-client-launcher.jar` (`ant create-launcher-jar`): the compiled classes with the manifest of `wubiq-client.jar` | the JDK 25 that compiled them |
| `packaged` | the jars of an installed package, `DIR` of `--packaged-app` | the package's own runtime, `JAVA` of `--packaged-java` |

The `source` and `packaged` jobs also get a row named `ROW-extents`: the witness measures the width of the job's text lines against `tests-src/net/sf/wubiq/dotmatrix/golden/test-page.extents`, the text extents of the test page PDF (`extents` in `../wubiq-dotmatrix-witness/README.md`).

For an installed package, `JAVA` and `DIR` are:

| OS | `--packaged-java` | `--packaged-app` |
|---|---|---|
| macOS | `/Applications/Wubiq.app/Contents/runtime/Contents/Home/bin/java` | `/Applications/Wubiq.app/Contents/app` |
| Linux | `/opt/wubiq/lib/runtime/bin/java` | `/opt/wubiq/lib/app` |

It needs:
- wubiq-server running at `http://localhost:8080` (`WUBIQ_SERVER` to change it);
- the `Witness_9-pin` CUPS queue with the witness driver (see `../wubiq-dotmatrix-witness/README.md`);
- port 9100 free: the script starts and stops its own witness listener;
- a JDK 25 with `javac`.

For each row it:
1. starts the witness listener and the client, with the uuid `witness-ROW`;
2. sends the test page from the server console link of that client's witness printer;
3. waits for the job, stops the client and the listener;
4. compares the job with the golden file (`compare` in the witness) and, for the `source` and `packaged` rows, checks its text extents.

While it runs, `Witness_9-pin` is the default printer; the previous default is restored on exit. Each row writes to `build-tests/dotmatrix-e2e/ROW/`: the client and listener logs, the job (`jobs/job-*/index.html`), `compare.txt` and `extents.txt`. `build-tests/dotmatrix-e2e/summary.txt` lists every row as `SAME`, `DIFFERENT` (with the first difference) or `NOT PRINTED` (with the client's error), and every extents row as `EXTENTS OK`, `EXTENTS DIFFERENT` or `NOT CHECKED` (no extents file).

`--update-golden` prints with the 2.7.4 jar on Java 6 only and stores its job as the golden file.

Exit codes:
- `0`: every row prints like Java 6 and no extents row is `EXTENTS DIFFERENT`.
- `1`: a row prints differently, doesn't print, or its text extents differ.
- `2`: setup error (no server, no queue, port 9100 in use, no JDK 25 to compile, no 2.7.4 jar, `--packaged-java` without `--packaged-app`).

#### End to end on Windows

```
.\test-dotmatrix-e2e-windows.ps1 -Jdks JDK6_HOME,JDK8_HOME,... [-CompileHome JDK25_HOME] [-PackagedJava JAVA -PackagedApp DIR] [-Server URL] [-Printer NAME] [-Tolerance IN]
```

The same test through a Windows printer driver, in PowerShell. The rows are the ones of the macOS script: every JDK of `-Jdks` prints with the 2.7.4 jar (`legacy-java-VERSION`), the JDK 25 of `-CompileHome` prints with the client built from source (`source-java-VERSION`), and `-PackagedJava` with `-PackagedApp` print with an installed package (`packaged`): `C:\Program Files\Wubiq\runtime\bin\java.exe` and `C:\Program Files\Wubiq\app`. Java 6 prints first, and every other job is compared with its job. Each client then prints the page through the fast path: the test page link gets `TP=true` and `TPT`, a file the client writes the ESC/P bytes to (a Windows path with forward slashes, as it has to be a URI). The client runs with `-Dwubiq.dotmatrix.charset=IBM850 -Dwubiq.dotmatrix.table=3`. The witness must read each fast path file without warnings, and it must match Java 6's file. The `source` and `packaged` driver jobs also get a `ROW-extents` row, as in the macOS script.

It needs:
- wubiq-server running at `http://localhost:8080` (`-Server` to change it);
- a printer named `Wubiq witness 9-pin` (`-Printer` to change it) with an ESC/P driver, on a Standard TCP/IP port to `127.0.0.1:9100` (see `../wubiq-dotmatrix-witness/README.md`);
- port 9100 free;
- `ant` on the `PATH`.

`-Jdks` takes the JDK homes, or `WUBIQ_JDKS` with the homes separated by `;`, and must include a Java 6. `-CompileHome` is a JDK 25 with `javac`; it defaults to the first Java 25 among `-Jdks`. `-Tolerance` is the largest accepted shift or size change in inches, 0.025 (3 dots at 120 dpi) by default. Each row writes to `build-tests/dotmatrix-e2e-windows/ROW/`, and `build-tests/dotmatrix-e2e-windows/summary.txt` lists every row as `REFERENCE` (Java 6), `SAME`, `DIFFERENT` or `NOT PRINTED`, in one line for the driver job, one, named `ROW-fast`, for the fast path file (which can also be `WARNINGS`), and one, named `ROW-extents`, for the text extents (`EXTENTS OK`, `EXTENTS DIFFERENT` or `NOT CHECKED`). The exit codes are the same as the macOS script's.

The GitHub Actions workflow `.github/workflows/dotmatrix-windows.yml` runs it on a Windows runner, on demand:
- Oracle Java 6u45, from the private S3 bucket `java-jdk-wadvanced` through the AWS role in the `AWS_ROLE_ARN` secret. It is the reference; runs without access to the role use Zulu 6;
- Zulu builds of Java 6, 7, 8u112, 8, 11, 17, 21 and 25;
- the MSI built by `../wubiq-installer`, installed on the runner, as the packaged client;
- wubiq-server on Tomcat 7, with the files `docker-compose.yml` mounts;
- the Epson LX-350 ESC/P driver from Epson's site, on a printer port to the witness.

The jobs and the Tomcat logs are uploaded as the `dotmatrix-e2e-windows` artifact, and the run's summary shows the result of every row.
