<!-- enriched-spec:start v2 -->
## Enriched Spec

**Complexity:** high

### Overview
The 2.x print client ships as a native package per OS (MSI, PKG, DEB, RPM) that carries its own Java 25 runtime, runs as a boot-time service and updates itself from packages the server streams; wubiq-server keeps serving the frozen 2.7.4 jar to the installed base. The Android app stops shipping the client and common jars and keeps its own copy of the classes it uses. Sections change `wubiq-common`, `wubiq-dotmatrix-witness`, `wubiq-client`, `wubiq-server`, `wubiq-installer` and `wubiq-android-gradle`; `wubiq-printerjob` is not changed.

### Section Map
| ID | Type | Scope | Depends on | Branch | PR title |
|---|---|---|---|---|---|
| COM-DOC-1 | Documentation | `CHANGELOG.md` § Wubiq 2.8.0 › Wubiq-common; `wubiq-common/README.md` § Language level, § Mobile device table | none | docs/52-com-doc-1-common-docs | docs(common): Java 8 level, device table and packaged client protocol (#52 · COM-DOC-1) |
| COM-BLD-1 | Build | `wubiq-common/build.xml`: Java 8 level, no copy into the Android project | COM-DOC-1 | build/52-com-bld-1-java8-no-android-copy | build(common): compile for Java 8 and stop copying the jar into the Android app (#52 · COM-BLD-1) |
| COM-BE-1 | Backend | Windows dot-matrix font narrowing removed | COM-DOC-1 | fix/52-com-be-1-remove-narrowing | fix(common): print Windows dot-matrix text at its real width (#52 · COM-BE-1) |
| COM-BE-2 | Backend | Protocol names of the packaged client, `ClientPackages`, the server packages directory key | COM-DOC-1 | feat/52-com-be-2-client-packages | feat(common): protocol names and package file rules for the packaged client (#52 · COM-BE-2) |
| COM-UI-1 | UI | Server label for the client versions summary | COM-DOC-1 | feat/52-com-ui-1-versions-label | feat(common): label for the connected client versions summary (#52 · COM-UI-1) |
| WIT-DOC-1 | Documentation | `CHANGELOG.md` › Wubiq-dotmatrix-witness; `wubiq-dotmatrix-witness/README.md` § Build, § Command line, § Testing wubiq-client end to end, § Testing a Windows client | COM-DOC-1 | docs/52-wit-doc-1-witness-docs | docs(witness): Java 8 level and the extents command (#52 · WIT-DOC-1) |
| WIT-BLD-1 | Build | `wubiq-dotmatrix-witness/build.xml`: Java 8 level | WIT-DOC-1 | build/52-wit-bld-1-java8 | build(witness): compile for Java 8 (#52 · WIT-BLD-1) |
| WIT-BE-1 | Backend | `extents` command: printed text widths against expected text extents | WIT-DOC-1 | feat/52-wit-be-1-extents | feat(witness): check printed text widths against expected extents (#52 · WIT-BE-1) |
| CLI-DOC-1 | Documentation | `CHANGELOG.md` › Wubiq-client; `wubiq-client/README.md` § Running, § Dot-matrix tests | WIT-DOC-1 | docs/52-cli-doc-1-client-docs | docs(client): Java 25 client and the packaged client tests (#52 · CLI-DOC-1) |
| CLI-BLD-1 | Build | `wubiq-client/build.xml`: Java 8 level, no copy into the Android project | CLI-DOC-1 | build/52-cli-bld-1-java8-no-android-copy | build(client): compile for Java 8 and stop copying the jar into the Android app (#52 · CLI-BLD-1) |
| CLI-BLD-2 | Build | `wubiq-client/build.xml`: client sources and tests at Java 25 | CLI-DOC-1, CLI-BLD-1, WIT-BLD-1 | build/52-cli-bld-2-java25 | build(client): compile the client for Java 25 (#52 · CLI-BLD-2) |
| CLI-BLD-3 | Build | Dot-matrix scripts: frozen jar rows, source row on JDK 25, packaged client row, extents row | CLI-DOC-1, CLI-BLD-2, WIT-BE-1 | build/52-cli-bld-3-dotmatrix-scripts | build(client): dot-matrix scripts for the frozen jar and the packaged client (#52 · CLI-BLD-3) |
| CLI-BE-1 | Backend | `TestPageExtents` test tool and the test page extents file | CLI-DOC-1 | test/52-cli-be-1-test-page-extents | test(client): text extents of the server test page (#52 · CLI-BE-1) |
| SRV-DOC-1 | Documentation | `CHANGELOG.md` › Wubiq-server; `wubiq-server/README.md` § Requirements, § Running the server, § Client packages | CLI-DOC-1 | docs/52-srv-doc-1-server-docs | docs(server): client packages directory and the two client lanes (#52 · SRV-DOC-1) |
| SRV-BLD-1 | Build | `wubiq-server/build.xml`: no client or installer build, the served client jar is not overwritten | SRV-DOC-1 | build/52-srv-bld-1-frozen-client-jar | build(server): stop building the client and keep the served jar (#52 · SRV-BLD-1) |
| SRV-BE-1 | Backend | `READ_VERSION` lanes and package streaming in `RemotePrintServlet` | SRV-DOC-1, COM-BE-2 | feat/52-srv-be-1-client-lanes | feat(server): frozen legacy lane and packaged client lane (#52 · SRV-BE-1) |
| SRV-UI-1 | UI | Console: connected clients by version | SRV-DOC-1, COM-UI-1 | feat/52-srv-ui-1-versions-summary | feat(server): show the connected clients by version (#52 · SRV-UI-1) |
| INS-DOC-1 | Documentation | `CHANGELOG.md` › Wubiq-installer; `wubiq-installer/README.md` (whole file) | SRV-DOC-1 | docs/52-ins-doc-1-installer-docs | docs(installer): native packages with a bundled Java (#52 · INS-DOC-1) |
| INS-BLD-1 | Build | IzPack build and assets removed; `build.xml` compiles at Java 25 | INS-DOC-1, CLI-BLD-2 | build/52-ins-bld-1-remove-izpack | build(installer): remove IzPack and compile for Java 25 (#52 · INS-BLD-1) |
| INS-BLD-2 | Build | `package` target: jlink runtime, jpackage MSI, PKG, DEB and RPM, service resources | INS-DOC-1, INS-BLD-1, COM-BE-2 | build/52-ins-bld-2-jpackage | build(installer): native packages with jlink and jpackage (#52 · INS-BLD-2) |
| INS-BE-1 | Backend | Launcher runs the bundled client; configuration directory; no jar download | INS-DOC-1, INS-BLD-2, COM-BE-2 | feat/52-ins-be-1-bundled-client | feat(installer): run the bundled client from the packaged launcher (#52 · INS-BE-1) |
| INS-BE-2 | Backend | Update by package from the running service | INS-DOC-1, INS-BE-1, SRV-BE-1 | feat/52-ins-be-2-package-update | feat(installer): update the packaged client from the server (#52 · INS-BE-2) |
| INS-BE-3 | Backend | Upgrade from an IzPack installation | INS-DOC-1, INS-BE-2 | feat/52-ins-be-3-izpack-upgrade | feat(installer): take over an IzPack installation (#52 · INS-BE-3) |
| INS-UI-1 | UI | Configurator: save error and update notice | INS-DOC-1, INS-BE-1 | feat/52-ins-ui-1-configurator-notices | feat(installer): configurator save error and update notice (#52 · INS-UI-1) |
| AND-DOC-1 | Documentation | `CHANGELOG.md` › Wubiq-android-gradle; `wubiq-android-gradle/README.md` § Shared classes | INS-DOC-1 | docs/52-and-doc-1-android-docs | docs(android): the app's own copy of the shared classes (#52 · AND-DOC-1) |
| AND-BLD-1 | Build | Shared classes as app sources; `wubiq-client.jar` and `wubiq-common.jar` removed | AND-DOC-1 | build/52-and-bld-1-own-shared-classes | build(android): own the shared classes and drop the client and common jars (#52 · AND-BLD-1) |

A section starts only when every dependency is **merged**. Independent
sections may run in parallel. Status is derived from GitHub, never recorded
here.

Three sections also wait for a hand-made PR listed under "Out of Scope": CLI-BLD-2 and INS-BLD-1 for PR H1, SRV-BE-1 for PR H0. Each of them states the check as its first step.

<!-- section:COM-DOC-1:start -->
### COM-DOC-1 — Documentation · wubiq-common
Depends on: none

#### Documentation references
`CHANGELOG.md` § `## Wubiq 2.7.4` (the insertion point); `wubiq-common/README.md` § Tests.

#### Implementation details

##### CHANGELOG.md
1. Insert a new version block directly above the line `## Wubiq 2.7.4`, followed by one blank line. Verbatim:
   ```
   ## Wubiq 2.8.0
   ### Wubiq-common
   - Dot-matrix text on Windows prints at its real width: the horizontal narrowing (0.55) and the tracking applied on Java 7 and later are removed.
   - Compiled for Java 8. The build no longer copies `wubiq-common.jar` into `wubiq-android-gradle`.
   - Protocol names for the packaged client: command `v2` (download the client package), parameters `cpf` (platform) and `cpt` (package type).
   ```

##### wubiq-common/README.md
1. Between the paragraph `This is the wubiq-common module. It contains all the common code to other parts of the project.` and `### Tests`, insert verbatim:
   ```
   ### Language level

   wubiq-common is compiled for Java 8 (`release="8"` in `build.xml`). wubiq-server loads it on Java 8 and the packaged client loads it on its bundled Java 25. Code in this module uses Java 8 syntax and classes only.

   ### Mobile device table

   `net.sf.wubiq.android.MobileDevices` is the device table wubiq-server reads. The Android app has its own copy in `wubiq-android-gradle/app/src/main/java/net/sf/wubiq/android/MobileDevices.java`. Add a device to both files in the same change, with the same name: the app sends only the device name.

   The constants in `CommandKeys`, `ParameterKeys` and `DirectConnectKeys` are the wire protocol. The app has its own copy of them too; change a constant in both places.

   ```

##### Acceptance criteria
- [x] AC-1: the CHANGELOG entry sits under `## Wubiq 2.8.0` › `### Wubiq-common` and
      carries no issue reference (mechanical — no red test; verified by
      `git diff origin/master...HEAD -- CHANGELOG.md | grep -E '^\+.*\(#[0-9]+\)'` returning nothing)
- [x] AC-2: nothing but this application's documentation modified, apart from this issue's
      spec file (its AC ticks) (mechanical — no red test; verified by
      `git diff --name-only origin/master...HEAD | grep -vE '^CHANGELOG\.md$|^wubiq-common/.*\.md$|^specs/issue-52-enriched-spec\.md$'`
      returning nothing)
- [x] AC-3: `CHANGELOG.md` has `## Wubiq 2.8.0` directly above `### Wubiq-common`, and that block sits above `## Wubiq 2.7.4` (mechanical — no red test; verified by `grep -n -A1 '^## Wubiq 2.8.0$' CHANGELOG.md` printing `### Wubiq-common` as the second line, and `grep -n '^## Wubiq 2\.[78]\.[04]$' CHANGELOG.md` listing 2.8.0 first)
- [x] AC-4: `wubiq-common/README.md` has the two new sections (mechanical — no red test; verified by `grep -c -E '^### (Language level|Mobile device table)$' wubiq-common/README.md` printing `2`)

##### Green checks
1. `.claude/scripts/gate.sh` clean (code-issue); `.claude/scripts/suite.sh` — full suite green
   (review-issue runs the suite)
<!-- section:COM-DOC-1:end -->
<!-- section:COM-BLD-1:start -->
### COM-BLD-1 — Build · wubiq-common · Java 8 level, no copy into the Android project
Depends on: COM-DOC-1

#### Documentation references
`wubiq-common/README.md` § Language level.

#### Implementation details
Language level: `source="1.6" target="1.6"` in the three `javac` tasks of `wubiq-common/build.xml` (targets `create-common-jar`, `compile`, `compile-tests`). This section raises it to Java 8.

##### Acceptance criteria
- [x] AC-1: the three `javac` tasks compile for Java 8 (mechanical — no red test; verified by `grep -c 'source="1.8" target="1.8" release="8"' wubiq-common/build.xml` printing `3` and `grep -c '1\.6' wubiq-common/build.xml` printing `0`)
- [x] AC-2: no target of `wubiq-common/build.xml` writes into `wubiq-android-gradle` (mechanical — no red test; verified by `grep -c -E 'basedir\.gradle|app/libs' wubiq-common/build.xml` printing `0`)
- [x] AC-3: Given JDK 8, whose `javac` has no `--release` option, when the gate compiles `COM`, then it compiles: Ant ignores `release` on JDK 8 and uses `source` and `target` (mechanical — no red test; verified by `.claude/scripts/gate.sh` ending `EXIT: 0`)

##### Code changes
1. `wubiq-common/build.xml` — delete the line
   `<property name="basedir.gradle" value="${workspace.dir}/wubiq-android-gradle" />`.
2. `wubiq-common/build.xml` target `create-common-jar` — delete the line
   `<copy todir="${basedir.gradle}/app/libs/" file="${outputjar}"/>`.
3. `wubiq-common/build.xml` targets `create-common-jar`, `compile`, `compile-tests` — in each `javac` task replace `target="1.6" source="1.6"` with `source="1.8" target="1.8" release="8"`. Change no other attribute.

##### Green tests
1. `.claude/scripts/suite.sh` — full suite green (review-issue runs this)
2. `.claude/scripts/gate.sh` clean
<!-- section:COM-BLD-1:end -->
<!-- section:COM-BE-1:start -->
### COM-BE-1 — Backend · wubiq-common · Windows dot-matrix font narrowing removed
Depends on: COM-DOC-1

#### Documentation references
`CHANGELOG.md` § `## Wubiq 2.8.0` › `### Wubiq-common`, first bullet.

#### Implementation details
Language level: Java 1.6 syntax and classes. `wubiq-client/build.xml` target `compile` compiles `wubiq-common/src` with `source="1.6"` until CLI-BLD-1 merges, so this section prescribes nothing above 1.6, in sources and in tests.

##### Acceptance criteria
- [x] AC-1: Given the cached OS name `Windows 10`, when `GraphicsUtils#properFont(Font, PrinterType)` receives `new Font("Arial", Font.PLAIN, 12)` and `PrinterType.DOT_MATRIX`, then the returned font has an identity transform
- [x] AC-2: Given the same call, then the returned font has no `TextAttribute.TRACKING` attribute, its size is 12 and its family is `SansSerif`
- [x] AC-3: Given the cached OS name `Windows 10`, when `properFont` receives `new Font("Courier New", Font.PLAIN, 12)` and `PrinterType.DOT_MATRIX_HQ`, then the returned font is named `Monospaced` and has an identity transform
- [x] AC-4: Given a font name the method does not map (`"Wingdings"`), when `properFont` receives it with `PrinterType.DOT_MATRIX`, then the returned font keeps the name `Wingdings`, size 12 and an identity transform (degraded path: an unmapped font is printed as sent, never narrowed)

##### Test ports
- `net.sf.wubiq.utils.GraphicsUtils#properFont(java.awt.Font, net.sf.wubiq.enums.PrinterType)` ·
  in: a font and a printer type · out: the font to draw with · existing (`wubiq-common/src/net/sf/wubiq/utils/GraphicsUtils.java`, `GraphicsUtils#properFont(Font, PrinterType)`)

##### Red tests (write first; each must fail before implementation)
Setup shared by every row. `GraphicsUtils` caches the OS name in its private field `osName` (`GraphicsUtils.osName`) on the first call. The test sets that field by reflection and never changes the `os.name` system property (changing it breaks the JDK font manager on macOS and Linux):
```java
Field field = GraphicsUtils.class.getDeclaredField("osName");
field.setAccessible(true);
field.set(GraphicsUtils.INSTANCE, "Windows 10");
```
in a `@Before` method; an `@After` method sets the field back to `null`.

| AC | Placement | Setup | Test file | Test name | Assertion sketch |
|---|---|---|---|---|---|
| AC-1 | new file (`ls wubiq-common/tests-src/net/sf/wubiq` lists no file) | `Font font = GraphicsUtils.INSTANCE.properFont(new Font("Arial", Font.PLAIN, 12), PrinterType.DOT_MATRIX)` | wubiq-common/tests-src/net/sf/wubiq/utils/GraphicsUtilsTest.java | `windowsDotMatrixFontIsNotScaled` | `assertTrue(font.getTransform().isIdentity())` |
| AC-2 | add to that file | same font | same file | `windowsDotMatrixFontHasNoTracking` | `assertNull(font.getAttributes().get(TextAttribute.TRACKING)); assertEquals(12, font.getSize()); assertEquals("SansSerif", font.getFamily())` |
| AC-3 | add to that file | `properFont(new Font("Courier New", Font.PLAIN, 12), PrinterType.DOT_MATRIX_HQ)` | same file | `courierNewBecomesMonospacedUnscaled` | `assertEquals("Monospaced", font.getName()); assertTrue(font.getTransform().isIdentity())` |
| AC-4 | add to that file | `properFont(new Font("Wingdings", Font.PLAIN, 12), PrinterType.DOT_MATRIX)` | same file | `unmappedFontIsKeptUnscaled` | `assertEquals("Wingdings", font.getName()); assertEquals(12, font.getSize()); assertTrue(font.getTransform().isIdentity())` |

The class is named `*Test`: target `test` of `wubiq-common/build.xml` runs every class with that suffix.

##### Code changes
1. `wubiq-common/src/net/sf/wubiq/utils/GraphicsUtils.java` `GraphicsUtils#windowsProperFont(Font, PrinterType)` — delete the whole block that starts with `if (System.getProperty("java.version").compareTo("1.7") >= 0) {` and ends with its closing brace (the block that sets `scale = 0.55f`, `TextAttribute.KERNING`, `TextAttribute.TRACKING`, `TextAttribute.TRANSFORM` and `TextAttribute.SIZE`). The method then ends with `return font;` right after the `else if (fontName.toLowerCase().contains("verdana") || fontName.equalsIgnoreCase("dialog"))` branch.
2. Same method — delete the declaration `int kerning = TextAttribute.KERNING_ON;` and the statement `kerning = 0;` in the `courier new` branch.
3. Same file — delete the imports nothing else in the file uses after steps 1 and 2: `java.awt.font.TextAttribute`, `java.math.BigDecimal`, `java.math.RoundingMode`, `java.util.HashMap`, `java.util.Map`. Keep `java.awt.geom.AffineTransform`: `GraphicsUtils#scaleGraphics(Graphics2D, PageFormat, boolean)` uses it.
4. Change nothing in `properFont`, `useDefaultFonts`, `linuxProperFont`, `macOsProperFont`: the font name substitutions stay.

##### Contract changes
| Site | Change | New handling |
|---|---|---|
| `wubiq-common/src/net/sf/wubiq/wrappers/GraphicsStreamWrapper.java`, `GraphicsDirectWrapper.java`, `PrintableWrapper.java` — callers of `properFont` | same signature; on Windows the returned font is no longer scaled to 0.55 | no edit |
| `wubiq-client/src/net/sf/wubiq/clients/remotes/PrintableChunkRemote.java` — caller of `properFont` (three sites) | same | no edit |

##### Green tests
1. The red tests above pass, unmodified (code-issue runs these, targeted:
   `.claude/scripts/section-test.sh COM net.sf.wubiq.utils.GraphicsUtilsTest`)
2. `.claude/scripts/suite.sh` — full suite green (review-issue runs this; code-issue runs only
   this section's test classes)
3. `.claude/scripts/gate.sh` clean
<!-- section:COM-BE-1:end -->
<!-- section:COM-BE-2:start -->
### COM-BE-2 — Backend · wubiq-common · Protocol names of the packaged client, `ClientPackages`, the server packages directory key
Depends on: COM-DOC-1

#### Documentation references
`CHANGELOG.md` § `## Wubiq 2.8.0` › `### Wubiq-common`, third bullet; `wubiq-common/README.md` § Mobile device table (the wire protocol rule).

#### Implementation details
Language level: Java 1.6 syntax and classes, for the reason stated in COM-BE-1: no diamond operator, no lambda, no try-with-resources, no `switch` on a `String`, no `java.nio.file`, no `java.time`.

Fixed values of this section:

| Name | Value |
|---|---|
| Platforms | `windows-x64`, `macos-arm64`, `macos-x64`, `linux-x64` |
| Package types | `msi`, `pkg`, `deb`, `rpm` |
| Package file name | `wubiq-client-<version>-<platform>.<type>`, `<version>` digits separated by dots — `wubiq-client-2.8.0-linux-x64.deb` |
| File name pattern | `^wubiq-client-(\d+(?:\.\d+)*)-(windows-x64\|macos-arm64\|macos-x64\|linux-x64)\.(msi\|pkg\|deb\|rpm)$` |
| Version pattern | `^\d+(\.\d+)*$` |

##### Acceptance criteria
- [ ] AC-1: Given `"2.10.0"` and `"2.9.9"`, when `ClientPackages#compareVersions(String, String)` compares them, then the result is positive; given `"2.8"` and `"2.8.0"`, the result is `0` (numbers, not strings; a missing part counts as 0)
- [ ] AC-2: Given an offered version, when `ClientPackages#isNewer(String, String)` compares it with the own version `"2.8.0"`, then it returns `true` for `"2.8.1"` and `false` for `"2.8.0"`, `"2.7.4"`, `""`, `null` and `"< 2.0"` (an equal, older, empty or malformed offer is rejected)
- [ ] AC-3: Given version `2.8.0`, platform `linux-x64` and type `deb`, when `ClientPackages#fileName(String, String, String)` builds the name, then it returns `wubiq-client-2.8.0-linux-x64.deb`, and `ClientPackages#parse(String)` of that name returns `{"2.8.0", "linux-x64", "deb"}`
- [ ] AC-4: Given a name that does not follow the pattern (`wubiq-client-2.8.0-linux-x64.zip`, `Wubiq-2.8.0.pkg`, `wubiq-client-2.8.0-solaris-x64.deb`), when `parse` reads it, then it returns `null`
- [ ] AC-5: Given a directory holding `wubiq-client-2.8.0-linux-x64.deb`, `wubiq-client-2.10.0-linux-x64.deb`, `wubiq-client-2.9.0-linux-x64.rpm`, `wubiq-client-3.0.0-windows-x64.msi`, `wubiq-client-2.8.0-macos-arm64.pkg`, `wubiq-client-2.9.0-macos-arm64.pkg`, `wubiq-client-2.10.0-macos-x64.pkg` and `notes.txt`, when `ClientPackages#latest(File, String, String)` is asked, then `linux-x64` and `deb` give `wubiq-client-2.10.0-linux-x64.deb`, `macos-arm64` and `pkg` give `wubiq-client-2.9.0-macos-arm64.pkg`, `macos-x64` and `pkg` give `wubiq-client-2.10.0-macos-x64.pkg`, and `windows-x64` and `msi` give `wubiq-client-3.0.0-windows-x64.msi` (every platform has a package in the directory; a higher version of the other macOS platform is never picked)
- [ ] AC-6: Given each of these: a directory that does not exist; a `null` directory; the AC-5 directory asked for `linux-x64` and `pkg`, a platform and type no file there has — when `latest` is asked, then it returns `null` (the server answers that it offers nothing; it never throws)
- [ ] AC-7: Given an OS name and architecture, when `ClientPackages#platform(String, String)` maps them, then `("Windows 11", "amd64")` gives `windows-x64`, `("Mac OS X", "aarch64")` gives `macos-arm64`, `("Mac OS X", "x86_64")` gives `macos-x64`, `("Linux", "amd64")` gives `linux-x64`, and `("Linux", "arm")` and `("SunOS", "amd64")` give `""`
- [ ] AC-8: Given a package type and a path, when `ClientPackages#installCommand(String, String)` builds the unattended install command, then it returns the row of the table in Code changes step 3, and `null` for an unknown type
- [ ] AC-9: the protocol names and keys exist with the values of Code changes step 1 (mechanical — no red test; verified by `grep -rhoE 'READ_PACKAGE = "v2"|CLIENT_PLATFORM = "cpf"|CLIENT_PACKAGE_TYPE = "cpt"|PROPERTY_CLIENT_PACKAGES_DIRECTORY = "client.packages.directory"|WUBIQ_PACKAGE_TYPE = "wubiq.package.type"' wubiq-common/src/net/sf/wubiq/common | wc -l` printing `5`)

##### Test ports
- `net.sf.wubiq.utils.ClientPackages#compareVersions(String, String)` · in: two versions matching the version pattern · out: negative, 0 or positive `int`; throws `IllegalArgumentException` when either does not match · new (`grep -rIl ClientPackages` over the repository returns nothing)
- `net.sf.wubiq.utils.ClientPackages#isNewer(String, String)` · in: offered and own version · out: `boolean` · new
- `net.sf.wubiq.utils.ClientPackages#fileName(String, String, String)` · in: version, platform, type · out: file name · new
- `net.sf.wubiq.utils.ClientPackages#parse(String)` · in: a file name · out: `String[3]` (version, platform, type) or `null` · new
- `net.sf.wubiq.utils.ClientPackages#latest(java.io.File, String, String)` · in: directory, platform, type · out: the file with the highest version, or `null` · new
- `net.sf.wubiq.utils.ClientPackages#platform(String, String)` · in: `os.name`, `os.arch` values · out: a platform or `""` · new
- `net.sf.wubiq.utils.ClientPackages#installCommand(String, String)` · in: type, absolute path of the package · out: `String[]` command or `null` · new

##### Red tests (write first; each must fail before implementation)
All rows: new file `wubiq-common/tests-src/net/sf/wubiq/utils/ClientPackagesTest.java` (the class under test is new; `ls wubiq-common/tests-src/net/sf/wubiq` lists no test for it).

| AC | Placement | Setup | Test file | Test name | Assertion sketch |
|---|---|---|---|---|---|
| AC-1 | new file | literals | ClientPackagesTest.java | `versionsCompareAsNumbers` | `assertTrue(ClientPackages.compareVersions("2.10.0", "2.9.9") > 0); assertEquals(0, ClientPackages.compareVersions("2.8", "2.8.0"))` |
| AC-2 | add to that file | literals | same | `onlyANewerOfferIsAccepted` | `assertTrue(ClientPackages.isNewer("2.8.1", "2.8.0")); assertFalse(ClientPackages.isNewer("2.8.0", "2.8.0")); assertFalse(ClientPackages.isNewer("2.7.4", "2.8.0")); assertFalse(ClientPackages.isNewer("", "2.8.0")); assertFalse(ClientPackages.isNewer(null, "2.8.0")); assertFalse(ClientPackages.isNewer("< 2.0", "2.8.0"))` |
| AC-3 | add to that file | literals | same | `fileNameRoundTrip` | `assertEquals("wubiq-client-2.8.0-linux-x64.deb", ClientPackages.fileName("2.8.0", "linux-x64", "deb")); assertArrayEquals(new String[] {"2.8.0", "linux-x64", "deb"}, ClientPackages.parse("wubiq-client-2.8.0-linux-x64.deb"))` |
| AC-4 | add to that file | literals | same | `misnamedFilesAreIgnored` | `assertNull(ClientPackages.parse("wubiq-client-2.8.0-linux-x64.zip")); assertNull(ClientPackages.parse("Wubiq-2.8.0.pkg")); assertNull(ClientPackages.parse("wubiq-client-2.8.0-solaris-x64.deb"))` |
| AC-5 | add to that file | a temporary directory: `File dir = File.createTempFile("packages", ""); dir.delete(); dir.mkdir();` then `new File(dir, name).createNewFile()` for the eight names of AC-5; files and directory deleted in a `finally` block | same | `latestPicksTheHighestVersionOfThePlatformAndType` | `assertEquals("wubiq-client-2.10.0-linux-x64.deb", ClientPackages.latest(dir, "linux-x64", "deb").getName()); assertEquals("wubiq-client-2.9.0-macos-arm64.pkg", ClientPackages.latest(dir, "macos-arm64", "pkg").getName()); assertEquals("wubiq-client-2.10.0-macos-x64.pkg", ClientPackages.latest(dir, "macos-x64", "pkg").getName()); assertEquals("wubiq-client-3.0.0-windows-x64.msi", ClientPackages.latest(dir, "windows-x64", "msi").getName())` |
| AC-6 | add to that file | the AC-5 directory with its eight files | same | `latestReturnsNullWhenNothingIsOffered` | `assertNull(ClientPackages.latest(new File(dir, "missing"), "linux-x64", "deb")); assertNull(ClientPackages.latest(null, "linux-x64", "deb")); assertNull(ClientPackages.latest(dir, "linux-x64", "pkg"))` |
| AC-7 | add to that file | literals | same | `platformOfOsNameAndArchitecture` | `assertEquals("windows-x64", ClientPackages.platform("Windows 11", "amd64")); assertEquals("macos-arm64", ClientPackages.platform("Mac OS X", "aarch64")); assertEquals("macos-x64", ClientPackages.platform("Mac OS X", "x86_64")); assertEquals("linux-x64", ClientPackages.platform("Linux", "amd64")); assertEquals("", ClientPackages.platform("Linux", "arm")); assertEquals("", ClientPackages.platform("SunOS", "amd64"))` |
| AC-8 | add to that file | literals | same | `installCommands` | `assertArrayEquals(new String[] {"msiexec", "/i", "C:\\t\\p.msi", "/qn", "/norestart"}, ClientPackages.installCommand("msi", "C:\\t\\p.msi")); assertArrayEquals(new String[] {"/usr/sbin/installer", "-pkg", "/t/p.pkg", "-target", "/"}, ClientPackages.installCommand("pkg", "/t/p.pkg")); assertArrayEquals(new String[] {"dpkg", "-i", "/t/p.deb"}, ClientPackages.installCommand("deb", "/t/p.deb")); assertArrayEquals(new String[] {"rpm", "-U", "/t/p.rpm"}, ClientPackages.installCommand("rpm", "/t/p.rpm")); assertNull(ClientPackages.installCommand("zip", "/t/p.zip"))` |

##### Code changes
1. Add these constants, each after the last constant of its group in the named class:

   | File | Constant |
   |---|---|
   | `wubiq-common/src/net/sf/wubiq/common/CommandKeys.java`, after `CommandKeys.RELOAD_SERVER_CONFIGURATION` | `public static final String READ_PACKAGE = "v2";` |
   | `wubiq-common/src/net/sf/wubiq/common/ParameterKeys.java`, after `ParameterKeys.CLIENT_SUPPORTS_COMPRESSION` | `public static final String CLIENT_PLATFORM = "cpf";` and `public static final String CLIENT_PACKAGE_TYPE = "cpt";` |
   | `wubiq-common/src/net/sf/wubiq/common/ConfigurationKeys.java`, after `ConfigurationKeys.PROPERTY_SUPPRESS_LOGS` | `public static final String PROPERTY_CLIENT_PACKAGES_DIRECTORY = "client.packages.directory";` |
   | `wubiq-common/src/net/sf/wubiq/common/PropertyKeys.java`, after `PropertyKeys.WUBIQ_REMOTE_COMMAND_LOG_DIR` | `public static final String WUBIQ_PACKAGE_TYPE = "wubiq.package.type";` |

2. `wubiq-common/src/net/sf/wubiq/utils/ServerProperties.java` — add, after `ServerProperties#getRemotePrintJobManager()`:
   ```java
   /**
    * Directory on the server machine that holds the client packages.
    * @return The configured path, blank when none is configured.
    */
   public String getClientPackagesDirectory() {
   	return get(ConfigurationKeys.PROPERTY_CLIENT_PACKAGES_DIRECTORY, "").trim();
   }
   ```
3. New file `wubiq-common/src/net/sf/wubiq/utils/ClientPackages.java` — `public final class ClientPackages` with a private constructor and only `public static` members:
   - `public static final String FILE_PREFIX = "wubiq-client-";`
   - two `private static final java.util.regex.Pattern` fields compiled from the file name pattern and the version pattern of the table above.
   - `compareVersions(String left, String right)`: throws `IllegalArgumentException` naming the value when either argument is `null` or does not match the version pattern; splits both on `\.`; compares part by part as `Long.parseLong` values, a missing part counting as 0; returns the sign of the first difference, `0` when none.
   - `isNewer(String offered, String own)`: returns `false` when either is `null` or does not match the version pattern; otherwise `compareVersions(offered, own) > 0`.
   - `fileName(String version, String platform, String type)`: returns `FILE_PREFIX + version + "-" + platform + "." + type`.
   - `parse(String fileName)`: returns `null` for `null` or a name the file name pattern does not match (`matches()`, the whole name); otherwise `new String[] {group(1), group(2), group(3)}`.
   - `latest(File directory, String platform, String type)`: returns `null` when `directory` is `null`, is not a directory, or `directory.listFiles()` returns `null`; otherwise, among the regular files whose `parse(getName())` is not `null` and whose platform and type equal the arguments, returns the one with the highest version by `compareVersions`; `null` when there is none. The arguments are compared with parsed values only; they are never made part of a path.
   - `platform(String osName, String osArch)`: returns `""` when either is `null`. OS part: `windows` when `osName` starts with `Windows`, `macos` when it starts with `Mac`, `linux` when it starts with `Linux`. Architecture part: `x64` for `amd64` and `x86_64`, `arm64` for `aarch64` and `arm64`. Returns `os + "-" + arch` only when the result is one of the four platforms of the table; otherwise `""`.
   - `currentPlatform()`: returns `platform(System.getProperty("os.name"), System.getProperty("os.arch"))`.
   - `installCommand(String type, String path)`:

     | `type` | Returned array |
     |---|---|
     | `msi` | `msiexec`, `/i`, `<path>`, `/qn`, `/norestart` |
     | `pkg` | `/usr/sbin/installer`, `-pkg`, `<path>`, `-target`, `/` |
     | `deb` | `dpkg`, `-i`, `<path>` |
     | `rpm` | `rpm`, `-U`, `<path>` |
     | anything else | `null` |

   - `main(String[] args)`: prints `fileName(Labels.VERSION, currentPlatform(), args[0])` with `System.out.println` and returns. With no argument, or when `currentPlatform()` is `""`, it prints `Usage: ClientPackages msi|pkg|deb|rpm (on Windows, macOS or Linux, x64 or arm64)` to `System.err` and calls `System.exit(2)`. INS-BLD-2 names the package files with this method.
   No test asserts what `main` prints: it carries `Labels.VERSION`.

##### Contract changes
| Site | Change | New handling |
|---|---|---|
| `CommandKeys`, `ParameterKeys`, `ConfigurationKeys`, `PropertyKeys` | new constants only | no existing reader; SRV-BE-1, INS-BLD-2, INS-BE-1, INS-BE-2 and INS-UI-1 cite them |
| `wubiq-android-gradle/app/libs/wubiq-client.jar` (the classes the app ships until AND-BLD-1) | none: the app sends none of the new names | no edit; the app's own copies of `CommandKeys` and `ParameterKeys` are made by AND-BLD-1 from the sources then on `master` |
| Released clients (2.7.4 and older) and released servers | never send `cpf`, `cpt` or `v2`; an older server ignores `cpf` and `cpt` on `v1` and answers its own version | SRV-BE-1 and INS-BE-2 state both sides |

##### Green tests
1. The red tests above pass, unmodified (code-issue runs these, targeted:
   `.claude/scripts/section-test.sh COM net.sf.wubiq.utils.ClientPackagesTest`)
2. `.claude/scripts/suite.sh` — full suite green (review-issue runs this; code-issue runs only
   this section's test classes)
3. `.claude/scripts/gate.sh` clean
<!-- section:COM-BE-2:end -->
<!-- section:COM-UI-1:start -->
### COM-UI-1 — UI · wubiq-common · Server label for the client versions summary
Depends on: COM-DOC-1

#### Documentation references
`wubiq-server/README.md` § Client packages (the console line that lists the connected clients by version).

#### Implementation details
Language level: Java 1.6 syntax and classes, for the reason stated in COM-BE-1.

The server's labels live in this application: `wubiq-common/i18n/server_labels_en.properties` and `wubiq-common/i18n/server_labels_es.properties`, read through `ServerLabels#get(String, String...)` (`wubiq-common/src/net/sf/wubiq/utils/ServerLabels.java`). `Labels#get(String, String...)` replaces `%0` with the first parameter and returns the key itself when the bundle has no such key.

##### Acceptance criteria
- [x] AC-1: Given the English locale, when `ServerLabels#get(String, String...)` is asked for `server.clients_by_version` with the parameter `2.7.4 (3)`, then it returns `Connected clients by version: 2.7.4 (3)`
- [x] AC-2: Given the Spanish locale, the same call returns `Clientes conectados por versión: 2.7.4 (3)`
- [x] AC-3: Given a key no bundle has (`server.no_such_label`), when `ServerLabels#get` is asked for it, then it returns the key (degraded path kept: a missing label never throws)

##### Test ports
- `net.sf.wubiq.utils.ServerLabels#get(String, String...)` · in: key and parameters · out: the localized text, or the key · existing (`wubiq-common/src/net/sf/wubiq/utils/ServerLabels.java`, `ServerLabels#get(String, String...)`)
- `net.sf.wubiq.utils.ServerLabels#setLocale(java.util.Locale)` · in: the locale to read the bundle with; it also calls `Locale.setDefault` · existing (same file, `ServerLabels#setLocale(Locale)`)

##### Red tests (write first; each must fail before implementation)
Setup shared by every row: a `@Before` method stores `Locale.getDefault()`; an `@After` method calls `ServerLabels.setLocale(Locale.ENGLISH)` and then `Locale.setDefault(<stored>)`.

| AC | Placement | Setup | Test file | Test name | Assertion sketch |
|---|---|---|---|---|---|
| AC-1 | new file (`ls wubiq-common/tests-src/net/sf/wubiq` lists no file) | `ServerLabels.setLocale(Locale.ENGLISH)` | wubiq-common/tests-src/net/sf/wubiq/utils/ServerLabelsTest.java | `clientsByVersionInEnglish` | `assertEquals("Connected clients by version: 2.7.4 (3)", ServerLabels.get("server.clients_by_version", "2.7.4 (3)"))` |
| AC-2 | add to that file | `ServerLabels.setLocale(new Locale("es"))` | same | `clientsByVersionInSpanish` | `assertEquals("Clientes conectados por versi\u00f3n: 2.7.4 (3)", ServerLabels.get("server.clients_by_version", "2.7.4 (3)"))` |
| AC-3 | add to that file | `ServerLabels.setLocale(Locale.ENGLISH)` | same | `missingLabelReturnsItsKey` | `assertEquals("server.no_such_label", ServerLabels.get("server.no_such_label"))` |

AC-3 passes before implementation: it pins the existing fallback and is not a red test. AC-1 and AC-2 are red.

##### Code changes
1. `wubiq-common/i18n/server_labels_en.properties` — after the line `server.client_disconnected=Client is disconnected`, insert:
   `server.clients_by_version=Connected clients by version: %0`
2. `wubiq-common/i18n/server_labels_es.properties` — after the line `server.client_disconnected=Cliente desconectado`, insert:
   `server.clients_by_version=Clientes conectados por versi\u00F3n: %0`
   (the six characters `\u00F3` are typed as shown; the file is ISO-8859-1 with unicode escapes).

##### Green tests
1. The red tests above pass, unmodified (code-issue runs these, targeted:
   `.claude/scripts/section-test.sh COM net.sf.wubiq.utils.ServerLabelsTest`)
2. `.claude/scripts/suite.sh` — full suite green (review-issue runs this; code-issue runs only
   this section's test classes)
3. `.claude/scripts/gate.sh` clean
<!-- section:COM-UI-1:end -->
<!-- section:WIT-DOC-1:start -->
### WIT-DOC-1 — Documentation · wubiq-dotmatrix-witness
Depends on: COM-DOC-1

#### Documentation references
`wubiq-dotmatrix-witness/README.md` § wubiq-dotmatrix-witness (the opening paragraph), § Build, § Command line, § Testing wubiq-client end to end, § Testing a Windows client, § Java API.

#### Implementation details

##### CHANGELOG.md
1. § `## Wubiq 2.8.0` — after the last bullet of `### Wubiq-common`, insert verbatim:
   ```
   ### Wubiq-dotmatrix-witness
   - Compiled for Java 8.
   - New `extents` command: measures the width of the text lines a job prints and compares them with expected text extents, so a job is checked without a reference job printed by another Java.
   ```

##### wubiq-dotmatrix-witness/README.md
1. § wubiq-dotmatrix-witness (opening paragraph) — replace the sentence `It has no dependencies, compiles for Java 6, and runs on any later JDK.` with `It has no dependencies, compiles for Java 8, and runs on Java 8 and later.`
2. § Build — replace `Compile with JDK 8. The jar runs on Java 6 and later.` with `Compile with JDK 8 or later. The jar runs on Java 8 and later.`
3. § Command line, the fenced usage block — after the line `java -jar dist/wubiq-dotmatrix-witness.jar compare [options] BASE OTHER` insert the line
   `java -jar dist/wubiq-dotmatrix-witness.jar extents --expect FILE [options] JOB`
   and replace the line that starts with `  --tolerance IN` with these two lines:
   ```
     --tolerance IN    compare: largest accepted shift or size change (default 1/60 in across, 1/72 in down); extents: largest accepted width or left edge difference (default 0.1 in)
     --expect FILE     extents: the expected text lines
   ```
4. § Command line — after the fenced block that shows the `compare` output (it ends with the line that starts `  PASS  sheet 1 dots:`), insert verbatim:
   ````
   `extents` checks a job rendered as graphics by a printer driver against the text the page is known to hold, and exits with 1 when a line differs. FILE has one text line per row, tab separated, in inches: the sheet (from 0), the left edge, the right edge, the baseline, and the text. Rows that start with `#` are comments.

   ```
   0	0.789	6.502	1.472	Free your printing. Wubiq: Ubiquituous printing from anywhere to everywhere.
   ```

   The witness splits the sheet's dots into horizontal bands, places the first expected line on the first band, and measures every expected line that has a band of its own:
   - the band must be as wide as the line, within the tolerance;
   - its left edge must be shifted like the other lines', within the tolerance.

   Lines that share a band with another line are reported as notes. At least three lines must be measured.

   `wubiq-client/tests-src/net/sf/wubiq/dotmatrix/golden/test-page.extents` holds the lines of wubiq-server's test page.
   ````
5. § Testing wubiq-client end to end › 2. Start wubiq-server — replace the paragraph ``` `ant wubiq-server` also builds wubiq-common and wubiq-client. Both of those builds copy their jars into `wubiq-android-gradle/app/libs/`; don't commit those copies by accident. ``` with ``` `ant wubiq-server` also builds wubiq-common. ```
6. § Testing wubiq-client end to end › 3. Configure wubiq-client, step 2 — replace from `2. Start the client with the JDK you want to test.` through the fenced block that ends with `-jar wubiq-client/dist/wubiq-client.jar -h http://localhost:8080 -u client-test -v` (the Apple Java 6 command) with, verbatim:
   ````
   2. Start the client. The client built from source needs Java 25:

      ```
      (cd wubiq-client && ant create-launcher-jar)
      java -jar wubiq-client/build-tests/wubiq-client-launcher.jar -h http://localhost:8080 -u client-test -v
      ```

      Start it with `-jar`: the jar's manifest opens the JDK packages the client uses by reflection (`Add-Opens`), and `-cp` skips the manifest.

      To test an older Java, start the 2.7.4 jar wubiq-server serves to the installed clients:

      ```
      java -jar wubiq-server/WebContent/wubiq-client.jar -h http://localhost:8080 -u client-test -v
      ```

      On Apple Silicon, start Apple's Java 6 with `-Xint -XX:+UseMembar -Djava.awt.headless=true`:

      ```
      /Library/Java/JavaVirtualMachines/1.6.0.jdk/Contents/Home/bin/java -Xint -XX:+UseMembar -Djava.awt.headless=true -jar wubiq-server/WebContent/wubiq-client.jar -h http://localhost:8080 -u client-test -v
      ```
   ````
7. § Testing wubiq-client end to end › 4. Print and inspect the output — replace the sentence `` `wubiq-client/test-dotmatrix-e2e.sh` runs these steps on every installed JDK and compares each job with the Java 6 one. `` with `` `wubiq-client/test-dotmatrix-e2e.sh` runs these steps with the 2.7.4 jar on every installed JDK, with the client built from source on JDK 25 and with a packaged client, compares each job with the Java 6 one and checks its text extents. ``
8. § Testing a Windows client, numbered list item 4 — replace it with: `4. runs `wubiq-client/test-dotmatrix-e2e-windows.ps1`, which prints wubiq-server's test page with the 2.7.4 jar on every JDK and with the packaged client, through the printer driver and through the fast path (the client writes ESC/P to a file), compares each job with the Oracle Java 6 one and checks the text extents of the packaged client's job.`
9. § Java API — replace the last sentence `` `wubiq-client/tests-src/net/sf/wubiq/dotmatrix` uses this API to check the 2.x client's ESC/P path on every installed JDK (`wubiq-client/test-dotmatrix-jdks.sh`). `` with `` `wubiq-client/tests-src/net/sf/wubiq/dotmatrix` uses this API to check the 2.x client's ESC/P path on every installed JDK 25 or later (`wubiq-client/test-dotmatrix-jdks.sh`). ``

##### Acceptance criteria
- [ ] AC-1: the CHANGELOG entry sits under `## Wubiq 2.8.0` › `### Wubiq-dotmatrix-witness` and
      carries no issue reference (mechanical — no red test; verified by
      `git diff origin/master...HEAD -- CHANGELOG.md | grep -E '^\+.*\(#[0-9]+\)'` returning nothing)
- [ ] AC-2: nothing but this application's documentation modified, apart from this issue's
      spec file (its AC ticks) (mechanical — no red test; verified by
      `git diff --name-only origin/master...HEAD | grep -vE '^CHANGELOG\.md$|^wubiq-dotmatrix-witness/.*\.md$|^specs/issue-52-enriched-spec\.md$'`
      returning nothing)
- [ ] AC-3: `wubiq-dotmatrix-witness/README.md` documents the `extents` command and the Java 8 level, and no longer mentions Java 6 as a build or run level nor the copies into the Android project (mechanical — no red test; verified by `grep -c -E 'extents --expect FILE|--expect FILE     extents|compiles for Java 8' wubiq-dotmatrix-witness/README.md` printing `3` and `grep -c -E 'compiles for Java 6|runs on Java 6 and later|app/libs' wubiq-dotmatrix-witness/README.md` printing `0`)

##### Green checks
1. `.claude/scripts/gate.sh` clean (code-issue); `.claude/scripts/suite.sh` — full suite green
   (review-issue runs the suite)
<!-- section:WIT-DOC-1:end -->
<!-- section:WIT-BLD-1:start -->
### WIT-BLD-1 — Build · wubiq-dotmatrix-witness · Java 8 level
Depends on: WIT-DOC-1

#### Documentation references
`wubiq-dotmatrix-witness/README.md` § Build.

#### Implementation details
Language level: `source="1.6" target="1.6"` in the two `javac` tasks of `wubiq-dotmatrix-witness/build.xml` (targets `compile`, `compile-tests`). This section raises it to Java 8.

##### Acceptance criteria
- [ ] AC-1: both `javac` tasks compile for Java 8 (mechanical — no red test; verified by `grep -c 'source="1.8" target="1.8" release="8"' wubiq-dotmatrix-witness/build.xml` printing `2` and `grep -c '1\.6' wubiq-dotmatrix-witness/build.xml` printing `0`)
- [ ] AC-2: Given JDK 8, whose `javac` has no `--release` option, when the gate compiles `WIT` and `CLI`, then both compile: Ant ignores `release` on JDK 8 (mechanical — no red test; verified by `.claude/scripts/gate.sh` ending `EXIT: 0`)

##### Code changes
1. `wubiq-dotmatrix-witness/build.xml` targets `compile` and `compile-tests` — in each `javac` task replace `target="1.6" source="1.6"` with `source="1.8" target="1.8" release="8"`. Change no other attribute.

##### Green tests
1. `.claude/scripts/suite.sh` — full suite green (review-issue runs this)
2. `.claude/scripts/gate.sh` clean
<!-- section:WIT-BLD-1:end -->
<!-- section:WIT-BE-1:start -->
### WIT-BE-1 — Backend · wubiq-dotmatrix-witness · `extents` command
Depends on: WIT-DOC-1

#### Documentation references
`wubiq-dotmatrix-witness/README.md` § Command line (the `extents` paragraphs and the expected file format).

#### Implementation details
Language level: Java 1.6 syntax and classes (the level `wubiq-dotmatrix-witness/build.xml` sets until WIT-BLD-1 merges; this section does not depend on it).

Fixed values:

| Name | Value |
|---|---|
| Row height | 1/144 in: a dot at `y` inches is in row `Math.round(y * 144)` |
| Band break | two consecutive ink rows more than 3 rows apart start a new band |
| Same baseline | expected lines whose baselines differ by at most 1/144 in are one line |
| Band slack | 0.05 in |
| Default tolerance | 0.1 in |
| Lines needed | 3 measured lines per sheet |

##### Acceptance criteria
- [ ] AC-1: Given a job with three graphics bars at 1 in from the left, 2.0, 3.0 and 1.5 in wide, on three text lines, and three expected lines of the same widths whose left edge is 1.25 in, when `TextExtents#check(EscpResult, List)` runs, then the result passes with three `PASS` items
- [ ] AC-2: Given the same expected lines and bars 0.55 times as wide (1.1, 1.65 and 0.833 in), when `check` runs, then the result fails and names the measured and the expected width of the first line: `width 1.100 in, expected 2.000 in`
- [ ] AC-3: Given the bars of AC-1 with the third one starting at 1.25 in, when `check` runs, then the result fails on the third line only, naming its left edge
- [ ] AC-4: Given a job with two bars and two expected lines, when `check` runs, then the result fails with `only 2 text line(s) measured, 3 needed`; given a job without graphics, it fails with `no graphics dots to measure` (error paths: too little to measure never passes)
- [ ] AC-5: Given an expected file whose second row has two fields, when `TextExtents#read(File)` reads it, then it throws an `IOException` whose message contains `:2:`
- [ ] AC-6: `Main` accepts the `extents` command and refuses it without `--expect` (mechanical — no red test; verified by `ant -q -f wubiq-dotmatrix-witness/build.xml compile && java -cp wubiq-dotmatrix-witness/bin net.sf.wubiq.dotmatrix.witness.Main extents wubiq-client/tests-src/net/sf/wubiq/dotmatrix/golden/test-page-9pin.escp; echo $?` printing `extents needs --expect FILE and the job to check.`, the usage text with the `extents` line, and `2`)

##### Test ports
- `net.sf.wubiq.dotmatrix.witness.TextExtents#check(EscpResult, java.util.List<TextExtents.Line>)` · in: an interpreted job and the expected lines · out: `CheckResult` · new (`grep -rIl TextExtents` over the repository returns nothing)
- `net.sf.wubiq.dotmatrix.witness.TextExtents#read(java.io.File)` · in: the expected file · out: `List<TextExtents.Line>`; `IOException` on a malformed row · new
- `net.sf.wubiq.dotmatrix.witness.TextExtents.Line#Line(int, double, double, double, String)` · in: sheet (from 0), left, right, baseline in inches, text · new
- `net.sf.wubiq.dotmatrix.witness.EscpInterpreter#interpret(byte[])` · existing (`wubiq-dotmatrix-witness/src/net/sf/wubiq/dotmatrix/witness/EscpInterpreter.java`), reached through the test helper `EscpInterpreterTest#interpret(Object...)`

##### Red tests (write first; each must fail before implementation)
Every row: add to `wubiq-dotmatrix-witness/tests-src/net/sf/wubiq/dotmatrix/witness/EscpInterpreterTest.java`, the witness's one test class (target `test` of `wubiq-dotmatrix-witness/build.xml` runs that class by name).

Shared setup, as two private helpers in that file:
- `bars(int[] starts, int[] columns)` returns an `EscpResult`: a `List<Object>` holding `ESC, '@'`, then for each bar `ESC, '$', starts[i], 0, ESC, 'K', columns[i], 0`, then `columns[i]` times the value `0x80`, then `CR, LF, LF`; then `FF`; interpreted with the existing `interpret(Object...)` helper (`interpret(stream.toArray())`). `ESC $ n 0` moves to `n/60` in; `ESC K` prints 60 columns per inch; `0x80` sets the top dot; each `LF` advances 1/6 in.
- `expectedLines(double[] widths)` returns the `List<TextExtents.Line>`: line `i` is `new TextExtents.Line(0, 1.25, 1.25 + widths[i], 0.5 + i / 3d, "line " + i)`.

| AC | Placement | Setup | Test file | Test name | Assertion sketch |
|---|---|---|---|---|---|
| AC-1 | add to EscpInterpreterTest.java | `bars(new int[] {60, 60, 60}, new int[] {120, 180, 90})`, `expectedLines(new double[] {2.0, 3.0, 1.5})` | EscpInterpreterTest.java | `textExtentsMatchExpectedWidths` | `assertTrue(check.toString(), check.isPassed()); assertEquals(3, check.count(CheckResult.Status.PASS))` |
| AC-2 | add to that file | `bars(new int[] {60, 60, 60}, new int[] {66, 99, 50})`, same expected lines | same | `textExtentsFindNarrowedText` | `assertFalse(check.isPassed()); assertTrue(check.toString(), check.toString().contains("width 1.100 in, expected 2.000 in"))` |
| AC-3 | add to that file | `bars(new int[] {60, 60, 75}, new int[] {120, 180, 90})`, same expected lines | same | `textExtentsFindAMovedLine` | `assertFalse(check.isPassed()); assertEquals(1, check.count(CheckResult.Status.FAIL)); assertTrue(check.toString(), check.toString().contains("left edge"))` |
| AC-4 | add to that file | `bars(new int[] {60, 60}, new int[] {120, 180})` with `expectedLines(new double[] {2.0, 3.0})`; and `interpret(ESC, '@', "Total", CR, FF)` with the three expected lines | same | `textExtentsNeedEnoughLines` | `assertTrue(first.toString().contains("only 2 text line(s) measured, 3 needed")); assertTrue(second.toString().contains("no graphics dots to measure")); assertFalse(first.isPassed()); assertFalse(second.isPassed())` |
| AC-5 | add to that file | a temporary file (`File.createTempFile("extents", ".txt")`, deleted in `finally`) written as UTF-8 with the rows `0\t1.25\t3.25\t0.5\tline 0` and `0\t1.25` | same | `textExtentsFileWithABadRowIsRefused` | `try { TextExtents.read(file); fail(); } catch (IOException e) { assertTrue(e.getMessage(), e.getMessage().contains(":2:")); }` |

##### Code changes
1. New file `wubiq-dotmatrix-witness/src/net/sf/wubiq/dotmatrix/witness/TextExtents.java`, `public class TextExtents`:
   - `public static class Line` with the public constructor `Line(int page, double left, double right, double baseline, String text)` and the getters `getPage()`, `getLeft()`, `getRight()`, `getBaseline()`, `getText()`.
   - `private double tolerance = 0.1;` and `public TextExtents setTolerance(double tolerance)`, returning `this`.
   - `public static List<Line> read(File file) throws IOException`: reads the file as UTF-8, line by line; skips empty rows and rows starting with `#`; splits on the tab character; a row with fewer than 4 fields, or whose first four fields do not parse (`Integer.parseInt`, `Double.parseDouble`), throws `new IOException(file.getName() + ":" + lineNumber + ": expected sheet, left, right and baseline separated by tabs")`, `lineNumber` counted from 1; the text is the fifth field, `""` when absent. Closes the reader in `finally`.
   - `public CheckResult check(EscpResult result, List<Line> expected)`: returns one `CheckResult` for every sheet named by an expected line, sheets in ascending order. Per sheet `p`, with subject prefix `"sheet " + (p + 1)`:
     1. Rows. For each `BitImage` of `result.getBitImages(p)`, each column `c` whose `getColumns()[c]` is not 0 and each dot `d` with `isSet(c, d)`: `x = getX() + c * getColumnWidth()`, `y = getY() + d * getDotPitch()`, `row = (int) Math.round(y * 144)`. Keep per row the smallest `x` and the largest `x + getColumnWidth()`, in a `TreeMap<Integer, double[]>`.
     2. No rows: `fail("sheet N", "no graphics dots to measure")`; go to the next sheet.
     3. Bands. Walk the rows in ascending order; a row more than 3 above the previous row starts a new band. A band has `top` (first row / 144), `bottom` (last row / 144), `left` (smallest `x` of its rows) and `right` (largest right edge of its rows).
     4. Expected lines of the sheet, sorted by baseline; consecutive lines whose baselines differ by at most `1d / 144` are merged into one: smallest left, largest right, the baseline of the first, the texts joined with one space.
     5. `dy = bands.get(0).bottom - merged.get(0).baseline`.
     6. A band *holds* a merged line when `band.top - 0.05 <= line.baseline + dy` and `line.baseline + dy <= band.bottom + 0.05`. A line is *measured* when exactly one band holds it and that band holds no other merged line.
     7. For each measured line: `width = band.right - band.left`, `expectedWidth = line.right - line.left`, `shift = band.left - line.left`.
     8. `median` = the element at index `size / 2` of the measured lines' shifts sorted ascending.
     9. For each measured line, with subject `"sheet N \"" + text + "\""` and message `String.format(Locale.US, "width %.3f in, expected %.3f in; left edge shifted %+.3f in, the sheet's lines %+.3f in", width, expectedWidth, shift, median)`: `fail` when `Math.abs(width - expectedWidth) > tolerance` or `Math.abs(shift - median) > tolerance`; otherwise `pass`.
     10. For each merged line that is not measured: `note` with the same subject form and the message `not measured: no ink band of its own`.
     11. When fewer than 3 lines are measured: `fail("sheet N", "only " + count + " text line(s) measured, 3 needed")`.
     `CheckResult#pass(String, String)`, `#fail(String, String)` and `#note(String, String)` are existing (`wubiq-dotmatrix-witness/src/net/sf/wubiq/dotmatrix/witness/CheckResult.java`).
2. `wubiq-dotmatrix-witness/src/net/sf/wubiq/dotmatrix/witness/Main.java`:
   - add the field `private File expect;`;
   - `Main#run(String[])`, option loop — add, before the branch `else if (arg.startsWith("--"))`: `else if ("--expect".equals(arg)) { expect = new File(value(args, ++index, arg)); }`;
   - `Main#run(String[])`, command dispatch — add, after the `compare` branch: `else if ("extents".equals(command)) { return extents(); }`;
   - new method `private int extents() throws IOException`: when `expect == null` or `inputs.size() != 1`, `throw new IllegalArgumentException("extents needs --expect FILE and the job to check.")`; otherwise interpret the job as `Main#compare()` does (`new EscpInterpreter(profile).interpret(read(new File(inputs.get(0))))`), build `new TextExtents()`, call `setTolerance(tolerance)` when `tolerance != null`, run `check(result, TextExtents.read(expect))`, print `(check.isPassed() ? "EXTENTS OK" : "EXTENTS DIFFERENT") + ": " + inputs.get(0)` and then every item as `"  " + item`, and return `0` when it passed, `1` otherwise;
   - `Main#usage()` — after the `compare` usage line add `"  java -jar wubiq-dotmatrix-witness.jar extents --expect FILE [options] JOB\n"`, replace the `--tolerance` line with `"  --tolerance IN    compare: largest accepted shift or size change (default 1/60 in across, 1/72 in down); extents: largest accepted width or left edge difference (default 0.1 in)\n"` and add after it, as the last line, `"  --expect FILE     extents: the expected text lines"`;
   - the class Javadoc `<pre>` block — add the line `  extents --expect FILE [options] JOB   check the printed text widths against the expected lines; exit 1 when one differs`.

##### Green tests
1. The red tests above pass, unmodified (code-issue runs these, targeted:
   `.claude/scripts/section-test.sh WIT net.sf.wubiq.dotmatrix.witness.EscpInterpreterTest`)
2. `.claude/scripts/suite.sh` — full suite green (review-issue runs this; code-issue runs only
   this section's test classes)
3. `.claude/scripts/gate.sh` clean
<!-- section:WIT-BE-1:end -->
<!-- section:CLI-DOC-1:start -->
### CLI-DOC-1 — Documentation · wubiq-client
Depends on: WIT-DOC-1

#### Documentation references
`wubiq-client/README.md` § Running, § Dot-matrix tests › Current JDK, › Every installed JDK, › End to end, through wubiq-server, › End to end on Windows.

#### Implementation details

##### CHANGELOG.md
1. § `## Wubiq 2.8.0` — after the last bullet of `### Wubiq-dotmatrix-witness`, insert verbatim:
   ```
   ### Wubiq-client
   - The client is compiled for Java 25 and is distributed inside the native packages of wubiq-installer, with their own Java runtime. Installed 2.7.4 clients keep receiving the 2.7.4 jar from wubiq-server.
   - The build no longer copies `wubiq-client.jar` into `wubiq-android-gradle`.
   - The dot-matrix end to end scripts print with the 2.7.4 jar on every JDK, with the client built from source on JDK 25 and with a packaged client, and check the text extents of each job.
   ```

##### wubiq-client/README.md
1. § Running — replace the whole section body (from the fenced block through the paragraph that starts `The client runs on Java 6 and later.`) with, verbatim:
   ````
   The client runs on Java 25. On a computer that prints, install the native package built by `../wubiq-installer`: it carries its own Java runtime and runs the client as a service, so no Java has to be installed.

   From a build, start it with `-jar`:

   ```
   java -jar wubiq-client.jar -h http://SERVER:8080 -u UUID
   ```

   Its manifest opens the JDK packages the client uses by reflection (`Add-Opens: java.base/java.lang java.desktop/javax.print java.desktop/sun.print`, kept in the `client.add.opens` property of `build.xml`). Java applies it only to the jar given to `-jar`; with `-cp`, pass the same packages as `--add-opens PACKAGE=ALL-UNNAMED`.
   ````
2. § Dot-matrix tests › Current JDK — replace `Run it with a JDK 8.` with `Run it with a JDK 25.`
3. § Dot-matrix tests › Every installed JDK — replace the section body, from the fenced block `./test-dotmatrix-jdks.sh [--strict]` through the paragraph that starts `Java 6 runs with`, with, verbatim:
   ````
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
   ````
4. § Dot-matrix tests › End to end, through wubiq-server — replace the section body, from the fenced block `./test-dotmatrix-e2e.sh [--update-golden]` through the `Exit codes:` list, with, verbatim:
   ````
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
   ````
5. § Dot-matrix tests › End to end on Windows — replace the section body, from the fenced block that starts `.\test-dotmatrix-e2e-windows.ps1 -Jdks` through the end of the file, with, verbatim:
   ````
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
   ````

##### Acceptance criteria
- [ ] AC-1: the CHANGELOG entry sits under `## Wubiq 2.8.0` › `### Wubiq-client` and
      carries no issue reference (mechanical — no red test; verified by
      `git diff origin/master...HEAD -- CHANGELOG.md | grep -E '^\+.*\(#[0-9]+\)'` returning nothing)
- [ ] AC-2: nothing but this application's documentation modified, apart from this issue's
      spec file (its AC ticks) (mechanical — no red test; verified by
      `git diff --name-only origin/master...HEAD | grep -vE '^CHANGELOG\.md$|^wubiq-client/.*\.md$|^specs/issue-52-enriched-spec\.md$'`
      returning nothing)
- [ ] AC-3: `wubiq-client/README.md` states Java 25 and documents the three client rows (mechanical — no red test; verified by `grep -c -E 'The client runs on Java 25\.|--packaged-java JAVA --packaged-app DIR|-PackagedJava JAVA -PackagedApp DIR|Run it with a JDK 25\.' wubiq-client/README.md` printing `4` and `grep -c -E 'Java 6 and later|JDK 8|source and target 1\.6' wubiq-client/README.md` printing `0`)

##### Green checks
1. `.claude/scripts/gate.sh` clean (code-issue); `.claude/scripts/suite.sh` — full suite green
   (review-issue runs the suite)
<!-- section:CLI-DOC-1:end -->
<!-- section:CLI-BLD-1:start -->
### CLI-BLD-1 — Build · wubiq-client · Java 8 level, no copy into the Android project
Depends on: CLI-DOC-1

#### Documentation references
`CHANGELOG.md` § `## Wubiq 2.8.0` › `### Wubiq-client`, second bullet.

#### Implementation details
Language level: `source="1.6" target="1.6"` in the four `javac` tasks of `wubiq-client/build.xml`: one in target `create-client-jar`, two in target `compile` (the first compiles `wubiq-common/src`, the second the client), one in target `compile-tests`. This section raises all four to Java 8.

##### Acceptance criteria
- [ ] AC-1: the four `javac` tasks compile for Java 8 (mechanical — no red test; verified by `grep -c 'source="1.8" target="1.8" release="8"' wubiq-client/build.xml` printing `4` and `grep -c '1\.6' wubiq-client/build.xml` printing `0`)
- [ ] AC-2: no target of `wubiq-client/build.xml` writes into `wubiq-android-gradle` (mechanical — no red test; verified by `grep -c -E 'basedir\.gradle|app/libs' wubiq-client/build.xml` printing `0`)
- [ ] AC-3: Given JDK 8, whose `javac` has no `--release` option, when the gate compiles `CLI` and the applications that depend on it, then they compile: Ant ignores `release` on JDK 8, and `INS` still compiles at `-source 1.6` against the Java 8 classes (mechanical — no red test; verified by `.claude/scripts/gate.sh` ending `EXIT: 0`)

##### Code changes
1. `wubiq-client/build.xml` — delete the line
   `<property name="basedir.gradle" value="${workspace.dir}/wubiq-android-gradle" />`.
2. `wubiq-client/build.xml` target `create-client-jar` — delete the line
   `<copy todir="${basedir.gradle}/app/libs/" file="${output.jar}.jar"/>`.
3. `wubiq-client/build.xml` targets `create-client-jar`, `compile`, `compile-tests` — in each of the four `javac` tasks replace `target="1.6" source="1.6"` with `source="1.8" target="1.8" release="8"`. Change no other attribute.

##### Green tests
1. `.claude/scripts/suite.sh` — full suite green (review-issue runs this)
2. `.claude/scripts/gate.sh` clean
<!-- section:CLI-BLD-1:end -->
<!-- section:CLI-BLD-2:start -->
### CLI-BLD-2 — Build · wubiq-client · client sources and tests at Java 25
Depends on: CLI-DOC-1, CLI-BLD-1, WIT-BLD-1

#### Documentation references
`wubiq-client/README.md` § Running; § Dot-matrix tests › Current JDK.

#### Implementation details
Language level: after CLI-BLD-1, `source="1.8" target="1.8" release="8"` in the four `javac` tasks of `wubiq-client/build.xml`. This section raises the three that compile client code to Java 25. The task that compiles `wubiq-common/src` stays at Java 8: wubiq-server loads those classes on Java 8.

##### Acceptance criteria
- [ ] AC-1: the client `javac` tasks compile for Java 25 and the common one for Java 8 (mechanical — no red test; verified by `grep -c 'release="25"' wubiq-client/build.xml` printing `3`, `grep -c 'source="1.8" target="1.8" release="8"' wubiq-client/build.xml` printing `1`, and `grep -B1 'release="8"' wubiq-client/build.xml | grep -c 'basedir.common}/src'` printing `1`)
- [ ] AC-2: the compiled client classes are Java 25 classes and the common classes Java 8 classes (mechanical — no red test; verified, after `.claude/scripts/gate.sh`, by `javap -v -cp wubiq-client/bin net.sf.wubiq.clients.LocalPrintManager | grep 'major version'` printing `major version: 69` and `javap -v -cp wubiq-common/bin net.sf.wubiq.utils.Labels | grep 'major version'` printing `major version: 52`)
- [ ] AC-3: commons-cli 1.2 and commons-logging 1.1.1 load on Java 25 (mechanical — no red test; verified, with a JDK 25 as `JAVA_HOME`, by `ant -q -f wubiq-client/build.xml create-launcher-jar && "$JAVA_HOME/bin/java" -jar wubiq-client/build-tests/wubiq-client-launcher.jar --help` printing the usage that starts `usage: java -jar wubiq-client.jar`)
- [ ] AC-4: Given a Java older than 25, when it starts the launcher jar, then it stops with `UnsupportedClassVersionError` and prints nothing else (error path: an old Java fails loudly; mechanical — no red test; verified by running the command of AC-3 with the `java` of a JDK 17 and reading `java.lang.UnsupportedClassVersionError` in its output)
- [ ] AC-5: the dot-matrix suite runs on JDK 25 (mechanical — no red test; verified by `.claude/scripts/suite.sh` ending `EXIT: 0`)
- [ ] AC-6: cglib-nodep 3.1 works on Java 25: the client built from source prints the server's test page (manual — no red test; verified by the `source-java-VERSION` row of `wubiq-client/test-dotmatrix-e2e.sh` reading `SAME`, on a Mac with the `Witness_9-pin` queue, once CLI-BLD-3 is merged)

##### Code changes
1. Precondition, checked before any edit: `awk -F'\t' '$1=="CLI"{print $4}' .claude/scripts/apps.tsv` prints `25`. When it prints `8`, PR H1 ("Out of Scope") is not merged: change nothing and end with `STATUS: BLOCKED — hand-pr-h1-not-merged`.
2. `wubiq-client/build.xml` target `create-client-jar` — in its `javac` task replace `source="1.8" target="1.8" release="8"` with `release="25"`.
3. `wubiq-client/build.xml` target `compile` — in its second `javac` task (the one with `<src refid="project.src"/>`) replace `source="1.8" target="1.8" release="8"` with `release="25"`. Leave the first `javac` task (`srcdir="${basedir.common}/src"`) unchanged.
4. `wubiq-client/build.xml` target `compile-tests` — in its `javac` task replace `source="1.8" target="1.8" release="8"` with `release="25"`.
5. `wubiq-client/build.xml` — replace the comment above the `client.add.opens` property with
   `<!-- Packages the client reaches by reflection; Java opens them when the jar runs with -jar. -->`.

##### Green tests
1. `.claude/scripts/suite.sh` — full suite green (review-issue runs this)
2. `.claude/scripts/gate.sh` clean
<!-- section:CLI-BLD-2:end -->
<!-- section:CLI-BLD-3:start -->
### CLI-BLD-3 — Build · wubiq-client · dot-matrix scripts for the frozen jar and the packaged client
Depends on: CLI-DOC-1, CLI-BLD-2, WIT-BE-1

#### Documentation references
`wubiq-client/README.md` § Dot-matrix tests › Every installed JDK, › End to end, through wubiq-server, › End to end on Windows.

#### Implementation details
Language level: not applicable — this section edits the shell and PowerShell scripts under `wubiq-client/`; it changes no Java source.

Names used by every step:

| Name | Value |
|---|---|
| Legacy jar | `wubiq-client/../wubiq-server/WebContent/wubiq-client.jar` (existing, tracked) |
| Launcher jar | `wubiq-client/build-tests/wubiq-client-launcher.jar`, written by target `create-launcher-jar` of `wubiq-client/build.xml` (existing) |
| Extents file | `wubiq-client/tests-src/net/sf/wubiq/dotmatrix/golden/test-page.extents` (written by CLI-BE-1; the scripts work without it) |
| Client main class | `net.sf.wubiq.clients.LocalPrintManager` (existing, `wubiq-client/src/net/sf/wubiq/clients/LocalPrintManager.java`) |
| Add-opens options | `--add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.desktop/javax.print=ALL-UNNAMED --add-opens=java.desktop/sun.print=ALL-UNNAMED` |
| Witness command | `extents --expect FILE JOB` of `net.sf.wubiq.dotmatrix.witness.Main` (WIT-BE-1): exit code 0 and first output line `EXTENTS OK: …`, or exit code 1 and `EXTENTS DIFFERENT: …` followed by `  FAIL  …` lines |

##### Acceptance criteria
- [ ] AC-1: `jdk-homes.sh` sets `COMPILE_HOME` to a JDK 25 with `javac` and still sets `SELECTED`, `ALL_JDKS` and `SEEN` as before (mechanical — no red test; verified by `grep -c '"\$m" = "25"' wubiq-client/jdk-homes.sh` printing `1`, `grep -c '"\$m" = "8"' wubiq-client/jdk-homes.sh` printing `0`, and `.claude/scripts/gate.sh` ending `EXIT: 0`, which finds its JDKs through this file)
- [ ] AC-2: `test-dotmatrix-jdks.sh` compiles with JDK 25, runs JDK 25 and later only and takes Java 25 as the baseline (mechanical — no red test; verified by `bash -n wubiq-client/test-dotmatrix-jdks.sh`, `grep -c 'BASELINE=25' wubiq-client/test-dotmatrix-jdks.sh` printing `1` and `grep -c -E 'UseMembar|BASELINE=8|for wanted in' wubiq-client/test-dotmatrix-jdks.sh` printing `0`)
- [ ] AC-3: `test-dotmatrix-e2e.sh` has the three kinds of rows and the extents rows (mechanical — no red test; verified by `bash -n wubiq-client/test-dotmatrix-e2e.sh` and `grep -c -E 'legacy-java-|source-java-|--packaged-java|--packaged-app|extents --expect|WebContent/wubiq-client.jar' wubiq-client/test-dotmatrix-e2e.sh` printing at least `6`)
- [ ] AC-4: `test-dotmatrix-e2e-windows.ps1` has the three kinds of rows and the extents rows (mechanical — no red test; verified by `grep -c -E 'legacy-java-|source-java-|PackagedJava|PackagedApp|extents --expect|WebContent\\wubiq-client.jar' wubiq-client/test-dotmatrix-e2e-windows.ps1` printing at least `6`)
- [ ] AC-5: Given no extents file, when either end to end script reaches an extents row, then the row reads `NOT CHECKED` and the exit code is not changed by it; given `--packaged-java` without `--packaged-app`, the shell script prints an error and exits with 2 before it prints anything (degraded and error paths; mechanical — no red test; verified by `./wubiq-client/test-dotmatrix-e2e.sh --packaged-java /bin/true; echo $?` printing the error and `2`, and by `grep -c 'NOT CHECKED' wubiq-client/test-dotmatrix-e2e.sh wubiq-client/test-dotmatrix-e2e-windows.ps1` printing at least `1` for each file)
- [ ] AC-6: on a Mac with the `Witness_9-pin` queue and wubiq-server running, `./test-dotmatrix-e2e.sh` lists a `legacy-java-VERSION` row per installed JDK build, one `source-java-VERSION` row and its `-extents` row, the `source` row reads `SAME` and its extents row `EXTENTS OK` (manual — no red test; verified by running the script from `wubiq-client/` after `(cd wubiq-server && ant wubiq-server) && docker compose up -d` and reading `build-tests/dotmatrix-e2e/summary.txt`)

##### Code changes
1. `wubiq-client/jdk-homes.sh` — in the header comment replace `COMPILE_HOME  a JDK 8 with javac (empty when there is none)` with `COMPILE_HOME  a JDK 25 with javac (empty when there is none)`; in the last loop replace `[ "$m" = "8" ]` with `[ "$m" = "25" ]`. Change nothing else: `.claude/scripts/apps.sh` sources this file and reads `SELECTED`.
2. `wubiq-client/test-dotmatrix-jdks.sh`:
   1. Header comment: replace the line `# - Compiles once with a JDK 8 (javac 12+ can't target Java 6).` with `# - Compiles once with a JDK 25.`; replace the two lines that start `# - Runs DotMatrixWitnessTest on Java 6, 8, 11, 17, 21` and `#   or on the JDK homes listed` with `# - Runs DotMatrixWitnessTest on every installed JDK 25 or later, or on the JDK homes listed in` and `#   WUBIQ_JDKS (space separated); older JDKs are skipped.`; in the `--strict` line replace `Java 8's` with `Java 25's`.
   2. Replace the message `A JDK 8 (with javac) is needed to compile for Java 6.` with `A JDK 25 (with javac) is needed to compile the client.`
   3. Delete the `for wanted in 6 8 11 17 21; do … done` loop (three lines).
   4. Delete the functions `self_check` and `retry`, with their comments.
   5. In the `while IFS='|' read -r m home version` loop: after `[ -n "$m" ] || continue` add `[ "$m" -ge 25 ] || continue`; replace the whole `if [ "$m" = "6" ]; then … else … fi` statement with the four lines of its `else` branch (`run_java org.junit.runner.JUnitCore $SELF_CHECK $TEST > "$LOG" 2>&1` through `[ $status -gt 1 ] && echo "  (JVM exited with $status)"`).
   6. Replace `BASELINE=8` with `BASELINE=25`.
3. `wubiq-client/test-dotmatrix-e2e.sh`:
   1. Arguments. Replace the single `--update-golden` test with a loop over `"$@"` that sets `UPDATE=true` for `--update-golden`, `PACKAGED_JAVA` for `--packaged-java VALUE` and `PACKAGED_APP` for `--packaged-app VALUE`, and exits with 2 after printing `Unknown option: ARG` to stderr for anything else. After the loop: when exactly one of `PACKAGED_JAVA` and `PACKAGED_APP` is set, print `--packaged-java and --packaged-app go together.` to stderr and exit with 2; when both are set and `"$PACKAGED_JAVA"` is not executable or `"$PACKAGED_APP"` is not a directory, print `No packaged client at $PACKAGED_JAVA and $PACKAGED_APP.` to stderr and exit with 2. This check runs before every other setup check.
   2. Setup. Add `LEGACY_JAR="$BASE/../wubiq-server/WebContent/wubiq-client.jar"` and `EXTENTS="$BASE/tests-src/net/sf/wubiq/dotmatrix/golden/test-page.extents"`. Replace the message `A JDK 8 (with javac) is needed to compile for Java 6.` with `A JDK 25 (with javac) is needed to compile the client.` Add the check: when `"$LEGACY_JAR"` is not a file, print `No 2.7.4 jar at $LEGACY_JAR.` to stderr and exit with 2.
   3. `print_with` takes the row name first and the client command after it: `print_with NAME COMMAND [ARG…]`. Its `uuid` is `witness-$NAME`. It starts the client with `( cd "$OUT" && exec "$@" ) > "$OUT/client.log" 2>&1 &` after `shift`. Its body is otherwise unchanged. The Java 6 options move to the caller.
   4. A new function `check_row NAME` holds what the main loop does today after a successful print: finds `JOB`, copies the golden file under `--update-golden`, runs `$WITNESS_JAVA compare "$GOLDEN" "$JOB/job.escp" > "$OUT/compare.txt" 2>&1` and appends the `SAME` or `DIFFERENT` line for `NAME` to `"$SUMMARY"`, counting `FAILED` as today.
   5. A new function `check_extents NAME`: when `"$EXTENTS"` is not a file, appends `NAME-extents<TAB>NOT CHECKED<TAB>no $EXTENTS` to `"$SUMMARY"`; otherwise runs `$WITNESS_JAVA extents --expect "$EXTENTS" "$JOB/job.escp" > "$OUT/extents.txt" 2>&1`, and appends `NAME-extents<TAB>EXTENTS OK<TAB>$OUT/extents.txt` when it exits with 0, or `NAME-extents<TAB>EXTENTS DIFFERENT<TAB>` followed by the first `FAIL` line of `extents.txt` (as the `DIFFERENT` line does), adding 1 to `FAILED`.
   6. A new function `run_row NAME ATTEMPTS COMMAND [ARG…]`: sets `OUT="$REPORT/$NAME"`, calls `print_with NAME COMMAND…` up to `ATTEMPTS` times until it succeeds; on failure appends the `NOT PRINTED` line and adds 1 to `FAILED`; on success calls `check_row NAME`. It returns 0 when the job printed.
   7. Rows, in this order, replacing the current `while … done <<EOF $ALL_JDKS EOF` loop:
      - for every line `m|home|version` of `$ALL_JDKS` (under `--update-golden`, only `m = 6`): `name="legacy-java-$(echo "$version" | tr '._+' '---')"`; `run_row "$name" ATTEMPTS "$home/bin/java" [-Xint -XX:+UseMembar -Djava.awt.headless=true] -jar "$LEGACY_JAR" -h "$SERVER" -u "witness-$name" -v`, the bracketed options and `ATTEMPTS=3` only when `m = 6`, `ATTEMPTS=1` otherwise;
      - unless `--update-golden`: `name="source-java-$(echo "$COMPILE_VERSION" | tr '._+' '---')"`, `COMPILE_VERSION` being the output of `java_version "$COMPILE_HOME"` (function of `jdk-homes.sh`); `run_row "$name" 1 "$COMPILE_HOME/bin/java" -jar "$LAUNCHER" -h "$SERVER" -u "witness-$name" -v && check_extents "$name"`;
      - unless `--update-golden`, when `PACKAGED_JAVA` is set: `run_row packaged 1 "$PACKAGED_JAVA" <the three add-opens options> -cp "$PACKAGED_APP/*" net.sf.wubiq.clients.LocalPrintManager -h "$SERVER" -u witness-packaged -v && check_extents packaged`.
   8. Header comment: describe the three rows, the two new options and the extents rows in the words of `wubiq-client/README.md` § End to end, through wubiq-server; exit code 2 lists the new setup errors.
4. `wubiq-client/test-dotmatrix-e2e-windows.ps1`:
   1. Parameters: add `[string]$PackagedJava = ""` and `[string]$PackagedApp = ""`. Add `$LegacyJar = Join-Path $Base "..\wubiq-server\WebContent\wubiq-client.jar"` and `$Extents = Join-Path $Base "tests-src\net\sf\wubiq\dotmatrix\golden\test-page.extents"`.
   2. Setup checks, with `Fail-Setup`: exactly one of `-PackagedJava` and `-PackagedApp` given → `-PackagedJava and -PackagedApp go together.`; both given and `$PackagedJava` not a file or `$PackagedApp` not a directory → `No packaged client at $PackagedJava and $PackagedApp.`; `$LegacyJar` not a file → `No 2.7.4 jar at $LegacyJar.`
   3. `-CompileHome`: its default is the first run whose `Major` is 25; replace the message `A JDK 8 (with javac) is needed to compile for Java 6; pass -CompileHome.` with `A JDK 25 (with javac) is needed to compile the client; pass -CompileHome.`
   4. `Invoke-Print` takes the client's executable and its arguments instead of a JDK home: `Invoke-Print($javaExe, $javaArguments, $name, $out)`. It starts the client with `-FilePath $javaExe` and `-ArgumentList "$ClientProperties $javaArguments -h $Server -u $uuid -v"`. Its body is otherwise unchanged.
   5. Rows. Every run object gets the properties `Name`, `JavaExe`, `JavaArguments`, `Major` and `Extents`:
      - one per JDK of `-Jdks`: `Name = "legacy-java-" + ($version -replace "[._+]", "-")`, `JavaExe = "$jdkHome\bin\java.exe"`, `JavaArguments = "-jar " + (Quote $LegacyJar)`, `Extents = $false`. `$Reference` is, as today, the first of them whose `Major` is 6;
      - one for the source build: `Name = "source-java-" + (<version of $CompileHome> -replace "[._+]", "-")`, `JavaExe = "$CompileHome\bin\java.exe"`, `JavaArguments = "-jar " + (Quote $Launcher)`, `Extents = $true`;
      - when `-PackagedJava` is given: `Name = "packaged"`, `JavaExe = $PackagedJava`, `JavaArguments = "<the three add-opens options> -cp " + (Quote "$PackagedApp\*") + " net.sf.wubiq.clients.LocalPrintManager"`, `Extents = $true`.
      The main loop runs the reference first, then the other runs in the order above, and calls `Invoke-Print $run.JavaExe $run.JavaArguments $run.Name $out`.
   6. Extents row. After the driver job's `REFERENCE`, `SAME` or `DIFFERENT` line of a run whose `Extents` is `$true`: when `$Extents` is not a file, add the line `NAME-extents<TAB>NOT CHECKED<TAB>no $Extents`; otherwise run the witness as the `compare` call does, with the arguments `extents --expect "$Extents" "$escp"`, output to `$out\extents.txt`, and add `NAME-extents<TAB>EXTENTS OK<TAB>$out\extents.txt` on exit code 0, or `NAME-extents<TAB>EXTENTS DIFFERENT<TAB>` followed by the first `FAIL` line, adding 1 to `$Failed`.
   7. Header comment: describe the three rows, the two new parameters and the extents rows in the words of `wubiq-client/README.md` § End to end on Windows.

##### Green tests
1. `.claude/scripts/suite.sh` — full suite green (review-issue runs this)
2. `.claude/scripts/gate.sh` clean
<!-- section:CLI-BLD-3:end -->
<!-- section:CLI-BE-1:start -->
### CLI-BE-1 — Backend · wubiq-client · `TestPageExtents` test tool and the test page extents file
Depends on: CLI-DOC-1

#### Documentation references
`wubiq-client/README.md` § Dot-matrix tests › End to end, through wubiq-server (the `ROW-extents` rows and the extents file); `wubiq-dotmatrix-witness/README.md` § Command line (the format of the expected file).

#### Implementation details
Language level: Java 1.6 syntax and classes (the level `wubiq-client/build.xml` sets for `tests-src` until CLI-BLD-1 merges; this section does not depend on it). The tool lives in the test sources: `test.classpath` of `wubiq-client/build.xml` holds PDFBox 1.8.6 (`wubiq-common/lib-nondeploy/pdfbox-1.8.6.jar`, `fontbox-1.8.6.jar`); the client's own classpath does not.

##### Acceptance criteria
- [ ] AC-1: Given a one-page PDF (US Letter) with `Hello` drawn in Helvetica 12 at 72 points from the left and 720 points from the bottom, when `TestPageExtents#extract(InputStream)` reads it, then it returns one row whose tab-separated fields are sheet `0`, left `1.000`, a right edge between 1.37 and 1.39, baseline `1.000` and the text `Hello`
- [ ] AC-2: Given a two-page PDF with one text on each page, when `extract` reads it, then the first row's sheet is `0` and the second row's sheet is `1`
- [ ] AC-3: Given a stream that is not a PDF, when `extract` reads it, then it throws an `IOException` (error path: an unreadable page never yields an empty extents file)
- [ ] AC-4: the committed extents file holds the 44 text runs of the server's test page (mechanical — no red test; verified by `grep -c -v '^#' wubiq-client/tests-src/net/sf/wubiq/dotmatrix/golden/test-page.extents` printing `44` and `grep -c "$(printf '^0\t0.789\t6.502\t1.472\tFree your printing')" wubiq-client/tests-src/net/sf/wubiq/dotmatrix/golden/test-page.extents` printing `1`)
- [ ] AC-5: the suite runs the new test class (mechanical — no red test; verified by `grep -c 'net.sf.wubiq.dotmatrix.TestPageExtentsTest' wubiq-client/build.xml` printing `1`)

##### Test ports
- `net.sf.wubiq.dotmatrix.TestPageExtents#extract(java.io.InputStream)` · in: a PDF stream · out: `List<String>`, one row per text run: `sheet<TAB>left<TAB>right<TAB>baseline<TAB>text`, inches with three decimals, sheet from 0 · new (`grep -rIl TestPageExtents` over the repository returns nothing)

##### Red tests (write first; each must fail before implementation)
Every row: new file `wubiq-client/tests-src/net/sf/wubiq/dotmatrix/TestPageExtentsTest.java` (the class under test is new).

Shared setup, a private helper `pdf(String... texts)` returning `byte[]`: a `PDDocument`; for each text a `new PDPage()` added with `addPage`, and a `PDPageContentStream` on it with `beginText()`, `setFont(PDType1Font.HELVETICA, 12)`, `moveTextPositionByAmount(72, 720)`, `drawString(text)`, `endText()`, `close()`; then `save(ByteArrayOutputStream)` and `close()`. Classes: `org.apache.pdfbox.pdmodel.PDDocument`, `org.apache.pdfbox.pdmodel.PDPage`, `org.apache.pdfbox.pdmodel.edit.PDPageContentStream`, `org.apache.pdfbox.pdmodel.font.PDType1Font`.

| AC | Placement | Setup | Test file | Test name | Assertion sketch |
|---|---|---|---|---|---|
| AC-1 | new file | `TestPageExtents.extract(new ByteArrayInputStream(pdf("Hello")))`, first row split on `"\t"` | TestPageExtentsTest.java | `extractsTheExtentsOfATextRun` | `assertEquals(1, rows.size()); assertEquals("0", f[0]); assertEquals("1.000", f[1]); assertEquals(1.38, Double.parseDouble(f[2]), 0.01); assertEquals("1.000", f[3]); assertEquals("Hello", f[4])` |
| AC-2 | add to that file | `extract(new ByteArrayInputStream(pdf("One", "Two")))` | same | `sheetsAreNumberedFromZero` | `assertTrue(rows.get(0).startsWith("0\t")); assertTrue(rows.get(1).startsWith("1\t"))` |
| AC-3 | add to that file | `extract(new ByteArrayInputStream("not a pdf".getBytes("UTF-8")))` | same | `aStreamThatIsNotAPdfIsRefused` | `try { …; fail(); } catch (IOException expected) { }` |

##### Code changes
1. New file `wubiq-client/tests-src/net/sf/wubiq/dotmatrix/TestPageExtents.java`, `public class TestPageExtents`:
   - `public static List<String> extract(InputStream pdf) throws IOException`: `PDDocument.load(pdf)`; an anonymous subclass of `org.apache.pdfbox.util.PDFTextStripper` with an `int page = -1` field, that overrides `startPage(PDPage)` to add 1 to `page` and call `super.startPage`, and overrides `writeString(String text, List<TextPosition> positions)` to add one row: with `first = positions.get(0)` and `last = positions.get(positions.size() - 1)`, `String.format(Locale.US, "%d\t%.3f\t%.3f\t%.3f\t%s", page, first.getXDirAdj() / 72, (last.getXDirAdj() + last.getWidthDirAdj()) / 72, first.getYDirAdj() / 72, text)`. Call `setSortByPosition(true)` and `getText(document)` on it; close the document in `finally`; return the rows.
   - `public static void main(String[] args) throws IOException`: `args[0]` is the PDF, `args[1]` the file to write. Writes, as UTF-8, the line `# sheet<TAB>left<TAB>right<TAB>baseline<TAB>text (inches), from ` followed by the PDF's file name, then every row of `extract`, each ended by `\n`. With fewer than two arguments it prints `Usage: TestPageExtents PDF OUTPUT` to `System.err` and calls `System.exit(2)`.
2. `wubiq-client/build.xml` target `test-dotmatrix` — after the line `<arg value="net.sf.wubiq.dotmatrix.DotMatrixWitnessTest" />` add `<arg value="net.sf.wubiq.dotmatrix.TestPageExtentsTest" />`.
3. Write the extents file. From `wubiq-client/`, after `.claude/scripts/gate.sh` compiled the tests:
   ```
   java -Djava.awt.headless=true -cp "build-tests/classes:../wubiq-common/lib/*:../wubiq-common/lib-nondeploy/*" net.sf.wubiq.dotmatrix.TestPageExtents ../wubiq-server/src/net/sf/wubiq/reports/TestPage.pdf tests-src/net/sf/wubiq/dotmatrix/golden/test-page.extents
   ```
   Commit `wubiq-client/tests-src/net/sf/wubiq/dotmatrix/golden/test-page.extents` as written. `TestPage.pdf` is the page `RemotePrintServlet#printTestPageCommand` sends to a desktop client (`wubiq-server/src/net/sf/wubiq/servlets/RemotePrintServlet.java`).

##### Green tests
1. The red tests above pass, unmodified (code-issue runs these, targeted:
   `.claude/scripts/section-test.sh CLI net.sf.wubiq.dotmatrix.TestPageExtentsTest`)
2. `.claude/scripts/suite.sh` — full suite green (review-issue runs this; code-issue runs only
   this section's test classes)
3. `.claude/scripts/gate.sh` clean
<!-- section:CLI-BE-1:end -->
<!-- section:SRV-DOC-1:start -->
### SRV-DOC-1 — Documentation · wubiq-server
Depends on: CLI-DOC-1

#### Documentation references
`wubiq-server/README.md` § Running the server, § Distributing the application.

#### Implementation details

##### CHANGELOG.md
1. § `## Wubiq 2.8.0` — after the last bullet of `### Wubiq-client`, insert verbatim:
   ```
   ### Wubiq-server
   - Two client lanes on the same servlet. A client that asks for the version without naming its platform (2.7.4 and older) is answered `2.7.4` and keeps downloading the 2.7.4 `wubiq-client.jar`. A packaged client names its platform and package type, is answered the highest version among the packages of `client.packages.directory`, and downloads that package through the servlet.
   - New property `client.packages.directory` in `wubiq-server.properties`: the directory that holds the client packages.
   - The console lists the connected clients by version.
   - The build no longer builds wubiq-client and wubiq-installer; `WebContent/wubiq-client.jar` is the 2.7.4 jar and is not rebuilt.
   ```

##### wubiq-server/README.md
1. § Running the server — replace `You have to build the server to run it. The wubiq-common and the wubiq-client are dependencies.` with `You have to build the server to run it. wubiq-common is its only dependency.`
2. § Running the server — replace from the line `Now that you have the server, you'll need some clients to connect to it.` through the paragraph `All the printers will be exposed in the server.` with, verbatim:
   ````
   Now that you have the server, you'll need some clients to connect to it. Install the native package built by `../wubiq-installer`, or download the 2.7.4 client jar from the webpage and run it with an installed Java:

   ```shell
   % java -jar ./wubiq-client.jar -h http://localhost:8080 -u client-test
   ```

   All the printers will be exposed in the server.

   ### Client packages

   The server serves two kinds of clients:

   | Client | Asks | Is answered |
   |---|---|---|
   | 2.7.4 and older, started by the IzPack installation's launcher | the version, without naming its platform | `2.7.4`, and `wubiq-client.jar` from the webpage: the 2.7.4 jar, kept in `WebContent` |
   | packaged client (native package of `../wubiq-installer`) | the version, naming its platform (`windows-x64`, `macos-arm64`, `macos-x64`, `linux-x64`) and package type (`msi`, `pkg`, `deb`, `rpm`) | the highest version among the packages for that platform and type; the client downloads the package through the servlet when that version is higher than its own |

   To offer packages, set the directory that holds them in `wubiq-server.properties`:

   ```
   client.packages.directory=/var/lib/wubiq/packages
   ```

   Copy the packages there with the names the installer build gives them, `wubiq-client-VERSION-PLATFORM.TYPE`:

   ```
   wubiq-client-2.8.0-windows-x64.msi
   wubiq-client-2.8.0-macos-arm64.pkg
   wubiq-client-2.8.0-macos-x64.pkg
   wubiq-client-2.8.0-linux-x64.deb
   wubiq-client-2.8.0-linux-x64.rpm
   ```

   The version is read from the file name; a file with another name is ignored. Without the property, or with an empty directory, packaged clients are offered nothing and keep running.

   The console shows the connected clients by version (`Connected clients by version: 2.7.4 (3), 2.8.0 (5)`), so it is visible when no 2.7.x client connects any more.
   ````
3. § Distributing the application — after the paragraph `That will create all the distribution files in **wubiq-server/dist**.` add the paragraph: `The client packages are built by `../wubiq-installer`, on each operating system; `ant` here does not build them.`

##### Acceptance criteria
- [ ] AC-1: the CHANGELOG entry sits under `## Wubiq 2.8.0` › `### Wubiq-server` and
      carries no issue reference (mechanical — no red test; verified by
      `git diff origin/master...HEAD -- CHANGELOG.md | grep -E '^\+.*\(#[0-9]+\)'` returning nothing)
- [ ] AC-2: nothing but this application's documentation modified, apart from this issue's
      spec file (its AC ticks) (mechanical — no red test; verified by
      `git diff --name-only origin/master...HEAD | grep -vE '^CHANGELOG\.md$|^wubiq-server/.*\.md$|^specs/issue-52-enriched-spec\.md$'`
      returning nothing)
- [ ] AC-3: `wubiq-server/README.md` has § Client packages with the property and the file names (mechanical — no red test; verified by `grep -c -E '^### Client packages$|^client\.packages\.directory=|^wubiq-client-2\.8\.0-' wubiq-server/README.md` printing `7`)

##### Green checks
1. `.claude/scripts/gate.sh` clean (code-issue); `.claude/scripts/suite.sh` — full suite green
   (review-issue runs the suite)
<!-- section:SRV-DOC-1:end -->
<!-- section:SRV-BLD-1:start -->
### SRV-BLD-1 — Build · wubiq-server · no client or installer build, the served client jar is not overwritten
Depends on: SRV-DOC-1

#### Documentation references
`wubiq-server/README.md` § Running the server, § Distributing the application; `CHANGELOG.md` § `## Wubiq 2.8.0` › `### Wubiq-server`, fourth bullet.

#### Implementation details
Language level: `wubiq-server/build.xml` target `compileWar` sets no `source` and no `target`; this section does not change it.

`wubiq-server/WebContent/wubiq-client.jar` and `wubiq-server/WebContent/wubiq-setup.jar` are tracked files. This section edits neither: it only removes the build steps that overwrite them and the steps that build applications a JDK 8 cannot compile any more.

##### Acceptance criteria
- [ ] AC-1: no target of `wubiq-server/build.xml` runs the client's or the installer's build, and none copies a jar into `WebContent` (mechanical — no red test; verified by `grep -c -E 'antfile="\.\./wubiq-(client|installer)/build\.xml" inheritall="false" />' wubiq-server/build.xml` printing `0` and `grep -c 'todir="${basedir}/WebContent"' wubiq-server/build.xml` printing `1`, the copy of `wubiq-android.apk`)
- [ ] AC-2: target `distribuite-binaries` takes no file from `wubiq-client/dist` and `wubiq-installer/dist` (mechanical — no red test; verified by `grep -c -E '\.\./wubiq-(client|installer)/dist' wubiq-server/build.xml` printing `0`)
- [ ] AC-3: the build file no longer names the Android project's `libs` (mechanical — no red test; verified by `grep -c 'basedir.gradle' wubiq-server/build.xml` printing `0`)
- [ ] AC-4: the tracked jars under `WebContent` are untouched by this section (mechanical — no red test; verified by `git diff --name-only origin/master...HEAD -- wubiq-server/WebContent` returning nothing)
- [ ] AC-5: the build file is still well formed and the server still compiles (mechanical — no red test; verified by `ant -q -f wubiq-server/build.xml -projecthelp` listing the targets without an error, and `.claude/scripts/gate.sh` ending `EXIT: 0`)

##### Code changes
1. `wubiq-server/build.xml` — delete the line `<property name="basedir.gradle" value="${workspace.dir}/wubiq-android-gradle" />`.
2. Target `wubiq-server` — delete the line `<ant antfile="../wubiq-client/build.xml" inheritall="false" />`. Keep `<ant antfile="../wubiq-common/build.xml" inheritall="false" />` and the `antcall`.
3. Target `distribute`:
   1. delete the commented block `<!-- <ant antfile="../wubiq-common/build.xml" inheritall="false" /> … <ant antfile="../wubiq-client/build.xml" inheritall="false" /> -->` (two lines);
   2. in the `<input>` task replace the `message` attribute value with `Common has been built. The Android application is built in Android Studio: generate the signed application and its documentation there NOW. Press Y to continue with the release process. (Y/N)?`;
   3. delete the line `<ant antfile="../wubiq-installer/build.xml" inheritall="false" />`;
   4. delete the lines `<copy file="../wubiq-client/dist/wubiq-client.jar" todir="${basedir}/WebContent" />` and `<copy file="../wubiq-installer/dist/wubiq-setup.jar" todir="${basedir}/WebContent" />`.
4. Target `distribuite-binaries` — delete the line `<fileset dir="../wubiq-client/dist" includes="wubiq-client.jar" />` and the whole `<fileset dir="../wubiq-installer/dist"> … </fileset>` element (four lines).
5. Change nothing else: `create-all-javadoc-documentation` keeps calling target `create-javadoc-documentation` of the client and of the installer, which both keep it.

##### Contract changes
| Site | Change | New handling |
|---|---|---|
| `.github/workflows/dotmatrix-windows.yml` step `Build wubiq-server` (`ant -q -f wubiq-server\build.xml wubiq-server` with a JDK 8) | the target no longer builds the client | no edit: the step needs only the war |
| `wubiq-dotmatrix-witness/README.md` § Testing wubiq-client end to end › 2. Start wubiq-server | "also builds wubiq-common and wubiq-client" | rewritten by WIT-DOC-1 |
| `docker-compose.yml` | mounts `wubiq-common/dist/wubiq-common.jar` and `wubiq-server/dist/wubiq-server.war`, both still built by target `wubiq-server` | no edit |

##### Green tests
1. `.claude/scripts/suite.sh` — full suite green (review-issue runs this)
2. `.claude/scripts/gate.sh` clean
<!-- section:SRV-BLD-1:end -->
<!-- section:SRV-BE-1:start -->
### SRV-BE-1 — Backend · wubiq-server · `READ_VERSION` lanes and package streaming
Depends on: SRV-DOC-1, COM-BE-2

#### Documentation references
`wubiq-server/README.md` § Client packages.

#### Implementation details
Language level: Java 8 — `wubiq-server/build.xml` target `compileWar` sets no `source` and no `target`, and the registry compiles `SRV` with a JDK 8. Servlet API 3.0 (`wubiq-server/lib-nondeploy/servlet-api.jar`, Tomcat 7): `HttpServletResponse#setContentLengthLong` does not exist; the length is set as a header.

`SRV` has no suite: every AC is mechanical or manual.

##### Acceptance criteria
- [ ] AC-1: `READ_VERSION` without `cpf` answers the constant `2.7.4`, not `Labels.VERSION` (mechanical — no red test; verified by `grep -c 'respond(LEGACY_CLIENT_VERSION, response)' wubiq-server/src/net/sf/wubiq/servlets/RemotePrintServlet.java` printing `1` and `grep -c 'respond(Labels.VERSION, response)' wubiq-server/src/net/sf/wubiq/servlets/RemotePrintServlet.java` printing `0`)
- [ ] AC-2: the servlet handles `CommandKeys.READ_PACKAGE` and reads the directory through `ServerProperties#getClientPackagesDirectory()` and `ClientPackages#latest(File, String, String)` (mechanical — no red test; verified by `grep -c -E 'CommandKeys\.READ_PACKAGE|getClientPackagesDirectory\(\)|ClientPackages\.latest\(' wubiq-server/src/net/sf/wubiq/servlets/RemotePrintServlet.java` printing at least `3`, and `.claude/scripts/gate.sh` ending `EXIT: 0`)
- [ ] AC-3: Given a running server whose `client.packages.directory` holds `wubiq-client-2.8.0-linux-x64.deb` and `wubiq-client-2.10.0-linux-x64.deb`, when a client posts `command=v1`, then the answer is `2.7.4`; when it posts `command=v1&cpf=linux-x64&cpt=deb`, the answer is `2.10.0`; when it posts `command=v1&cpf=macos-arm64&cpt=pkg`, the answer is empty (manual — no red test; verified by `curl -s -d 'command=v1' http://localhost:8080/wubiq-server/wubiq.do` and the two other bodies, against the server of `wubiq-server/README.md` § Running the server with the property set)
- [ ] AC-4: Given the same server, when a client posts `command=v2&cpf=linux-x64&cpt=deb`, then it receives the bytes of the `2.10.0` file with `Content-Type: application/octet-stream` and a `Content-Length` equal to the file size (manual — no red test; verified by `curl -s -D headers.txt -o got.deb -d 'command=v2&cpf=linux-x64&cpt=deb' http://localhost:8080/wubiq-server/wubiq.do && cmp got.deb <the directory>/wubiq-client-2.10.0-linux-x64.deb`)
- [ ] AC-5: Given no `client.packages.directory`, a directory that does not exist, or no package for the platform and type, when a client posts `command=v2&cpf=linux-x64&cpt=rpm`, then the server answers HTTP 404 and logs nothing at error level; a `cpf` or `cpt` value such as `../../etc` selects no file, because the values are only compared with parsed file names (error paths; manual — no red test; verified by `curl -s -o /dev/null -w '%{http_code}' -d 'command=v2&cpf=linux-x64&cpt=rpm' http://localhost:8080/wubiq-server/wubiq.do` printing `404`, and the same with `cpf=../../etc`)
- [ ] AC-6: a 2.7.4 installation keeps printing against this server and is offered nothing but the 2.7.4 jar (manual — no red test; verified by the `legacy-java-VERSION` rows of `wubiq-client/test-dotmatrix-e2e.sh` reading `SAME` against a server built from this branch)

##### Code changes
1. Precondition, checked before any edit: `unzip -p wubiq-server/WebContent/wubiq-client.jar META-INF/MANIFEST.MF | grep -c '^Add-Opens:'` prints `1`. When it prints `0`, the tracked jar is not the 2.7.4 jar of PR H0 ("Out of Scope"): change nothing and end with `STATUS: BLOCKED — hand-pr-h0-not-merged`.
2. `wubiq-server/src/net/sf/wubiq/servlets/RemotePrintServlet.java` — add the imports `java.io.File`, `java.io.FileInputStream` and `net.sf.wubiq.utils.ClientPackages`, and delete the import `net.sf.wubiq.utils.Labels`: step 4 removes its only use in this class. (`net.sf.wubiq.utils.ServerProperties`, `IOUtils` and `Is` are imported already.)
3. Same class — add, below `serialVersionUID`:
   ```java
   /**
    * Version answered to every client that does not name its platform: the version of
    * WebContent/wubiq-client.jar, the last jar those clients are served. It never changes.
    */
   static final String LEGACY_CLIENT_VERSION = "2.7.4";
   ```
4. `RemotePrintServlet#doGet(HttpServletRequest, HttpServletResponse)` — replace
   ```java
   if (CommandKeys.READ_VERSION.equalsIgnoreCase(command)) {
   	respond(Labels.VERSION, response);
   } else if (CommandKeys.CONNECTION_TEST.equalsIgnoreCase(command)) {
   ```
   with
   ```java
   if (CommandKeys.READ_VERSION.equalsIgnoreCase(command)) {
   	readVersionCommand(request, response, parameters);
   } else if (CommandKeys.READ_PACKAGE.equalsIgnoreCase(command)) {
   	readPackageCommand(uuid, request, response, parameters);
   } else if (CommandKeys.CONNECTION_TEST.equalsIgnoreCase(command)) {
   ```
5. Same class — add three private methods, after `RemotePrintServlet#serverTimestampCommand(String, HttpServletRequest, HttpServletResponse)`:
   - `private File clientPackage(HttpServletRequest request, Map<String, Object> parameters)`: reads `directory = ServerProperties.INSTANCE.getClientPackagesDirectory()`; returns `null` when `Is.emptyString(directory)`; otherwise returns `ClientPackages.latest(new File(directory), getParameter(request, parameters, ParameterKeys.CLIENT_PLATFORM), getParameter(request, parameters, ParameterKeys.CLIENT_PACKAGE_TYPE))`.
   - `private void readVersionCommand(HttpServletRequest request, HttpServletResponse response, Map<String, Object> parameters)`: when `Is.emptyString(getParameter(request, parameters, ParameterKeys.CLIENT_PLATFORM))`, `respond(LEGACY_CLIENT_VERSION, response)`; otherwise, with `File offered = clientPackage(request, parameters)`, `respond(offered == null ? "" : ClientPackages.parse(offered.getName())[0], response)`.
   - `private void readPackageCommand(String uuid, HttpServletRequest request, HttpServletResponse response, Map<String, Object> parameters) throws IOException`: with `File offered = clientPackage(request, parameters)`: when `offered == null`, `response.sendError(HttpServletResponse.SC_NOT_FOUND)` and return. Otherwise `response.setContentType("application/octet-stream")`, `response.setHeader("Content-Length", Long.toString(offered.length()))`, `response.setHeader("Content-Disposition", "attachment;filename=" + offered.getName())`, `LOG.info("Client package " + offered.getName() + " requested by " + uuid + " (" + request.getRemoteAddr() + ")")`, then `IOUtils.INSTANCE.copy(new FileInputStream(offered), response.getOutputStream())` (it copies in 64 KB blocks and closes both streams; the file is never read whole into memory).
6. `RemotePrintServlet#registerComputerNameCommand(String, HttpServletRequest, HttpServletResponse, Map)` — before `respond("ok", response);` add `LOG.info("Client registered: " + uuid + ", version " + client.getClientVersion());`.
7. Do not use `RemotePrintServlet#respond(String, InputStream, HttpServletResponse)` for the package: it buffers the whole stream in memory.

##### Contract changes
| Site | Change | New handling |
|---|---|---|
| `wubiq-installer/src/net/sf/wubiq/clients/RunningClient.java` `RunningClient#serverVersion()` in the released 2.7.4 launcher — sends `v1` with no `cpf` | answered `2.7.4` whatever `Labels.VERSION` is | no edit: it compares for equality with its installed 2.7.4 jar and downloads nothing; with an older jar it downloads `/wubiq-client.jar`, the 2.7.4 jar |
| Packaged client (INS-BE-2, INS-UI-1) — sends `v1` with `cv`, `cpf`, `cpt` | answered the highest package version, or an empty body | it updates only when the answer is a higher version |
| `wubiq-android-gradle` app | sends neither `v1` nor `v2` (`grep -rn 'READ_VERSION' wubiq-android-gradle/app/src` returns nothing) | none |
| `wubiq-server/WebContent/index.jsp` — reads `Labels.VERSION` for the page title | unchanged | none |

##### Green tests
1. `.claude/scripts/suite.sh` — full suite green (review-issue runs this)
2. `.claude/scripts/gate.sh` clean
<!-- section:SRV-BE-1:end -->
<!-- section:SRV-UI-1:start -->
### SRV-UI-1 — UI · wubiq-server · connected clients by version
Depends on: SRV-DOC-1, COM-UI-1

#### Documentation references
`wubiq-server/README.md` § Client packages, last paragraph.

#### Implementation details
Language level: Java 8, as stated in SRV-BE-1. `SRV` has no suite: every AC is mechanical or manual.

The console already shows each client's version next to its uuid (`wubiq-server/WebContent/index.jsp`, `<%=uuid%><%=clientVersion%>`); the version comes from `cv` at registration (`RemotePrintServlet#registerComputerNameCommand`, `RemoteClient#getClientVersion()`). This section adds one summary line.

##### Acceptance criteria
- [ ] AC-1: `RemoteClientManager` counts the active clients by version (mechanical — no red test; verified by `grep -c -E 'public SortedMap<String, Integer> clientVersionCounts\(\)|public String clientVersionSummary\(\)' wubiq-server/src/net/sf/wubiq/remote/RemoteClientManager.java` printing `2` and `.claude/scripts/gate.sh` ending `EXIT: 0`)
- [ ] AC-2: the console prints the summary through the label `server.clients_by_version`, and prints no line when no client is connected (mechanical — no red test; verified by `grep -c -E 'clientVersionSummary\(\)|server\.clients_by_version' wubiq-server/WebContent/index.jsp` printing `2`)
- [ ] AC-3: Given two connected clients that registered with `cv=2.7.4` and one with `cv=2.8.0`, when the console is opened, then its header shows `Connected clients by version: 2.7.4 (2), 2.8.0 (1)`; given a client that is disconnected or killed, it is not counted; given a client that sent no version, it is counted under `< 2.0`, the value the servlet stores for it (manual — no red test; verified on the server of `wubiq-server/README.md` § Running the server with clients started as that section shows)

##### Code changes
1. `wubiq-server/src/net/sf/wubiq/remote/RemoteClientManager.java` — add the imports `java.util.SortedMap`, `java.util.TreeMap` and `net.sf.wubiq.utils.Is`; add, after `RemoteClientManager#getUuids()`:
   - `public SortedMap<String, Integer> clientVersionCounts()`: a `TreeMap<String, Integer>`; for every `uuid` of `getUuids()`: `RemoteClient client = getRemoteClient(uuid, true)`; skip it when `client == null`, when `Boolean.TRUE.equals(client.isKilled())`, or when `!isRemoteActive(uuid)`; the key is `client.getClientVersion()`, or `"?"` when `Is.emptyString` of it; add 1 to the key's count.
   - `public String clientVersionSummary()`: joins the entries of `clientVersionCounts()` as `version (count)` with `", "`; `""` for an empty map.
2. `wubiq-server/WebContent/index.jsp` — in the header table (`<table class="wubiq_header <%=ready%>">`), directly after the `</tr>` that closes the row of the four download buttons (the `</tr>` that follows `<td>&nbsp;</td>`), insert:
   ```jsp
   <%
   String clientsByVersion = RemoteClientManager.getRemoteClientManager(request).clientVersionSummary();
   if (!Is.emptyString(clientsByVersion)) {
   %>
   			<tr>
   				<td align="center" colspan="5"><%=ServerLabels.get("server.clients_by_version", clientsByVersion)%></td>
   			</tr>
   <%
   }
   %>
   ```
   `RemoteClientManager`, `Is` and `ServerLabels` are imported by the page already.

##### Green tests
1. `.claude/scripts/suite.sh` — full suite green (review-issue runs this)
2. `.claude/scripts/gate.sh` clean
<!-- section:SRV-UI-1:end -->
<!-- section:INS-DOC-1:start -->
### INS-DOC-1 — Documentation · wubiq-installer
Depends on: SRV-DOC-1

#### Documentation references
`wubiq-installer/README.md` (today the single line `## wubiq-installer`).

#### Implementation details

##### CHANGELOG.md
1. § `## Wubiq 2.8.0` — after the last bullet of `### Wubiq-server`, insert verbatim:
   ```
   ### Wubiq-installer
   - The client is installed from a native package per operating system: MSI for 64-bit Windows 10 or later, PKG for macOS, DEB and RPM for Linux. Each package carries its own Java 25 runtime: no Java has to be installed. IzPack and `wubiq-setup.jar` are no longer built.
   - The package registers the client as a service that starts at boot (Windows service `wubiq-client`, launchd daemon `net.sf.wubiq.client-Wubiq`, systemd unit `wubiq-Wubiq`) and adds the configurator to the applications menu.
   - The service updates itself: when wubiq-server offers a package with a higher version, it downloads it and runs the operating system's installer unattended.
   - The configuration is kept outside the installation folder and through updates. An IzPack installation is taken over at the first start: its `wubiq-installer.properties` is imported and its service removed.
   - The packages are not signed.
   ```

##### wubiq-installer/README.md
1. Replace the whole file with, verbatim:
   ````
   ## wubiq-installer

   Builds the native packages of the Wubiq print client and holds the two programs they install: the service launcher (`WubiqLauncher`) and the configurator (`WubiqConfigurator`). A package carries wubiq-client, wubiq-common and its own Java 25 runtime; nothing in installing, configuring or running the client needs an installed Java.

   | Operating system | Package | Installs into |
   |---|---|---|
   | Windows 10 or later, 64-bit | `wubiq-client-VERSION-windows-x64.msi` | `C:\Program Files\Wubiq` |
   | macOS, Apple silicon | `wubiq-client-VERSION-macos-arm64.pkg` | `/Applications/Wubiq.app` |
   | macOS, Intel | `wubiq-client-VERSION-macos-x64.pkg` | `/Applications/Wubiq.app` |
   | Linux x64, Debian family | `wubiq-client-VERSION-linux-x64.deb` | `/opt/wubiq` |
   | Linux x64, Red Hat family | `wubiq-client-VERSION-linux-x64.rpm` | `/opt/wubiq` |

   ### Building

   A package is built on the operating system it is for. It needs:
   - a JDK 25 as `JAVA_HOME` (its `jdeps`, `jlink` and `jpackage` are used);
   - Ant;
   - on Windows, the WiX Toolset 3 on the `PATH`;
   - on Linux, `dpkg-deb` and `fakeroot` for the DEB and `rpmbuild` for the RPM.

   ```
   ant package
   ```

   compiles wubiq-common, wubiq-client and the installer, builds the runtime with `jlink` from the modules `jdeps` lists, and writes the package of the current operating system (both of them on Linux) into `dist`, named as in the table above. The version is the client's version.

   The packages are not signed.

   ### Installing

   | Operating system | Steps |
   |---|---|
   | Windows | Open the MSI. SmartScreen warns that the publisher is unknown: choose **More info**, then **Run anyway**. |
   | macOS | Open the PKG. Gatekeeper refuses it: open **System Settings › Privacy & Security** and choose **Open Anyway**. |
   | Debian, Ubuntu | `sudo dpkg -i wubiq-client-VERSION-linux-x64.deb` |
   | Fedora, RHEL | `sudo rpm -U wubiq-client-VERSION-linux-x64.rpm` |

   The package registers the client as a service that starts at boot, and starts it:

   | Operating system | Service | Stop and start |
   |---|---|---|
   | Windows | `wubiq-client` (shown as Wubiq) | `net stop wubiq-client`, `net start wubiq-client` |
   | macOS | launchd daemon `net.sf.wubiq.client-Wubiq` | `sudo launchctl unload /Library/LaunchDaemons/net.sf.wubiq.client-Wubiq.plist`, `sudo launchctl load /Library/LaunchDaemons/net.sf.wubiq.client-Wubiq.plist` |
   | Linux | systemd unit `wubiq-Wubiq` | `sudo systemctl stop wubiq-Wubiq`, `sudo systemctl start wubiq-Wubiq` |

   ### Configuring

   The service reads `wubiq-installer.properties` from the configuration directory. Only an administrator can write there: the file holds the options of a program that runs as the system.

   | Operating system | Configuration directory | Log |
   |---|---|---|
   | Windows | `C:\ProgramData\Wubiq` | `C:\ProgramData\Wubiq\logs` |
   | macOS | `/Library/Application Support/Wubiq` | `/var/log/wubiq.log` |
   | Linux | `/etc/wubiq` | `journalctl -u wubiq-Wubiq` |

   Start the configurator as an administrator, set the server addresses, the unique id and the groups, and save. Saving restarts the client.

   | Operating system | Configurator |
   |---|---|
   | Windows | Start menu › **WubiqConfigurator**, with right click › **Run as administrator** |
   | macOS | `sudo /Applications/Wubiq.app/Contents/MacOS/WubiqConfigurator` |
   | Linux | `sudo /opt/wubiq/bin/WubiqConfigurator` |

   Until a server address is saved, the service runs and waits.

   ### Updating

   The service asks its server for the client version on offer for its platform and package type. When that version is higher than its own, it downloads the package from the server and runs the operating system's installer unattended (`msiexec /i FILE /qn /norestart`, `installer -pkg FILE -target /`, `dpkg -i FILE`, `rpm -U FILE`). The installer stops the service, replaces the files and starts the service again; the configuration directory is kept. An equal or lower version is never installed. The configurator's title shows when a higher version is on offer.

   The output of the last update is in `wubiq-client-update.log`, in the temporary directory of the system account (`C:\Windows\Temp`, `/tmp`). When an update fails, the service keeps running its version and tries again after its next start.

   The server offers the packages of its `client.packages.directory` (see `../wubiq-server/README.md`).

   ### Upgrading from an IzPack installation

   Install the package over the old installation. At its first start the service:
   1. finds the old installation (the Windows service `wubiq`, or `/etc/init.d/wubiq`);
   2. copies its `wubiq-installer.properties` into the configuration directory, unless one is there already;
   3. stops the old client and removes its service.

   The old installation's folder is not deleted; remove it once the new client prints.

   ### Uninstalling

   | Operating system | Steps |
   |---|---|
   | Windows | **Settings › Apps**, Wubiq, **Uninstall** |
   | macOS | `sudo launchctl unload /Library/LaunchDaemons/net.sf.wubiq.client-Wubiq.plist`, then delete that file and `/Applications/Wubiq.app` |
   | Debian, Ubuntu | `sudo dpkg -r wubiq` |
   | Fedora, RHEL | `sudo rpm -e wubiq` |

   The configuration directory is left in place.
   ````

##### Acceptance criteria
- [ ] AC-1: the CHANGELOG entry sits under `## Wubiq 2.8.0` › `### Wubiq-installer` and
      carries no issue reference (mechanical — no red test; verified by
      `git diff origin/master...HEAD -- CHANGELOG.md | grep -E '^\+.*\(#[0-9]+\)'` returning nothing)
- [ ] AC-2: nothing but this application's documentation modified, apart from this issue's
      spec file (its AC ticks) (mechanical — no red test; verified by
      `git diff --name-only origin/master...HEAD | grep -vE '^CHANGELOG\.md$|^wubiq-installer/.*\.md$|^specs/issue-52-enriched-spec\.md$'`
      returning nothing)
- [ ] AC-3: `wubiq-installer/README.md` has its six sections and the unsigned install steps (mechanical — no red test; verified by `grep -c -E '^### (Building|Installing|Configuring|Updating|Upgrading from an IzPack installation|Uninstalling)$' wubiq-installer/README.md` printing `6` and `grep -c -E 'Run anyway|Open Anyway' wubiq-installer/README.md` printing `2`)

##### Green checks
1. `.claude/scripts/gate.sh` clean (code-issue); `.claude/scripts/suite.sh` — full suite green
   (review-issue runs the suite)
<!-- section:INS-DOC-1:end -->
<!-- section:INS-BLD-1:start -->
### INS-BLD-1 — Build · wubiq-installer · IzPack removed, `build.xml` compiles at Java 25
Depends on: INS-DOC-1, CLI-BLD-2

#### Documentation references
`wubiq-installer/README.md` § Building; `CHANGELOG.md` § `## Wubiq 2.8.0` › `### Wubiq-installer`, first bullet.

#### Implementation details
Language level: `source="1.6" target="1.6"` in the one `javac` task of `wubiq-installer/build.xml` (target `compile`). This section replaces the build file; its `compile` target compiles with `release="25"`. The registry row `INS` (`.claude/scripts/apps.tsv`) compiles the sources with its own `javac` flags, `--release 25` once PR H1 is merged.

`INS` has no suite: every AC is mechanical.

##### Acceptance criteria
- [ ] AC-1: the IzPack descriptor, compiler, panel texts, service scripts and the 32-bit service wrapper are gone; three files stay under `installation` (mechanical — no red test; verified by `git ls-files wubiq-installer/installation` printing exactly `wubiq-installer/installation/resources/prunsrv-amd64.exe`, `wubiq-installer/installation/resources/wubiq.ico` and `wubiq-installer/installation/resources/wubiq.png`)
- [ ] AC-2: nothing under `wubiq-installer` but its README names IzPack or the removed class any more (mechanical — no red test; verified by `git grep -n -i -E 'izpack|InstallerSetupUtils|make_install|wubiq-setup' -- wubiq-installer ':!wubiq-installer/README.md'` returning nothing; the README names IzPack on purpose)
- [ ] AC-3: target `compile` compiles the installer at Java 25 against the compiled common and client classes, without building a jar (mechanical — no red test; verified, with a JDK 25 as `JAVA_HOME`, by `ant -q -f wubiq-installer/build.xml compile` ending without error and `javap -v -cp wubiq-installer/bin net.sf.wubiq.clients.WubiqLauncher | grep 'major version'` printing `major version: 69`)
- [ ] AC-4: the registry still compiles `INS` and everything else (mechanical — no red test; verified by `.claude/scripts/gate.sh` ending `EXIT: 0`)

##### Code changes
1. Precondition, checked before any edit: `awk -F'\t' '$1=="INS"{print $4}' .claude/scripts/apps.tsv` prints `25`. When it prints `8`, PR H1 ("Out of Scope") is not merged: change nothing and end with `STATUS: BLOCKED — hand-pr-h1-not-merged`.
2. Delete, with `git rm`:
   - `wubiq-installer/installation/install.xml`
   - `wubiq-installer/installation/IzPack/` (two tracked files)
   - `wubiq-installer/installation/i18n/` (ten tracked files)
   - `wubiq-installer/installation/wubiq-installer.properties`
   - under `wubiq-installer/installation/resources/`: `install_service.bat`, `install_service.sh`, `install_wubiq.bat`, `install_wubiq.sh`, `post_install.bat`, `post_install.sh`, `remove_service.bat`, `remove_service.sh`, `wubiq`, `prunsrv-i386.exe`, `start.bmp`, `start.ico`, `start.png`, `stop.bmp`, `stop.ico`, `stop.png`, `wubiq.bmp`, `wubiq_uninstall.bmp`, `wubiq_uninstall.ico`, `wubiq_uninstall.png`
   - `wubiq-installer/manifest.mf`
   - `wubiq-installer/readme.txt`
   - `wubiq-installer/src/net/sf/wubiq/utils/InstallerSetupUtils.java`
3. `wubiq-installer/.gitignore` — delete the lines `/izpack-settings.properties`, `/installation/build/` and `/wubiq-setup.properties`.
4. Replace `wubiq-installer/build.xml` with, verbatim (tabs for indentation):
   ```xml
   <project name="wubiq-installer" basedir="." default="compile">
   	<property name="bin" value="${basedir}/bin" />
   	<property name="lib" value="${basedir}/lib" />
   	<property name="common.dir" value="${basedir}/../wubiq-common" />
   	<property name="client.dir" value="${basedir}/../wubiq-client" />

   	<path id="project.classpath">
   		<fileset dir="${lib}" includes="*.jar" />
   		<pathelement path="${common.dir}/bin" />
   		<fileset dir="${common.dir}/lib" includes="*.jar" />
   		<fileset dir="${common.dir}/lib-nondeploy" includes="*.jar" />
   		<pathelement path="${client.dir}/bin" />
   	</path>

   	<target name="compile" description="Compiles wubiq-common, wubiq-client and the installer sources without packaging">
   		<ant dir="${client.dir}" target="compile" inheritAll="false" />
   		<delete dir="${bin}" failonerror="false" />
   		<mkdir dir="${bin}" />
   		<javac srcdir="${basedir}/src" destdir="${bin}" nowarn="true" failonerror="true" includeantruntime="false"
   				debug="true" release="25" encoding="UTF-8">
   			<classpath refid="project.classpath" />
   		</javac>
   		<copy todir="${bin}">
   			<fileset dir="${basedir}/src">
   				<include name="**/*.png" />
   				<include name="**/*.properties" />
   			</fileset>
   		</copy>
   	</target>

   	<target name="create-javadoc-documentation" description="Creates javadoc documentation">
   		<echo message="Creating wubiq-installer javadoc" />
   		<javadoc destdir="${basedir}/javadoc" sourcepath="${basedir}/src" packagenames="net.sf.wubiq.*"
   				classpathref="project.classpath" failonerror="false" use="true" windowtitle="Wubiq Installer Javadoc"
   				doctitle="Wubiq Installer API Documentation" />
   	</target>
   </project>
   ```
   `compile` of `wubiq-client/build.xml` (existing target) compiles `wubiq-common/src` and the client into their `bin` directories.

##### Contract changes
Every reference to what this section removes, found with `grep -rIn -i -E 'izpack|InstallerSetupUtils|make_install|wubiq-setup|install\.xml|prunsrv-i386|manifest\.mf'` over the repository:

| Site | Refers to | New handling |
|---|---|---|
| `wubiq-installer/build.xml` (targets `clean`, `distribute`, `sign-setup`, `make_install`, the `IzPack` task definition) | IzPack build | removed by step 4 |
| `wubiq-installer/.gitignore` | `izpack-settings.properties`, `installation/build`, `wubiq-setup.properties` | lines removed by step 3 |
| `wubiq-installer/src/net/sf/wubiq/utils/InstallerSetupUtils.java`, `wubiq-installer/manifest.mf` | generate an IzPack setup | removed by step 2; no other class calls `InstallerSetupUtils` |
| `wubiq-server/build.xml` targets `distribute`, `distribuite-binaries` | `../wubiq-installer/build.xml` default target, `wubiq-setup.jar`, `wubiq-installer.jar` | removed by SRV-BLD-1; until it merges, `distribute` runs this file's `compile` target, which builds nothing it needs |
| `wubiq-server/WebContent/index.jsp` (the `wubiq-setup.jar` button), `wubiq-server/WebContent/WEB-INF/web.xml` and `wubiq-server/src/net/sf/wubiq/servlets/WubiqSetupServlet.java` (`wubiq-setup.do`) | the tracked `wubiq-server/WebContent/wubiq-setup.jar` | no edit: the tracked jar stays and is still served ("Out of Scope") |
| `.vscode/launch-example.json` (entry `InstallerSetupUtils`), `.claude/skills/code-issue/SKILL.md` (names `make_install`) | removed class and target | outside every application directory: PR H3 ("Out of Scope") |
| `wubiq-installer/build.gradle` | `lib/uispec4j.jar`, the `dist` jars of client and common | no edit ("Out of Scope") |

##### Green tests
1. `.claude/scripts/suite.sh` — full suite green (review-issue runs this)
2. `.claude/scripts/gate.sh` clean
<!-- section:INS-BLD-1:end -->
<!-- section:INS-BLD-2:start -->
### INS-BLD-2 — Build · wubiq-installer · native packages with jlink and jpackage
Depends on: INS-DOC-1, INS-BLD-1, COM-BE-2

#### Documentation references
`wubiq-installer/README.md` § wubiq-installer (the package table), § Building, § Installing (the service table).

#### Implementation details
Language level: `release="25"` in target `compile` of `wubiq-installer/build.xml` (INS-BLD-1). This section adds build targets and package resources; it changes no Java source.

`INS` has no suite: every AC is mechanical or manual. `jpackage` builds only for the operating system it runs on.

Fixed names:

| Name | Value | Source |
|---|---|---|
| Application and launcher name | `Wubiq` | this section |
| Second launcher | `WubiqConfigurator`, main class `net.sf.wubiq.clients.WubiqConfigurator` (existing) | this section |
| Main class | `net.sf.wubiq.clients.WubiqLauncher` (existing), default argument `start` | `WubiqLauncher#main(String[])` |
| Linux package name | `wubiq`; systemd unit file `wubiq-Wubiq.service` | `LinuxLaunchersAsServices.getServiceUnitFileName` (JDK 25): `<package name>-<launcher name>.service` |
| macOS bundle identifier | `net.sf.wubiq.client`; launchd file `net.sf.wubiq.client-Wubiq.plist` | `MacLaunchersAsServices.getServicePListFileName` (JDK 25): `<bundle identifier>-<launcher name>.plist` |
| Windows service | `wubiq-client`, registered by the tracked `prunsrv-amd64.exe` | this section |
| MSI upgrade code | `E1687847-85F4-415F-8395-E4B6674F9FCC` | generated for this spec; never changes |
| Package file | `ClientPackages#fileName(String, String, String)` of `Labels.VERSION`, the build platform and the type (COM-BE-2) | printed by `ClientPackages#main(String[])` |
| System property of a packaged client | `PropertyKeys.WUBIQ_PACKAGE_TYPE` = `wubiq.package.type`, value `msi`, `pkg`, `deb` or `rpm` (COM-BE-2) | set by every launcher and by the Windows service |

##### Acceptance criteria
- [ ] AC-1: `wubiq-installer/build.xml` has the targets `stage`, `package`, `package-windows`, `package-macos` and `package-linux`, and still has `compile` and `create-javadoc-documentation` (mechanical — no red test; verified by `ant -q -f wubiq-installer/build.xml -projecthelp` listing `compile`, `create-javadoc-documentation`, `package` and `stage`, and `grep -c -E '<target name="(stage|package|package-windows|package-macos|package-linux)"' wubiq-installer/build.xml` printing `5`)
- [ ] AC-2: the service wrapper, the icons and the package resources are tracked where the targets read them, and `installation` is gone (mechanical — no red test; verified by `git ls-files wubiq-installer/package` printing exactly the nine paths of Code changes steps 1 to 6, and `git ls-files wubiq-installer/installation` printing nothing)
- [ ] AC-3: on a Mac with a JDK 25 as `JAVA_HOME`, `ant -f wubiq-installer/build.xml package` writes one PKG named by the convention, and the PKG carries the launchd override (mechanical — no red test; verified by `ant -f wubiq-installer/build.xml package > /tmp/wubiq-package.log 2>&1; echo $?` printing `0`, `ls wubiq-installer/dist | grep -c -E '^wubiq-client-[0-9.]+-macos-(arm64|x64)\.pkg$'` printing `1`, and `rm -rf /tmp/wubiq-pkg && pkgutil --expand-full wubiq-installer/dist/wubiq-client-*.pkg /tmp/wubiq-pkg && grep -rl AbandonProcessGroup /tmp/wubiq-pkg | wc -l` printing at least `1`)
- [ ] AC-4: the staged runtime keeps the `java` command, which the launcher starts the client with (mechanical — no red test; verified, after AC-3, by `wubiq-installer/build/package/runtime/bin/java -version` printing a version 25)
- [ ] AC-5: Given a `JAVA_HOME` older than 25, when `ant -f wubiq-installer/build.xml package` runs, then it stops at `compile` with a `javac` error and writes no package (error path: a wrong JDK fails loudly; mechanical — no red test; verified with a JDK 17 as `JAVA_HOME` by the command ending `BUILD FAILED` and `ls wubiq-installer/dist 2>/dev/null | wc -l` printing `0` after `rm -rf wubiq-installer/dist`)
- [ ] AC-6: on Windows (WiX 3 on the `PATH`) the target writes `wubiq-client-VERSION-windows-x64.msi`; on Linux it writes `wubiq-client-VERSION-linux-x64.deb` and `wubiq-client-VERSION-linux-x64.rpm` (manual — no red test; verified by running `ant -f wubiq-installer/build.xml package` on a Windows machine and on a Linux machine with `rpmbuild`, `dpkg-deb` and `fakeroot`, and listing `wubiq-installer/dist`)

##### Code changes
1. Move, with `git mv`:
   - `wubiq-installer/installation/resources/prunsrv-amd64.exe` → `wubiq-installer/package/windows/prunsrv-amd64.exe`
   - `wubiq-installer/installation/resources/wubiq.ico` → `wubiq-installer/package/resources/windows/Wubiq.ico`
   - `wubiq-installer/installation/resources/wubiq.png` → `wubiq-installer/package/resources/linux/Wubiq.png`
2. Copy `wubiq-installer/package/resources/windows/Wubiq.ico` to `wubiq-installer/package/resources/windows/WubiqConfigurator.ico` and `wubiq-installer/package/resources/linux/Wubiq.png` to `wubiq-installer/package/resources/linux/WubiqConfigurator.png` (`jpackage` takes `<launcher name>.ico` and `<launcher name>.png` from the resource directory).
3. New file `wubiq-installer/package/windows/wubiq-service.bat`, CRLF line ends, verbatim:
   ```bat
   @echo off
   rem Registers or removes the Wubiq client service. The MSI runs it as SYSTEM.
   setlocal enabledelayedexpansion
   set "APP=%~dp0"
   for %%I in ("%APP%..") do set "ROOT=%%~fI"
   set "DATA=%ProgramData%\Wubiq"
   set "PRUNSRV=%APP%prunsrv-amd64.exe"

   if /i "%~1"=="install" goto install
   if /i "%~1"=="remove" goto remove
   echo Usage: wubiq-service.bat install^|remove
   exit /b 2

   :install
   if not exist "%DATA%\logs" mkdir "%DATA%\logs"
   icacls "%DATA%" /inheritance:r /grant:r "*S-1-5-18:(OI)(CI)F" "*S-1-5-32-544:(OI)(CI)F" "*S-1-5-32-545:(OI)(CI)RX" >nul
   set "CP="
   for %%J in ("%APP%*.jar") do set "CP=!CP!%%~fJ;"
   net stop wubiq-client >nul 2>&1
   "%PRUNSRV%" delete wubiq-client >nul 2>&1
   "%PRUNSRV%" install wubiq-client --DisplayName "Wubiq" --Description "Wubiq print client" --Startup auto --Jvm "%ROOT%\runtime\bin\server\jvm.dll" --StartMode jvm --StopMode jvm --Classpath "!CP!" --StartPath "%DATA%" --StartClass net.sf.wubiq.clients.WubiqLauncher --StartParams start --StopClass net.sf.wubiq.clients.WubiqLauncher --StopParams stop --LogPath "%DATA%\logs" --LogPrefix wubiq --StdOutput auto --StdError auto ++JvmOptions "-Dwubiq.package.type=msi" ++JvmOptions "--add-opens=java.base/java.lang=ALL-UNNAMED" ++JvmOptions "--add-opens=java.desktop/javax.print=ALL-UNNAMED" ++JvmOptions "--add-opens=java.desktop/sun.print=ALL-UNNAMED"
   if errorlevel 1 exit /b 1
   net start wubiq-client
   exit /b %errorlevel%

   :remove
   net stop wubiq-client >nul 2>&1
   "%PRUNSRV%" delete wubiq-client >nul 2>&1
   exit /b 0
   ```
   The `install` and `delete` forms and the options `--Startup`, `--StartMode`, `--StartClass`, `--StartParams`, `--StopMode`, `--StopClass`, `--StopParams`, `--StartPath`, `--LogPath`, `--LogPrefix`, `--StdOutput`, `--StdError` are the ones `install_service.bat` used with this same binary. `--Jvm` (the path of `jvm.dll`) replaces its `--JavaHome`; `--Classpath` and `++JvmOptions` are Procrun options. The three SIDs are Local System, Administrators and Users: users read the configuration directory and cannot write it.
4. New file `wubiq-installer/package/resources/windows/main.wxs`: copy the JDK 25 template from `https://raw.githubusercontent.com/openjdk/jdk/jdk-25-ga/src/jdk.jpackage/windows/classes/jdk/jpackage/internal/resources/main.wxs` unchanged, then make two insertions.
   1. Directly above the line `    <UIRef Id="JpUI"/>`:
      ```xml
          <CustomAction Id="WubiqServiceInstall" Directory="INSTALLDIR"
            ExeCommand="&quot;[SystemFolder]cmd.exe&quot; /c &quot;&quot;[INSTALLDIR]app\wubiq-service.bat&quot; install&quot;"
            Execute="deferred" Impersonate="no" Return="check" />
          <CustomAction Id="WubiqServiceRemove" Directory="INSTALLDIR"
            ExeCommand="&quot;[SystemFolder]cmd.exe&quot; /c &quot;&quot;[INSTALLDIR]app\wubiq-service.bat&quot; remove&quot;"
            Execute="deferred" Impersonate="no" Return="ignore" />

      ```
   2. Directly above the line `    </InstallExecuteSequence>`:
      ```xml
            <Custom Action="WubiqServiceRemove" Before="RemoveFiles">REMOVE="ALL"</Custom>
            <Custom Action="WubiqServiceInstall" After="InstallFiles">NOT REMOVE</Custom>
      ```
   The template is written for WiX 3; `jpackage` converts it when it finds WiX 4 or 5. `INSTALLDIR` is the directory id the template itself uses (`ARPINSTALLLOCATION`). The template runs `RemoveExistingProducts` before `CostInitialize`, so an update first runs the installed version's `WubiqServiceRemove` and then this version's `WubiqServiceInstall`.
5. New file `wubiq-installer/package/resources/linux/wubiq-Wubiq.service`, verbatim (`jpackage` replaces the two upper-case words):
   ```
   [Unit]
   Description=SERVICE_DESCRIPTION

   [Service]
   ExecStart=APPLICATION_LAUNCHER
   Restart=on-failure
   KillMode=process

   [Install]
   WantedBy=multi-user.target
   ```
   It is `unit-template.service` of JDK 25 plus `KillMode=process`: stopping the unit ends the launcher only, so the `dpkg` or `rpm` the launcher started for an update keeps running when the package's scripts stop the unit.
6. New file `wubiq-installer/package/resources/macos/net.sf.wubiq.client-Wubiq.plist`, verbatim (`jpackage` replaces `LABEL` and `APPLICATION_LAUNCHER`):
   ```xml
   <?xml version="1.0" encoding="UTF-8"?>
   <!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
   <plist version="1.0">
     <dict>
       <key>Label</key>
       <string>LABEL</string>
       <key>ProgramArguments</key>
       <array>
         <string>APPLICATION_LAUNCHER</string>
       </array>
       <key>RunAtLoad</key><true/>
       <key>KeepAlive</key><true/>
       <key>AbandonProcessGroup</key><true/>
       <key>StandardOutPath</key>
       <string>/var/log/wubiq.log</string>
       <key>StandardErrorPath</key>
       <string>/var/log/wubiq.log</string>
     </dict>
   </plist>
   ```
   It is `launchd.plist.template` of JDK 25 plus three keys. `AbandonProcessGroup` keeps the `installer` the launcher started for an update alive when the package's scripts unload the daemon.
7. `wubiq-installer/build.xml` — add, between the `project.classpath` path and target `compile`:
   ```xml
   	<property name="package.dir" value="${basedir}/package" />
   	<property name="package.build" value="${basedir}/build/package" />
   	<property name="dist" value="${basedir}/dist" />
   	<property name="jdk.bin" value="${java.home}/bin" />

   	<condition property="package.windows"><os family="windows" /></condition>
   	<condition property="package.macos"><os family="mac" /></condition>
   	<condition property="package.linux"><and><os family="unix" /><not><os family="mac" /></not></and></condition>
   ```
8. `wubiq-installer/build.xml` — add, after target `compile`, verbatim:
   ```xml
   	<target name="stage" depends="compile" description="Writes the package input and the Java runtime into build/package">
   		<delete dir="${package.build}" failonerror="false" />
   		<mkdir dir="${package.build}/input" />
   		<mkdir dir="${dist}" />
   		<jar jarfile="${package.build}/input/wubiq-common.jar">
   			<fileset dir="${common.dir}/bin" />
   			<fileset dir="${common.dir}/i18n" />
   		</jar>
   		<jar jarfile="${package.build}/input/wubiq-client.jar">
   			<fileset dir="${client.dir}/bin" />
   			<fileset dir="${client.dir}/i18n" />
   		</jar>
   		<jar jarfile="${package.build}/input/wubiq-installer.jar" basedir="${bin}" />
   		<copy todir="${package.build}/input" flatten="true">
   			<fileset dir="${lib}" includes="commons-logging-1.1.1.jar,miglayout15-swing.jar" />
   			<fileset dir="${client.dir}/lib" includes="cglib-nodep-3.1.jar,commons-cli-1.2.jar" />
   			<fileset dir="${common.dir}/lib" includes="commons-lang3-3.4.jar" />
   		</copy>
   		<java classname="net.sf.wubiq.clients.VersionInformation" outputproperty="app.version" failonerror="true" fork="true">
   			<classpath><fileset dir="${package.build}/input" includes="*.jar" /></classpath>
   		</java>
   		<exec executable="${jdk.bin}/jdeps" outputproperty="jdeps.modules" failonerror="true">
   			<arg value="--ignore-missing-deps" />
   			<arg value="--print-module-deps" />
   			<arg value="--multi-release" />
   			<arg value="25" />
   			<arg value="--class-path" />
   			<arg value="${package.build}/input/*" />
   			<arg value="${package.build}/input/wubiq-installer.jar" />
   			<arg value="${package.build}/input/wubiq-client.jar" />
   			<arg value="${package.build}/input/wubiq-common.jar" />
   		</exec>
   		<exec executable="${jdk.bin}/jlink" failonerror="true">
   			<arg value="--add-modules" />
   			<arg value="${jdeps.modules},java.logging,jdk.charsets" />
   			<arg value="--output" />
   			<arg value="${package.build}/runtime" />
   			<arg value="--strip-debug" />
   			<arg value="--no-man-pages" />
   			<arg value="--no-header-files" />
   		</exec>
   	</target>

   	<macrodef name="jpackage">
   		<attribute name="type" />
   		<attribute name="os" />
   		<attribute name="shortcut" default="" />
   		<element name="options" optional="true" />
   		<sequential>
   			<echo file="${package.build}/WubiqConfigurator-@{type}.properties">main-class=net.sf.wubiq.clients.WubiqConfigurator
   java-options=-Dwubiq.package.type=@{type}
   launcher-as-service=false
   @{shortcut}
   </echo>
   			<exec executable="${jdk.bin}/jpackage" failonerror="true">
   				<arg value="--type" />
   				<arg value="@{type}" />
   				<arg value="--name" />
   				<arg value="Wubiq" />
   				<arg value="--app-version" />
   				<arg value="${app.version}" />
   				<arg value="--vendor" />
   				<arg value="Wubiq" />
   				<arg value="--description" />
   				<arg value="Wubiq print client" />
   				<arg value="--input" />
   				<arg value="${package.build}/input" />
   				<arg value="--main-jar" />
   				<arg value="wubiq-installer.jar" />
   				<arg value="--main-class" />
   				<arg value="net.sf.wubiq.clients.WubiqLauncher" />
   				<arg value="--arguments" />
   				<arg value="start" />
   				<arg value="--runtime-image" />
   				<arg value="${package.build}/runtime" />
   				<arg value="--java-options" />
   				<arg value="-Dwubiq.package.type=@{type}" />
   				<arg value="--java-options" />
   				<arg value="--add-opens=java.base/java.lang=ALL-UNNAMED" />
   				<arg value="--java-options" />
   				<arg value="--add-opens=java.desktop/javax.print=ALL-UNNAMED" />
   				<arg value="--java-options" />
   				<arg value="--add-opens=java.desktop/sun.print=ALL-UNNAMED" />
   				<arg value="--add-launcher" />
   				<arg value="WubiqConfigurator=${package.build}/WubiqConfigurator-@{type}.properties" />
   				<arg value="--resource-dir" />
   				<arg value="${package.dir}/resources/@{os}" />
   				<arg value="--dest" />
   				<arg value="${package.build}/out-@{type}" />
   				<arg value="--verbose" />
   				<options />
   			</exec>
   			<java classname="net.sf.wubiq.utils.ClientPackages" outputproperty="package.file.@{type}" failonerror="true" fork="true">
   				<classpath><fileset dir="${package.build}/input" includes="*.jar" /></classpath>
   				<arg value="@{type}" />
   			</java>
   			<move todir="${dist}">
   				<fileset dir="${package.build}/out-@{type}" includes="*.@{type}" />
   				<mergemapper to="${package.file.@{type}}" />
   			</move>
   		</sequential>
   	</macrodef>

   	<target name="package-windows" depends="stage" if="package.windows">
   		<copy todir="${package.build}/input">
   			<fileset dir="${package.dir}/windows" />
   		</copy>
   		<jpackage type="msi" os="windows" shortcut="win-menu=true">
   			<options>
   				<arg value="--win-upgrade-uuid" />
   				<arg value="E1687847-85F4-415F-8395-E4B6674F9FCC" />
   			</options>
   		</jpackage>
   	</target>

   	<target name="package-macos" depends="stage" if="package.macos">
   		<jpackage type="pkg" os="macos">
   			<options>
   				<arg value="--launcher-as-service" />
   				<arg value="--mac-package-identifier" />
   				<arg value="net.sf.wubiq.client" />
   			</options>
   		</jpackage>
   	</target>

   	<target name="package-linux" depends="stage" if="package.linux">
   		<jpackage type="deb" os="linux" shortcut="linux-shortcut=true">
   			<options>
   				<arg value="--launcher-as-service" />
   				<arg value="--linux-package-name" />
   				<arg value="wubiq" />
   			</options>
   		</jpackage>
   		<jpackage type="rpm" os="linux" shortcut="linux-shortcut=true">
   			<options>
   				<arg value="--launcher-as-service" />
   				<arg value="--linux-package-name" />
   				<arg value="wubiq" />
   			</options>
   		</jpackage>
   	</target>

   	<target name="package" depends="package-windows,package-macos,package-linux"
   			description="Writes the native package of this operating system into dist" />
   ```
   Facts the block relies on:
   - `jlink` is run without `--strip-native-commands`, so the runtime keeps `bin/java` (`jpackage` strips it only from a runtime it builds itself).
   - `jdk.charsets` holds `IBM850`, the charset the fast dot-matrix path is run with; `java.logging` is reached by commons-logging through reflection, which `jdeps` does not see.
   - `--launcher-as-service` is not passed on Windows: the MSI registers the service through `wubiq-service.bat`.
   - `ClientPackages` (in `wubiq-common.jar`) prints the file name for the platform the build runs on; `VersionInformation` (in `wubiq-client.jar`) prints the version. No build file holds a version.
   - `dist`, `build` and `bin` are ignored by `wubiq-installer/.gitignore`; no output is committed.

##### Green tests
1. `.claude/scripts/suite.sh` — full suite green (review-issue runs this)
2. `.claude/scripts/gate.sh` clean
<!-- section:INS-BLD-2:end -->
<!-- section:INS-BE-1:start -->
### INS-BE-1 — Backend · wubiq-installer · the launcher runs the bundled client
Depends on: INS-DOC-1, INS-BLD-2, COM-BE-2

#### Documentation references
`wubiq-installer/README.md` § Installing, § Configuring.

#### Implementation details
Language level: Java 25 (`release="25"` in `wubiq-installer/build.xml`; `--release 25` in the registry row `INS`). The code below needs nothing above Java 8 syntax.

`INS` has no suite: every AC is mechanical or manual.

How the launcher works today, kept by this section: `WubiqLauncher#main(String[])` with `start` opens a command socket and starts a `RunningClient`; `RunningClient#run()` loops over `createJavaRun()`, `killClient()` and `runClient()`; `runClient()` starts the client as a child process with `JavaRun#command()` and waits for it. This section changes which command that is, where the configuration is read, and removes the jar download.

##### Acceptance criteria
- [ ] AC-1: the launcher no longer downloads `wubiq-client.jar` nor compares versions for equality (mechanical — no red test; verified by `git grep -n -E 'wubiq-client\.jar|loadNewJar|wubiqClientVersion|wubiqClientFile|StreamGobbler' -- wubiq-installer/src` returning nothing)
- [ ] AC-2: the client child process is started with the runtime's `java`, the three add-opens options, the class path `<application directory>/*` and the main class `net.sf.wubiq.clients.LocalPrintManager` (mechanical — no red test; verified by `grep -c -E 'InstallerKeys\.CLIENT_ADD_OPENS|InstallerKeys\.CLIENT_MAIN_CLASS|appDirectory\(\)' wubiq-installer/src/net/sf/wubiq/clients/RunningClient.java` printing at least `3`, and `.claude/scripts/gate.sh` ending `EXIT: 0`)
- [ ] AC-3: a packaged client reads `wubiq-installer.properties` from the configuration directory; a client started without `wubiq.package.type` reads it from the working directory as before (mechanical — no red test; verified by `grep -c 'configDirectory()' wubiq-installer/src/net/sf/wubiq/utils/InstallerProperties.java` printing `1` and `grep -c 'super.getPropertiesFile(fileName.toString())' wubiq-installer/src/net/sf/wubiq/utils/InstallerProperties.java` printing `1`)
- [ ] AC-4: when the launcher's JVM ends, the client child process is destroyed (mechanical — no red test; verified by `grep -c -E 'addShutdownHook|destroyProcess\(\)' wubiq-installer/src/net/sf/wubiq/clients/WubiqLauncher.java` printing at least `2`)
- [ ] AC-5: on a clean virtual machine with no Java, for each of Windows 10 or later, macOS and Linux (DEB on Debian or Ubuntu, RPM on Fedora): the package installs; after a reboot the service is running; the configurator opens and saves as administrator; the server console lists the machine's printers; the test page prints (manual — no red test; verified by installing the package of INS-BLD-2 as `wubiq-installer/README.md` § Installing and § Configuring describe, and printing the test page from the server console)
- [ ] AC-6: Given an installed package with no `wubiq-installer.properties`, when the service starts, then it keeps running and retrying, starts no client, and connects after the configurator saves a server address (degraded path; manual — no red test; verified on the machine of AC-5 before the first save: the service is listed as running, and the server console shows the client within a minute of the save)
- [ ] AC-7: the peers' LX-300+II prints the server's test page and an invoice with the packaged client on Windows, with text at its real width and in the same places as with the Java 6 client (manual — no red test; verified by the peers' scans of both pages, compared with their Java 6 scans)
- [ ] AC-8: the Windows workflow prints the test page with the packaged client `SAME` as Oracle Java 6 on the LX-350 driver, and its extents row reads `EXTENTS OK`; on macOS the `packaged` row of `wubiq-client/test-dotmatrix-e2e.sh` reads `SAME` and its extents row `EXTENTS OK`; on Linux, whose CUPS driver the macOS golden job was not printed with, the `packaged-extents` row reads `EXTENTS OK` (manual — no red test; verified by the summary of the workflows of PR H2)

##### Code changes
1. `wubiq-installer/src/net/sf/wubiq/common/InstallerKeys.java` — add:
   ```java
   /** Options that open the JDK packages the client reaches by reflection (client.add.opens of wubiq-client/build.xml). */
   public static final String[] CLIENT_ADD_OPENS = {
   	"--add-opens=java.base/java.lang=ALL-UNNAMED",
   	"--add-opens=java.desktop/javax.print=ALL-UNNAMED",
   	"--add-opens=java.desktop/sun.print=ALL-UNNAMED"
   };
   public static final String CLIENT_MAIN_CLASS = "net.sf.wubiq.clients.LocalPrintManager";
   public static final String CONFIG_DIRECTORY_NAME = "Wubiq";
   ```
2. `wubiq-installer/src/net/sf/wubiq/utils/InstallerUtils.java`:
   1. delete the methods `wubiqClientFile()` and `wubiqClientVersion()`, and the class `StreamGobbler` at the end of the file; delete the imports that become unused (`java.io.BufferedReader`, `java.io.IOException`, `java.io.InputStream`, `java.io.InputStreamReader`);
   2. add the imports `java.net.URISyntaxException`, `net.sf.wubiq.common.InstallerKeys` and `net.sf.wubiq.common.PropertyKeys`, and the methods:
      - `public String packageType()`: returns `System.getProperty(PropertyKeys.WUBIQ_PACKAGE_TYPE)`, `""` when it is `null`.
      - `public boolean isPackaged()`: returns `!Is.emptyString(packageType())`.
      - `public File appDirectory()`: returns `new File(InstallerUtils.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParentFile()` — the directory that holds `wubiq-installer.jar` and the other jars, whatever the operating system's package layout; a `URISyntaxException` is rethrown as `IllegalStateException`.
      - `public File configDirectory()`: with `os = System.getProperty("os.name")`: when `os` starts with `Windows`, `new File(programData, InstallerKeys.CONFIG_DIRECTORY_NAME)` where `programData` is `System.getenv("ProgramData")`, `C:\ProgramData` when that is `null`; when `os` starts with `Mac`, `new File("/Library/Application Support", InstallerKeys.CONFIG_DIRECTORY_NAME)`; otherwise `new File("/etc/wubiq")`. Calls `mkdirs()` on it when it does not exist, ignoring the result, and returns it.
3. `wubiq-installer/src/net/sf/wubiq/utils/InstallerProperties.java` `InstallerProperties#getPropertiesFile()` — replace the last statement `return super.getPropertiesFile(fileName.toString());` with:
   ```java
   if (InstallerUtils.INSTANCE.isPackaged()) {
   	return new File(InstallerUtils.INSTANCE.configDirectory(), fileName.toString() + ".properties");
   }
   return super.getPropertiesFile(fileName.toString());
   ```
   A packaged client always gets a `File`, existing or not: `BaseProperties#getProperties()` reports a missing file through `showPropertiesNotFound()`, and `WubiqConfigurator#save(boolean)` writes to the file it is given.
4. `wubiq-installer/src/net/sf/wubiq/clients/RunningClient.java` `RunningClient#createJavaRun()`:
   1. delete `File wubiqClientJar = InstallerUtils.INSTANCE.wubiqClientFile();`;
   2. directly after the loop that fills `jvmParameters` from `getJvmParameters()`, add:
      ```java
      for (String option : InstallerKeys.CLIENT_ADD_OPENS) {
      	jvmParameters.add(option);
      }
      jvmParameters.add("-cp");
      jvmParameters.add(new File(InstallerUtils.INSTANCE.appDirectory(), "*").getPath());
      ```
   3. replace `javaRun.setJarFile(wubiqClientJar.getPath());` with `javaRun.setMainName(InstallerKeys.CLIENT_MAIN_CLASS);` (`JavaRun#command()` puts the main class after the JVM parameters when no jar is set);
   4. replace everything from `boolean loadNewJar = true;` to the end of the method with the single statement `serverVersion();`. The call stays: it throws `ConnectException` while no server answers, which makes `run()` wait and retry, and it sets the preferred URL `killClient()` and `runClient()` need.
   5. imports: add `net.sf.wubiq.common.InstallerKeys`; delete `java.io.FileOutputStream`, `java.net.HttpURLConnection` and `net.sf.wubiq.utils.IOUtils`.
5. Same file, `RunningClient#wubiqClientRun(boolean, String...)`:
   1. before `processBuilder.command(command);` add: `if (InstallerUtils.INSTANCE.isPackaged()) { processBuilder.directory(InstallerUtils.INSTANCE.configDirectory()); }`;
   2. directly after `currentProcess = processBuilder.start();` add `if (setProcess) { process = currentProcess; }`, and delete the later `if (setProcess) { process = currentProcess; }` that runs after `waitFor()`.
6. Same file — add:
   ```java
   /**
    * Ends the client process at once. Called when the launcher's JVM is shutting down.
    */
   public void destroyProcess() {
   	stopClient = true;
   	if (process != null) {
   		process.destroy();
   	}
   }
   ```
7. `wubiq-installer/src/net/sf/wubiq/clients/WubiqLauncher.java` constructor `WubiqLauncher(String)` — after `this.serviceName = serviceName;` add:
   ```java
   Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
   	public void run() {
   		if (runningClient != null) {
   			runningClient.destroyProcess();
   		}
   	}
   }, "WubiqShutdown"));
   ```
   systemd (`KillMode=process`) and launchd (`AbandonProcessGroup`) end only the launcher; the hook ends the client it started.

##### Contract changes
| Site | Change | New handling |
|---|---|---|
| `RunningClient#createJavaRun()` — the only caller of `InstallerUtils#wubiqClientFile()` and `InstallerUtils#wubiqClientVersion()` | methods removed | step 4 |
| `WubiqConfigurator#save(boolean)`, `WubiqConfigurator#loadProperties()`, `WubiqLauncher#requestServiceCommand`, `WubiqLauncher#run()` — callers of `InstallerProperties#getPropertiesFile()` directly or through `get` | packaged: the file is in the configuration directory | no edit |
| Released 2.7.4 launchers (IzPack installations) | not rebuilt by this section | they keep their own classes; SRV-BE-1 keeps serving them |

##### Green tests
1. `.claude/scripts/suite.sh` — full suite green (review-issue runs this)
2. `.claude/scripts/gate.sh` clean
<!-- section:INS-BE-1:end -->
<!-- section:INS-BE-2:start -->
### INS-BE-2 — Backend · wubiq-installer · update by package from the running service
Depends on: INS-DOC-1, INS-BE-1, SRV-BE-1

#### Documentation references
`wubiq-installer/README.md` § Updating; `wubiq-server/README.md` § Client packages.

#### Implementation details
Language level: Java 25, as stated in INS-BE-1. `INS` has no suite: every AC is mechanical or manual. The rules that can be tested are in `ClientPackages` (COM-BE-2), which has the tests.

Protocol, client side (the constants are COM-BE-2's):

| Step | Request to the servlet URL | Answer |
|---|---|---|
| Ask | `command=v1` (`CommandKeys.READ_VERSION`) with `cv=<Labels.VERSION>` (`ParameterKeys.CLIENT_VERSION`), `cpf=<platform>` (`ParameterKeys.CLIENT_PLATFORM`), `cpt=<type>` (`ParameterKeys.CLIENT_PACKAGE_TYPE`) | the version on offer, as text; empty when none. A server older than SRV-BE-1 ignores `cpf` and `cpt` and answers its own version, which is never higher than a packaged client's |
| Download | `command=v2` (`CommandKeys.READ_PACKAGE`) with `uuid`, `cpf`, `cpt` | the package, `application/octet-stream`, with `Content-Length`; HTTP 404 when none |

##### Acceptance criteria
- [ ] AC-1: the launcher asks with its version, platform and package type, and updates only through `ClientPackages#isNewer(String, String)` (mechanical — no red test; verified by `grep -c -E 'ParameterKeys\.CLIENT_PLATFORM|ParameterKeys\.CLIENT_PACKAGE_TYPE' wubiq-installer/src/net/sf/wubiq/clients/RunningClient.java` printing `2`, `grep -c 'ClientPackages.isNewer(offered, Labels.VERSION)' wubiq-installer/src/net/sf/wubiq/utils/PackageUpdater.java` printing `1`, and `.claude/scripts/gate.sh` ending `EXIT: 0`)
- [ ] AC-2: the package is streamed to a file and its size is checked against `Content-Length` before the installer runs; the installer command comes from `ClientPackages#installCommand(String, String)` (mechanical — no red test; verified by `grep -c -E 'getContentLengthLong\(\)|ClientPackages\.installCommand\(' wubiq-installer/src/net/sf/wubiq/utils/PackageUpdater.java` printing at least `2` and `grep -c 'pollServer' wubiq-installer/src/net/sf/wubiq/utils/PackageUpdater.java` printing `0`)
- [ ] AC-3: Given a packaged client of version N and a server whose packages directory holds version N+1 for its platform and type, when the service next starts its client, then it installs N+1 unattended, the service runs again on N+1, the configuration is unchanged and the client prints — on Windows (MSI), macOS (PKG), Debian or Ubuntu (DEB) and Fedora (RPM) (manual — no red test; verified on the machines of INS-BE-1 AC-5 with two packages built by `ant package` from two commits whose `Labels.VERSION` differ, the higher one copied into the server's `client.packages.directory`; the server console then shows the client with the higher version)
- [ ] AC-4: Given a server that offers the client's own version, a lower version, nothing, or a legacy answer, when the service starts its client, then nothing is downloaded and the client runs (manual — no red test; verified on one machine of AC-3 with the packages directory holding only the installed version, then only a lower one, then empty: `wubiq-client-update.log` is not written and no package file appears in the temporary directory)
- [ ] AC-5: Given a download that ends early, an HTTP 404, or an installer that exits with a code other than 0, when the update runs, then the partial file is deleted, the failure is logged with its reason, the installed client keeps running, and that version is not tried again until the service restarts (error paths; manual — no red test; verified on one machine of AC-3 by offering a file named as version N+1 that is not a package: the log names the installer's exit code, the client keeps printing, and no second attempt is logged until the service is restarted)

##### Code changes
1. New file `wubiq-installer/src/net/sf/wubiq/utils/PackageUpdater.java`, `public enum PackageUpdater { INSTANCE; … }` (new: `grep -rIl PackageUpdater` over the repository returns nothing), with a commons-logging `LOG` as the other classes of the package have, the field `private String failedVersion;` and:
   - `public boolean updateIfNewer(String offered, URL servletUrl, String uuid)`:
     1. returns `false` when `!ClientPackages.isNewer(offered, Labels.VERSION)`, when `servletUrl == null`, when `offered.equals(failedVersion)`, or when `ClientPackages.currentPlatform()` or `InstallerUtils.INSTANCE.packageType()` is empty;
     2. `File target = new File(System.getProperty("java.io.tmpdir"), ClientPackages.fileName(offered, platform, type))`;
     3. calls `download(servletUrl, uuid, platform, type, target)`;
     4. `String[] command = ClientPackages.installCommand(type, target.getAbsolutePath())`; logs at info `"Updating from " + Labels.VERSION + " to " + offered + ": " + Arrays.toString(command)`;
     5. starts it with a `ProcessBuilder(command)`, `redirectErrorStream(true)` and `redirectOutput(new File(System.getProperty("java.io.tmpdir"), "wubiq-client-update.log"))`, and waits with `waitFor()`;
     6. exit code 0: returns `true`. Any other exit code: sets `failedVersion = offered`, logs at error `"The update to " + offered + " failed: the installer ended with " + exitCode + "; see wubiq-client-update.log"`, returns `false`;
     7. an `IOException` or `InterruptedException` from steps 3 to 5: sets `failedVersion = offered`, logs it at error with `"The update to " + offered + " failed: " + e.getMessage()`, returns `false`.
   - `private void download(URL servletUrl, String uuid, String platform, String type, File target) throws IOException`: the body is `ParameterKeys.UUID + "=" + WebUtils.INSTANCE.encode(uuid) + "&" + ParameterKeys.COMMAND + "=" + CommandKeys.READ_PACKAGE + "&" + ParameterKeys.CLIENT_PLATFORM + "=" + platform + "&" + ParameterKeys.CLIENT_PACKAGE_TYPE + "=" + type`, as UTF-8 bytes. Opens `(HttpURLConnection) servletUrl.openConnection()`, `setDoOutput(true)`, `setRequestMethod("POST")`, `setConnectTimeout(10000)`, `setReadTimeout(60000)`, `setUseCaches(false)`; writes the body to `getOutputStream()` and closes it. When `getResponseCode() != 200`, throws `new IOException("the server answered " + code)`. Reads `long expected = connection.getContentLengthLong()`, then `IOUtils.INSTANCE.copy(connection.getInputStream(), new FileOutputStream(target))` (it closes both). When `expected <= 0` or `target.length() != expected`: deletes `target` and throws `new IOException("incomplete package: " + target.length() + " of " + expected + " bytes")`. The package is never held in memory: `AbstractLocalPrintManager#pollServer` is not used for it.
2. `wubiq-installer/src/net/sf/wubiq/clients/RunningClient.java`:
   1. `RunningClient#serverVersion()` — replace its body with:
      ```java
      String platform = ClientPackages.currentPlatform();
      String type = InstallerUtils.INSTANCE.packageType();
      if (Is.emptyString(platform) || Is.emptyString(type)) {
      	return (String) pollServer(CommandKeys.READ_VERSION);
      }
      return (String) pollServer(CommandKeys.READ_VERSION,
      		ParameterKeys.CLIENT_VERSION + ParameterKeys.PARAMETER_SEPARATOR + Labels.VERSION,
      		ParameterKeys.CLIENT_PLATFORM + ParameterKeys.PARAMETER_SEPARATOR + platform,
      		ParameterKeys.CLIENT_PACKAGE_TYPE + ParameterKeys.PARAMETER_SEPARATOR + type);
      ```
   2. `RunningClient#createJavaRun()` — replace the last statement `serverVersion();` (INS-BE-1) with:
      ```java
      String offered = serverVersion();
      PackageUpdater.INSTANCE.updateIfNewer(offered, getPreferredURL(), getUuid());
      ```
      After a successful update the operating system's installer has stopped this service; after a refused or failed one the method returns and the client starts as usual.
   3. imports: add `net.sf.wubiq.common.ParameterKeys`, `net.sf.wubiq.utils.ClientPackages`, `net.sf.wubiq.utils.Labels` and `net.sf.wubiq.utils.PackageUpdater`.

##### Contract changes
| Site | Change | New handling |
|---|---|---|
| `RemotePrintServlet#doGet` — reader of `v1` and `v2` | receives `cpf` and `cpt` | SRV-BE-1 |
| A server older than SRV-BE-1 | receives `v1` with parameters it ignores; never receives `v2`, because its answer is never a higher version | none |
| `WubiqConfigurator` | asks the same question to show a notice | INS-UI-1 |

##### Green tests
1. `.claude/scripts/suite.sh` — full suite green (review-issue runs this)
2. `.claude/scripts/gate.sh` clean
<!-- section:INS-BE-2:end -->
<!-- section:INS-BE-3:start -->
### INS-BE-3 — Backend · wubiq-installer · take over an IzPack installation
Depends on: INS-DOC-1, INS-BE-2

#### Documentation references
`wubiq-installer/README.md` § Upgrading from an IzPack installation.

#### Implementation details
Language level: Java 25, as stated in INS-BE-1. `INS` has no suite: every AC is mechanical or manual.

What an IzPack installation left on the machine (from the scripts INS-BLD-1 removes; quoted here because they are gone from the tree):

| Operating system | Service | Where its `wubiq-installer.properties` is |
|---|---|---|
| Windows | `wubiq`, registered by `prunsrv.exe install wubiq … --StartPath "<install path>\bin"`; `prunsrv.exe` is in `<install path>\bin` | `<install path>\bin` — the directory of the service's executable |
| Linux, macOS | `/etc/init.d/wubiq`, whose first command is `cd <install path>/bin`; registered with `update-rc.d wubiq defaults` | `<install path>/bin` |

Its launcher is the same `WubiqLauncher`: it listens on the installer port (`InstallerProperties#getInstallerPortAddress()`, 51000 by default) and ends itself on the `STOP` command.

##### Acceptance criteria
- [ ] AC-1: a packaged launcher looks for an IzPack installation before it opens its command socket, on `start` only (mechanical — no red test; verified by `grep -c -E 'LegacyInstallation\.INSTANCE\.(find|importConfiguration|removeService)\(' wubiq-installer/src/net/sf/wubiq/clients/WubiqLauncher.java` printing `3` and `.claude/scripts/gate.sh` ending `EXIT: 0`)
- [ ] AC-2: Given a machine with an IzPack installation that prints, when the package is installed over it, then after the first start the new client is registered at the server with the old unique id, groups, connections and printer type lists, the old service is gone (`sc query wubiq` fails on Windows; `/etc/init.d/wubiq` does not exist on Linux), and the test page prints — on Windows and on Debian or Ubuntu (manual — no red test; verified on a virtual machine with Java 8 and the tracked `wubiq-server/WebContent/wubiq-setup.jar` installed and configured first, then the package of INS-BLD-2)
- [ ] AC-3: Given a configuration directory that already holds `wubiq-installer.properties`, when an IzPack installation is found, then the existing file is kept and only the old service is removed (mechanical — no red test; verified by `grep -c '!current.exists()' wubiq-installer/src/net/sf/wubiq/utils/LegacyInstallation.java` printing `1`)
- [ ] AC-4: Given no IzPack installation, or one whose properties file is missing, or a command (`sc`, `update-rc.d`) that fails, when the launcher starts, then it logs what it found and starts the client normally; it never ends because of the takeover (error paths; mechanical — no red test; verified by reading `LegacyInstallation`: every method catches `IOException` and `InterruptedException`, logs them at error level and returns)

##### Code changes
1. New file `wubiq-installer/src/net/sf/wubiq/utils/LegacyInstallation.java`, `public enum LegacyInstallation { INSTANCE; … }` (new: `grep -rIl LegacyInstallation` over the repository returns nothing), with a commons-logging `LOG` and:
   - `public File find()`: returns the old installation's `bin` directory, or `null`.
     - `os.name` starts with `Windows`: runs `sc qc wubiq` (`ProcessBuilder`, `redirectErrorStream(true)`), reads its output lines, waits for it; returns `null` when the exit code is not 0 or no line contains `BINARY_PATH_NAME`. From that line takes the text after the first `:` and trims it; when it starts with `"`, the path is the text up to the next `"`; otherwise the text up to the first space. Returns `new File(path).getParentFile()`.
     - any other system: returns `null` when `/etc/init.d/wubiq` is not a file; otherwise reads it line by line and returns `new File(line.trim().substring(3).trim())` for the first line whose trimmed form starts with `cd `; `null` when there is none.
     Logs at info `"IzPack installation found in " + directory` before returning a directory.
   - `public void importConfiguration(File legacyBin)`: `File old = new File(legacyBin, "wubiq-installer.properties")`, `File current = new File(InstallerUtils.INSTANCE.configDirectory(), "wubiq-installer.properties")`. When `old.isFile() && !current.exists()`: `java.nio.file.Files.copy(old.toPath(), current.toPath())`, `InstallerProperties.INSTANCE().resetProperties()`, and logs at info `"Configuration imported from " + old`. Otherwise logs at info which of the two conditions kept it from importing.
   - `public void removeService()`:
     - Windows: runs `sc stop wubiq`, then `sc delete wubiq`, each with `ProcessBuilder`, output discarded (`redirectErrorStream(true)` and the stream read to its end), waiting for each; logs each exit code at info.
     - any other system: when `/usr/sbin/update-rc.d` can be executed, runs `/usr/sbin/update-rc.d -f wubiq remove` the same way; then deletes `/etc/init.d/wubiq` and logs at info whether `delete()` returned `true`.
   Every method catches `IOException` and `InterruptedException`, logs them at error level with `LOG.error(e.getMessage(), e)` and returns (`find()` returns `null`).
2. `wubiq-installer/src/net/sf/wubiq/clients/WubiqLauncher.java` — add the private static method:
   ```java
   /**
    * Ends a launcher that listens on this service's port: the one of an IzPack installation.
    */
   private static void stopLegacyLauncher(String serviceName) throws InterruptedException {
   	for (int attempt = 0; attempt < 30; attempt++) {
   		if (ServiceReturnStatus.NO_SERVICE.equals(requestServiceCommand(ServiceCommandType.STATUS, serviceName))) {
   			return;
   		}
   		requestServiceCommand(ServiceCommandType.STOP, serviceName);
   		Thread.sleep(1000);
   	}
   	LOG.error("The IzPack installation's launcher is still running.");
   }
   ```
3. Same file, `WubiqLauncher#main(String[])` — directly before the line `ServiceReturnStatus currentServiceStatus = requestServiceCommand(ServiceCommandType.STATUS, serviceName);` add:
   ```java
   if ("start".equalsIgnoreCase(args[0]) && InstallerUtils.INSTANCE.isPackaged()) {
   	File legacyBin = LegacyInstallation.INSTANCE.find();
   	if (legacyBin != null) {
   		LegacyInstallation.INSTANCE.importConfiguration(legacyBin);
   		stopLegacyLauncher(serviceName);
   		LegacyInstallation.INSTANCE.removeService();
   	}
   }
   ```
   and the imports `java.io.File`, `net.sf.wubiq.utils.InstallerUtils` and `net.sf.wubiq.utils.LegacyInstallation`. The configuration is imported first, so the port the old launcher is asked on is the one its own file sets.

##### Green tests
1. `.claude/scripts/suite.sh` — full suite green (review-issue runs this)
2. `.claude/scripts/gate.sh` clean
<!-- section:INS-BE-3:end -->
<!-- section:INS-UI-1:start -->
### INS-UI-1 — UI · wubiq-installer · configurator save error and update notice
Depends on: INS-DOC-1, INS-BE-1

#### Documentation references
`wubiq-installer/README.md` § Configuring (the configurator is started as an administrator), § Updating (the configurator's title shows when a higher version is on offer).

#### Implementation details
Language level: Java 25, as stated in INS-BE-1. `INS` has no suite: every AC is mechanical or manual.

The configurator's messages are in `wubiq-installer/src/net/sf/wubiq/i18n/installer_messages_en.properties` and `installer_messages_es.properties`, read through `InstallerBundle#getMessage(String, Object...)`, which replaces `{0}` with `String#replaceAll`.

##### Acceptance criteria
- [ ] AC-1: both locale files have the two new keys (mechanical — no red test; verified by `grep -c -E '^(error\.cannot_write_configuration|info\.update_available)=' wubiq-installer/src/net/sf/wubiq/i18n/installer_messages_en.properties` printing `2`, and the same for `installer_messages_es.properties`)
- [ ] AC-2: Given a configuration file the user cannot write, when the configurator saves, then it shows the `error.cannot_write_configuration` message with the system's reason, restarts nothing and stays open (error path; mechanical — no red test; verified by `grep -c 'error.cannot_write_configuration' wubiq-installer/src/net/sf/wubiq/clients/WubiqConfigurator.java` printing `2`; manual check: start the installed configurator without administrator rights and save)
- [ ] AC-3: Given a packaged configurator and a server that offers a higher version, when the window opens, then its title reads `Version:<own version> - version <offered> is available; the service installs it by itself` (Spanish: `- la versión <offered> está disponible; el servicio la instala por sí mismo`); given an equal or lower offer, no answer, or no saved connection, the title stays `Version:<own version>` (manual — no red test; verified on the machine of INS-BE-2 AC-3 before the service updates, and on a machine with nothing on offer)
- [ ] AC-4: the notice never blocks the window: the question is asked on a thread of its own and a server that does not answer leaves the title unchanged (mechanical — no red test; verified by `grep -c -E 'private void checkForUpdate\(\)|setDaemon\(true\)' wubiq-installer/src/net/sf/wubiq/clients/WubiqConfigurator.java` printing `2`, and `.claude/scripts/gate.sh` ending `EXIT: 0`)

##### Code changes
1. `wubiq-installer/src/net/sf/wubiq/i18n/installer_messages_en.properties` — after the line that starts `error.invalid_address=`, add:
   ```
   error.cannot_write_configuration=The configuration could not be saved: {0}. Start the configurator as an administrator (macOS and Linux: with sudo).
   ```
   and after the line that starts `info.wubiq.created_successfully=`, add:
   ```
   info.update_available=version {0} is available; the service installs it by itself
   ```
2. `wubiq-installer/src/net/sf/wubiq/i18n/installer_messages_es.properties` — at the same two places, add:
   ```
   error.cannot_write_configuration=No se pudo guardar la configuraci\u00f3n: {0}. Inicie el configurador como administrador (macOS y Linux: con sudo).
   ```
   ```
   info.update_available=la versi\u00f3n {0} est\u00e1 disponible; el servicio la instala por s\u00ed mismo
   ```
   (unicode escapes typed as shown, as the file's other lines have them).
3. `wubiq-installer/src/net/sf/wubiq/clients/WubiqConfigurator.java` `WubiqConfigurator#save(boolean)` — in the `catch (FileNotFoundException e)` block and in the `catch (IOException e)` block, replace `handleException(e);` with
   `handleException(new IOException(InstallerBundle.getMessage("error.cannot_write_configuration", Matcher.quoteReplacement(String.valueOf(e.getMessage())))));`
   (`java.util.regex.Matcher` is imported already). `quoteReplacement` keeps the backslashes of a Windows path through `replaceAll`. The third block, `catch (Exception e)`, is unchanged.
4. Same class, `WubiqConfigurator#readCommandConnection(String, String)` — change its signature to `readCommandConnection(String address, String command, String... parameters)` and its call `encodedParameters(command)` to `encodedParameters(command, parameters)`. Its caller `WubiqConfigurator#testConnection(String)` is unchanged.
5. Same class — add the method `private void checkForUpdate()`:
   1. returns at once when `!InstallerUtils.INSTANCE.isPackaged()` or `Is.emptyString(ClientPackages.currentPlatform())`;
   2. otherwise creates a `Thread` named `UpdateCheck`, calls `setDaemon(true)` on it and starts it. The thread, for each non-empty trimmed part of `InstallerProperties.INSTANCE(serviceName).getConnections().split("[,;]")`: calls `readCommandConnection(address, CommandKeys.READ_VERSION, ParameterKeys.CLIENT_VERSION + "=" + Labels.VERSION, ParameterKeys.CLIENT_PLATFORM + "=" + ClientPackages.currentPlatform(), ParameterKeys.CLIENT_PACKAGE_TYPE + "=" + InstallerUtils.INSTANCE.packageType())`; a `null` answer moves to the next address; the first answer that is not `null` ends the loop. When that answer satisfies `ClientPackages.isNewer(answer, Labels.VERSION)`, it runs, through `EventQueue.invokeLater`: `frame.setTitle("Version:" + Labels.VERSION + " - " + InstallerBundle.getMessage("info.update_available", answer))`.
6. Same class, `WubiqConfigurator#initialize()` — as its last statement, after the `try { … } finally { changingId = false; }` block, add `checkForUpdate();`. Add the import `net.sf.wubiq.utils.ClientPackages`.

##### Green tests
1. `.claude/scripts/suite.sh` — full suite green (review-issue runs this)
2. `.claude/scripts/gate.sh` clean
<!-- section:INS-UI-1:end -->
<!-- section:AND-DOC-1:start -->
### AND-DOC-1 — Documentation · wubiq-android-gradle
Depends on: INS-DOC-1

#### Documentation references
`wubiq-android-gradle/README.md` § wubiq-android-gradle (the whole file is that heading and one paragraph).

#### Implementation details

##### CHANGELOG.md
1. § `## Wubiq 2.8.0` — after the last bullet of `### Wubiq-installer`, insert verbatim:
   ```
   ### Wubiq-android-gradle
   - The app has its own copy of the classes it shares with the server (the device table, the protocol names and the polling client) and no longer ships `wubiq-client.jar` and `wubiq-common.jar`; cglib, commons-cli and commons-logging leave the APK.
   ```

##### wubiq-android-gradle/README.md
1. After the paragraph `This is a the android module. Changes to android mobile apps are to be performed here.`, add verbatim:
   ```

   ### Shared classes

   The app does not depend on wubiq-common or wubiq-client. The classes it shares with the server are app sources, under `app/src/main/java/net/sf/wubiq/` in the packages `android` (`Mobile*`), `clients`, `common`, `enums`, `exceptions` and `utils`; they log through `java.util.logging`.

   Two things must agree with wubiq-server, and are changed in both places in the same change:
   - the device table, `android/MobileDevices.java`: the app sends only the device name, and the server looks the name up in the copy in `wubiq-common`;
   - the constants of `common/CommandKeys.java`, `common/ParameterKeys.java` and `common/DirectConnectKeys.java`, and the order of `enums/DirectConnectCommand.java`: they are the wire protocol.

   Everything else in those packages belongs to the app.
   ```

##### Acceptance criteria
- [ ] AC-1: the CHANGELOG entry sits under `## Wubiq 2.8.0` › `### Wubiq-android-gradle` and
      carries no issue reference (mechanical — no red test; verified by
      `git diff origin/master...HEAD -- CHANGELOG.md | grep -E '^\+.*\(#[0-9]+\)'` returning nothing)
- [ ] AC-2: nothing but this application's documentation modified, apart from this issue's
      spec file (its AC ticks) (mechanical — no red test; verified by
      `git diff --name-only origin/master...HEAD | grep -vE '^CHANGELOG\.md$|^wubiq-android-gradle/.*\.md$|^specs/issue-52-enriched-spec\.md$'`
      returning nothing)
- [ ] AC-3: `wubiq-android-gradle/README.md` has § Shared classes naming the device table and the protocol constants (mechanical — no red test; verified by `grep -c -E '^### Shared classes$|android/MobileDevices\.java|common/CommandKeys\.java' wubiq-android-gradle/README.md` printing `3`)

##### Green checks
1. `.claude/scripts/gate.sh` clean (code-issue); `.claude/scripts/suite.sh` — full suite green
   (review-issue runs the suite)
<!-- section:AND-DOC-1:end -->
<!-- section:AND-BLD-1:start -->
### AND-BLD-1 — Build · wubiq-android-gradle · shared classes as app sources, client and common jars removed
Depends on: AND-DOC-1

#### Documentation references
`wubiq-android-gradle/README.md` § Shared classes.

#### Implementation details
Levels: Java 8 (`compileOptions` of `app/build.gradle.kts`: `sourceCompatibility` and `targetCompatibility` `JavaVersion.VERSION_1_8`) · minSdk 28 · targetSdk 37

Recorded decisions this section carries out: the app keeps its own copy of the shared classes, and `app/libs/wubiq-client.jar` and `app/libs/wubiq-common.jar` are deleted. They are the only two tracked libraries any section of this issue touches; `ZSDK_API.jar` and the three `.aar` files stay as they are. No Gradle dependency is added: the copies log through `java.util.logging`, which is what commons-logging 1.1.1 resolves to on Android.

The copies are the sources on `master` when this section is coded, with the edits of steps 3 and 4 only. No class changes what it does.

##### Acceptance criteria
- [ ] AC-1: the app no longer depends on the two jars and they are gone (mechanical — no red test; verified by `grep -c 'wubiq-client.jar' wubiq-android-gradle/app/build.gradle.kts` printing `0` and `git ls-files wubiq-android-gradle/app/libs` printing exactly `StarIODeviceSetting-5.20.0.aar`, `ZSDK_API.jar`, `stario-5.20.0.aar` and `starioextension-5.20.0.aar` under that directory)
- [ ] AC-2: the 21 classes of step 1 and the two label files of step 2 are app sources (mechanical — no red test; verified by `git ls-files wubiq-android-gradle/app/src/main/java/net/sf/wubiq/clients wubiq-android-gradle/app/src/main/java/net/sf/wubiq/common wubiq-android-gradle/app/src/main/java/net/sf/wubiq/enums wubiq-android-gradle/app/src/main/java/net/sf/wubiq/exceptions wubiq-android-gradle/app/src/main/java/net/sf/wubiq/utils | wc -l` printing `16`, `ls wubiq-android-gradle/app/src/main/java/net/sf/wubiq/android/Mobile*.java | wc -l` printing `5`, and `ls wubiq-android-gradle/app/src/main/resources` printing `client_labels_en.properties` and `client_labels_es.properties`)
- [ ] AC-3: the device table, the protocol names and the command order are byte for byte the server's (mechanical — no red test; verified by `for f in android/MobileClientConversionStep android/MobileConversionHint android/MobileDeviceInfo android/MobileDevices android/MobileServerConversionStep common/CommandKeys common/ConfigurationKeys common/DirectConnectKeys common/ParameterKeys common/PropertyKeys enums/DirectConnectCommand exceptions/TimeoutException utils/Is; do diff -q wubiq-common/src/net/sf/wubiq/$f.java wubiq-android-gradle/app/src/main/java/net/sf/wubiq/$f.java; done` printing nothing)
- [ ] AC-4: no app source names commons-logging, commons-lang3 or a class the app does not hold (mechanical — no red test; verified by `grep -rn -E 'org\.apache\.commons|net\.sf\.wubiq\.(interfaces|wrappers|print)\.|NotificationType|ServerLabels' wubiq-android-gradle/app/src/main/java` returning nothing and `grep -c -E 'commons' wubiq-android-gradle/app/proguard-rules.pro` printing `0`)
- [ ] AC-5: the app and its unit tests compile and pass without the jars (mechanical — no red test; verified by `.claude/scripts/gate.sh` ending `EXIT: 0` and `.claude/scripts/suite.sh` ending `EXIT: 0`)
- [ ] AC-6: Given a release build, when R8 shrinks it, then it finds every class the app references; a class left behind in a jar makes it stop with `Missing class` (error path: a missing class fails the build, not the device; mechanical — no red test; verified by `(cd wubiq-android-gradle && ./gradlew -q :app:minifyReleaseWithR8)` ending with exit code 0)
- [ ] AC-7: the app built from this branch registers its printers at a wubiq-server and prints the mobile test page on a paired printer (manual — no red test; verified on a phone with the debug-signed release APK: start the service, check the device on the server console, print the test page)

##### Code changes
1. Copy these 21 files, each to the same path under `wubiq-android-gradle/app/src/main/java/net/sf/wubiq/` (create the directories):

   | From | Files |
   |---|---|
   | `wubiq-common/src/net/sf/wubiq/` | `android/MobileClientConversionStep.java`, `android/MobileConversionHint.java`, `android/MobileDeviceInfo.java`, `android/MobileDevices.java`, `android/MobileServerConversionStep.java`, `common/CommandKeys.java`, `common/ConfigurationKeys.java`, `common/DirectConnectKeys.java`, `common/ParameterKeys.java`, `common/PropertyKeys.java`, `enums/DirectConnectCommand.java`, `exceptions/TimeoutException.java`, `utils/BaseProperties.java`, `utils/DirectConnectUtils.java`, `utils/IOUtils.java`, `utils/Is.java`, `utils/Labels.java`, `utils/WebUtils.java` |
   | `wubiq-client/src/net/sf/wubiq/` | `clients/AbstractLocalPrintManager.java`, `utils/ClientLabels.java`, `utils/ClientProperties.java` |

   This is the set `javac` needs to compile `AbstractLocalPrintManager` and the classes the app imports, less the classes that reach `javax.print`, which Android does not have.
2. Copy `wubiq-client/i18n/client_labels_en.properties` and `wubiq-client/i18n/client_labels_es.properties` to `wubiq-android-gradle/app/src/main/resources/` (new directory; Gradle packages it as Java resources, where `ClientLabels` loads the `client_labels` bundle from).
3. In the copy `utils/DirectConnectUtils.java` only:
   1. delete the methods `notifyTimeout(IAdapter, Set<IRemoteListener>)`, `notifyException(IAdapter, Set<IRemoteListener>, String)` and `convertToGraphicParameters(Method, Object...)`, each with its Javadoc;
   2. delete the imports `net.sf.wubiq.enums.NotificationType`, `net.sf.wubiq.interfaces.IAdapter`, `net.sf.wubiq.interfaces.IRemoteListener`, `net.sf.wubiq.wrappers.GraphicParameter` and `java.util.Set`.
4. In the seven copies that log (`clients/AbstractLocalPrintManager.java`, `utils/BaseProperties.java`, `utils/ClientProperties.java`, `utils/DirectConnectUtils.java`, `utils/IOUtils.java`, `utils/Labels.java`, `utils/WebUtils.java`), replace commons-logging with `java.util.logging`, line by line:

   | Found | Replaced with |
   |---|---|
   | `import org.apache.commons.logging.Log;` | `import java.util.logging.Level;` — only in the files that get a `LOG.log(Level.SEVERE, …)` call below; deleted in the others |
   | `import org.apache.commons.logging.LogFactory;` | `import java.util.logging.Logger;` |
   | `Log LOG = LogFactory.getLog(X.class)` (the modifiers before it stay) | `Logger LOG = Logger.getLogger(X.class.getName())` |
   | `LOG.debug(a);` | `LOG.fine(String.valueOf(a));` |
   | `LOG.info(a);` | `LOG.info(String.valueOf(a));` |
   | `LOG.error(a);` | `LOG.severe(String.valueOf(a));` |
   | `LOG.error(a, e);` | `LOG.log(Level.SEVERE, String.valueOf(a), e);` |

   and in `utils/IOUtils.java` also: delete `import org.apache.commons.lang3.exception.ExceptionUtils;` and replace `LOG.debug(ExceptionUtils.getMessage(e));` with `LOG.fine(String.valueOf(e));`. These seven files hold no other commons-logging call (`fatal`, `warn` and `trace` do not occur in them).
5. `wubiq-android-gradle/app/build.gradle.kts` block `dependencies` — delete the line `implementation(files("libs/wubiq-client.jar"))`. Change nothing else in the file; `versionCode` and `versionName` are not edited.
6. Delete, with `git rm`: `wubiq-android-gradle/app/libs/wubiq-client.jar` and `wubiq-android-gradle/app/libs/wubiq-common.jar`.
7. `wubiq-android-gradle/app/proguard-rules.pro` — delete from the comment line that starts `# wubiq-client.jar bundles commons-logging 1.1.1` through the last line of the file (`-dontwarn org.apache.commons.lang3.exception.ExceptionUtils`): the commons-logging keep rule, its four `-dontwarn` lines and the commons-lang3 `-dontwarn` with their comments. No keep rule replaces them: nothing in the copies is reached by a class name, and `DirectConnectUtils` serializes only `java.util` and `java.lang` objects.

##### Resource and manifest changes
1. `app/src/main/resources/client_labels_en.properties`, `app/src/main/resources/client_labels_es.properties` — step 2; copied unchanged.
2. `app/src/main/AndroidManifest.xml` — no change.
3. `app/proguard-rules.pro` — step 7. `app/build.gradle.kts` — step 5.

##### Green tests
1. `.claude/scripts/suite.sh` — full suite green (review-issue runs this)
2. `.claude/scripts/gate.sh` clean
<!-- section:AND-BLD-1:end -->

---

### Out of Scope
- **PR H0, made by hand — the 2.7.4 client jar.** Before SRV-BE-1. From a checkout of commit `ed15bcf` (`master` before any section of this issue), with a JDK 8: `ant -f wubiq-client/build.xml create-client-jar`, copy `wubiq-client/dist/wubiq-client.jar` over `wubiq-server/WebContent/wubiq-client.jar`, and discard the two jars that build writes into `wubiq-android-gradle/app/libs/`. Commit only `wubiq-server/WebContent/wubiq-client.jar`. The tracked jar today reports 2.7.0 and has no `Add-Opens` manifest entry; the lane of SRV-BE-1 answers `2.7.4`.
- **PR H1, made by hand — the registry.** After WIT-BLD-1 and CLI-BLD-1 are merged, before CLI-BLD-2 and INS-BLD-1. In `.claude/scripts/apps.tsv`: row `CLI`, column `jdk`, `8` → `25`; row `INS`, column `jdk`, `8` → `25`, and in its `compile` column `-source 1.6 -target 1.6` → `--release 25`. A JDK 25 must be installed on every machine that runs the gate (`WUBIQ_JDKS` lists JDK homes when it is not found).
- **PR H2, made by hand — the workflows.** After INS-BLD-2 and CLI-BLD-3. A packaging workflow with a matrix of `windows-2022`, `macos` on arm64, `macos` on x64 and `ubuntu` (with `rpm` installed): JDK 25, `ant -f wubiq-installer/build.xml package`, upload `wubiq-installer/dist`. In `.github/workflows/dotmatrix-windows.yml`: a JDK 25 as `-CompileHome`, the MSI installed with `msiexec /i … /qn`, and `-PackagedJava "C:\Program Files\Wubiq\runtime\bin\java.exe" -PackagedApp "C:\Program Files\Wubiq\app"` passed to `wubiq-client\test-dotmatrix-e2e-windows.ps1`. On the macOS and Linux runners: the package installed, a CUPS Epson queue to the witness, and `wubiq-client/test-dotmatrix-e2e.sh --packaged-java … --packaged-app …`. The comparison of the three operating systems' jobs with each other (`compare` of the witness on the uploaded jobs) is a step of that workflow.
- **PR H3, made by hand — files outside every application.** `.vscode/launch-example.json`: remove the `InstallerSetupUtils` entry. The pipeline's rule texts that name the tracked jars of `wubiq-android-gradle/app/libs/` and the targets `create-*-jar` and `make_install` (`.claude/skills/`): reword once AND-BLD-1 and INS-BLD-1 are merged.
- Signing the MSI and the PKG, and any certificate or secret.
- `wubiq-server/WebContent/wubiq-setup.jar`, its download button and `wubiq-setup.do`: the tracked IzPack setup stays as it is and is not rebuilt.
- `wubiq-installer/build.gradle`, the Gradle wrapper, `tests-src`, `lib/uispec4j-*.jar`, `tests-readme.txt`, the tracked `wubiq-installer/wubiq-client.jar` and `wubiq-installer/wubiq-installer.properties`: not used by the package build, left as they are.
- `RunningClient#createJavaRun()` reads the dot-matrix font options from `InstallerProperties.INSTANCE("serviceName")`, a literal: unchanged here.
- The version number: `Labels.VERSION`, `versionName` and `versionCode` are release work.
- Removing the legacy lane and the 2.7.4 jar once no 2.7.x client connects.
- `wubiq-printerjob`, `wubiq-test`, `wubiq-android-test`, `wubiq-assets`.
- The fast dot-matrix path (#46).
- A new protocol or a Go client (#21).
<!-- enriched-spec:end -->
