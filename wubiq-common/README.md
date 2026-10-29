## wubiq-common

This is the wubiq-common module. It contains all the common code to other parts of the project.

### Dot-matrix tests

`tests-src/net/sf/wubiq/dotmatrix` prints a set of fixture jobs through the ESC/P stream path (`PageableStreamWrapper` → `PrintableStreamWrapper` → `ESCPrinter`). It feeds the bytes to the virtual printer in `../wubiq-dotmatrix-witness` and checks that every string lands where the fixture drew it.

```
ant test-dotmatrix          # current JDK; report in build-tests/dotmatrix-report/java-VERSION/index.html
./test-dotmatrix-jdks.sh    # every installed JDK (6, 8, 11, 17, 21, ...); matrix in build-tests/dotmatrix-report/index.html
```

Each fixture runs through four drivers:
- `direct`: the wrappers on an image graphics.
- `direct-300dpi`: the same, with a printer-like device scale.
- `jdk-print`: the JDK print pipeline through the PostScript stream service. Its `job.ps`/`job.pdf` show what a laser printer would print.
- `reference`: a correct encoder that is not Wubiq code. It must always pass; this keeps the fixtures and tolerances honest.

The tests only report by default. `-Dwubiq.dotmatrix.strict=true` (or `--strict` for the script) makes layout and golden-file mismatches fail. `-Dwubiq.dotmatrix.updateGolden=true` writes the golden files.
