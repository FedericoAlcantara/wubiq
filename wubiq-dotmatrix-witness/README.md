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
- **macOS or Linux print queue:** create a raw CUPS queue pointing at the listener. The OS print path and Java `PrintService` lookups then see it as a regular printer. Including `9-pin` in the name makes Wubiq treat it as a dot-matrix printer.

  ```
  lpadmin -p Witness_9-pin -E -v socket://127.0.0.1:9100 -D "Wubiq dot-matrix witness"
  lp -d Witness_9-pin -o raw job.escp
  ```

  Remove it with `lpadmin -x Witness_9-pin`.

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

`wubiq-common/tests-src/net/sf/wubiq/dotmatrix` uses this API to check the 2.x client's ESC/P path on every installed JDK (`wubiq-common/test-dotmatrix-jdks.sh`).
