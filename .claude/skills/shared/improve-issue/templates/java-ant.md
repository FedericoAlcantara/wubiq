# improve-issue · Java (Ant) application sections

Read by `improve-issue` **Draft** when a section of an application whose registry toolchain is
`java-ant` is in scope. Such a section changes the Java sources of one application directory,
built by that directory's `build.xml`.

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
6. **Dependencies.** No new third-party jar unless the section names it: the jar file, the
   `lib` directory it goes in, and why a class already on the classpath does not serve.
7. **Strings.** Every user-visible string is fixed, exact text, and comes from the
   application's `i18n/` bundle. The section gives the key and the text for every locale file
   the bundle has.
8. **Protocol.** A key, command or parameter name both ends of the wire use is defined in
   `wubiq-common`, in a `COM-k` section. The consuming section depends on it and cites the
   constant; it never repeats the literal.
9. **Tests.** JUnit 4. The suites, and where a section's tests go:

   | Application | Test sources | Suite |
   |---|---|---|
   | `WIT` | `wubiq-dotmatrix-witness/tests-src` | yes |
   | `CLI` | `wubiq-client/tests-src` | yes |
   | `COM` | `wubiq-client/tests-src`, when the changed code is reachable from a client test | none of its own |
   | `SRV`, `INS`, `PJB` | — | none |

   A test is headless (`java.awt.headless=true`), needs no printer, device, server or network,
   never sleeps, and never asserts a version string. A test added to `wubiq-client/tests-src`
   for a `COM-k` section is that section's one exception to rule 1.
10. **Dot-matrix output.** `ant test-dotmatrix` only reports layout and golden-file mismatches
    unless `-Dwubiq.dotmatrix.strict=true` is set. An AC about what is printed names the JUnit
    assertion that fails, never the report.
11. An application with a suite: at least three ACs, at least one of them an error path (a
    missing printer, an unreadable stream, an unsupported option surfacing loudly). An
    application with none: every AC a test cannot reach ends
    `(mechanical — no red test; verified by <command>)`.

## Template

```markdown
<!-- section:<APP>-k:start -->
### <APP>-k — <application directory> · <capability>
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
2. <i18n keys, with the text for every locale file>

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
<!-- section:<APP>-k:end -->
```
