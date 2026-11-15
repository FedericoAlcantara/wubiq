# improve-issue · Java (Ant) application sections

Read by `improve-issue` **Draft** when a `BLD`, `BE` or `UI` section of an application whose
registry toolchain is `java-ant` is in scope. Such a section changes one application directory,
built by that directory's `build.xml`. Rules 1–11 hold for every type; § Per type adds what
each type prescribes.

## Rules

1. One application per section. Every file the section edits sits under the application's
   directory (`.claude/scripts/apps.tsv`, column `dir`). Work in two applications is two
   sections, the dependent one carrying `Depends on:`.
2. Subsection order is fixed, and is the TDD order: acceptance criteria → test ports → red
   tests → code changes → contract changes → green tests.
3. Every test port carries its in and out shapes, and is marked `existing` (with its citation)
   or `new` (with its search evidence).
4. **Language level.** The `source` / `target` of the application's `build.xml` `javac` tasks
   decides which syntax and which JDK classes the section may prescribe; the section states the
   level it read. At `1.6` that excludes lambdas, method references, try-with-resources, the
   diamond operator, `switch` on a `String`, streams, `java.time` and `java.nio.file`.
5. **Build output.** A section never runs an Ant default target or a `create-*-jar` target:
   they overwrite the tracked jars in `wubiq-android-gradle/app/libs/`. It never commits `bin/`,
   `dist/`, `build/`, `build-tests/` or a jar. Compiling is the gate's job
   (`.claude/scripts/gate.sh`).
6. **Dependencies.** A third-party jar is added, replaced or removed only by a `BLD` section.
   A `BE` or `UI` section that needs one depends on that `BLD` section and prescribes no jar.
7. **Strings.** Every user-visible string is fixed, exact text, and comes from the
   application's `i18n/` bundle. The section gives the key and the text for every locale file
   the bundle has.
8. **Protocol.** A key, command or parameter name both ends of the wire use is defined in
   `wubiq-common`, in a `COM-BE-k` section. The consuming section depends on it and cites the
   constant; it never repeats the literal.
9. **Tests.** JUnit 4. The suites, and where a section's tests go:

   | Application | Test sources | Suite |
   |---|---|---|
   | `COM` | `wubiq-common/tests-src` | yes |
   | `WIT` | `wubiq-dotmatrix-witness/tests-src` | yes |
   | `CLI` | `wubiq-client/tests-src` | yes |
   | `SRV`, `INS`, `PJB` | — | none |

   A test is headless (`java.awt.headless=true`), needs no printer, device, server or network,
   never sleeps, and never asserts a version string. A section's tests sit in its own
   application, like the rest of its files (rule 1). In `wubiq-common` a test class is named
   `*Test`: `ant test` there runs every class with that suffix.
10. **Dot-matrix output.** `ant test-dotmatrix` only reports layout and golden-file mismatches
    unless `-Dwubiq.dotmatrix.strict=true` is set. An AC about what is printed names the JUnit
    assertion that fails, never the report.
11. An AC a test cannot reach ends `(mechanical — no red test; verified by <command>)` and
    names the command.

## Per type

| Type | Prescribes | ACs and their proof |
|---|---|---|
| `BLD` | `build.xml` targets, properties and classpaths; vendored jars (the jar file, the `lib` directory it goes in, why a class already on the classpath does not serve); manifests; the installer definition (`wubiq-installer/installation/`); launch scripts. No change to what a class does | mostly mechanical: the gate (`.claude/scripts/gate.sh`) for "still compiles", and a named command for the rest — the Ant target run and what it must print or produce, a `grep` on the build file, `unzip -l` on a jar the target builds outside the tracked ones. Subsections Test ports and Red tests are omitted when every AC is mechanical |
| `BE` | classes with no screen: protocol keys and wrappers, print managers, services, device drivers, servlet logic, persistence | red tests in the application's suite (rule 9): at least three ACs, at least one an error path (a missing printer, an unreadable stream, an unsupported option surfacing loudly). In `SRV`, `INS` and `PJB`, which have no suite, every AC is mechanical |
| `UI` | Swing screens, JSP and `WebContent`, and the i18n text they show (rule 7) | a Swing screen or a JSP is not reachable headless: an AC about what the user sees is mechanical (a `grep` for the key in every locale file, for the component or the tag), and an AC about behaviour is a red test on the logic behind the screen, moved into a method a test can call — the section names that method. Where no suite exists, every AC is mechanical |

## Template

```markdown
<!-- section:<APP>-<TYPE>-k:start -->
### <APP>-<TYPE>-k — <Type> · <application directory> · <capability>
Depends on: <ids>

#### Documentation references
<the README § sections specifying this behaviour>

#### Implementation details
Language level: <source/target read from build.xml>

##### Acceptance criteria
- [ ] AC-1: Given <state or input>, when `<Class>#<method>` <does what>, then <observable result>

##### Test ports
- `<fully.qualified.Class>#<method>(<parameter types>)` ·
  in: <shape> · out: <shape> · existing (`<path>.java`, `Class#method`) | new

##### Red tests (write first; each must fail before implementation)
| AC | Placement | Setup | Test file | Test name | Assertion sketch |
|---|---|---|---|---|---|
| AC-1 | amend \| add to \| new file | <fixture or object built in the test> | <application>/tests-src/<package path>/<Class>Test.java | `<methodName>` | `assertEquals(<expected>, <actual>)` |

##### Code changes
1. `<application>/src/<package path>/<Class>.java` `<Class>#<method>` — <the exact change>
2. <`UI`: i18n keys, with the text for every locale file>
3. <`BLD`: `<application>/build.xml` target `<name>` — the exact change; the jar and its `lib` directory>

##### Contract changes   (only when a signature, a protocol key or a serialized shape changes)
| Site | Change | New handling |
|---|---|---|
| `<path>.java` `Class#method` | <caller, implementer or reader of what changed> | <what it does now> |

##### Green tests
1. The red tests above pass, unmodified (code-issue runs these, targeted:
   `.claude/scripts/section-test.sh <APP> <fully.qualified.TestClass>`)
2. `.claude/scripts/suite.sh` — full suite green (review-issue runs this; code-issue runs only
   this section's test classes)
3. `.claude/scripts/gate.sh` clean
<!-- section:<APP>-<TYPE>-k:end -->
```
