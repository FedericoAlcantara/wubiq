# improve-issue · Android (Gradle) application sections

Read by `improve-issue` **Draft** when a section of an application whose registry toolchain is
`android-gradle` is in scope. Such a section changes the Android app under
`wubiq-android-gradle/app/`: Java sources, resources, the manifest, `build.gradle.kts`.

## Rules

1. One application per section. Every file the section edits sits under
   `wubiq-android-gradle/`.
2. Subsection order is fixed, and is the TDD order: acceptance criteria → test ports → red
   tests → code changes → resource and manifest changes → green tests.
3. Every test port carries its in and out shapes, and is marked `existing` (with its citation)
   or `new` (with its search evidence).
4. **Language and API level.** `app/build.gradle.kts` decides: `compileOptions` gives the Java
   source level (Java 8), `minSdk` (28) the oldest API a call may assume, `targetSdk` (37) the
   behaviour the platform applies. The section states the values it read. A call above
   `minSdk` carries its `Build.VERSION.SDK_INT` guard and the path taken below it.
5. **Tracked libraries.** `app/libs/*.jar` and `*.aar` are never edited, replaced or rebuilt.
   The app consumes `wubiq-common` and `wubiq-client` through those jars, so a section never
   relies on common or client code that is not already in them.
6. **Dependencies.** No new Gradle dependency unless the section names it: coordinates,
   version, and why the platform or a library already declared does not serve.
7. **Versions.** `versionCode` and `versionName` are never edited by a section.
8. **Strings.** Every user-visible string is fixed, exact text, and is a string resource. The
   section gives the name and the text for every `res/values*/strings.xml` the app has.
9. **Release builds.** R8 minifies and obfuscates them. A class, method or field reached by
   reflection, by name from a library, or from the manifest gets a keep rule; the section names
   the rule and the ProGuard file it goes in.
10. **Permissions.** A new manifest permission states the API levels it applies to, whether it
    is requested at runtime, and what the app does when it is denied.
11. **Tests.** JUnit 4 JVM unit tests under `app/src/test/java` only — no instrumentation test,
    no device, no emulator. Logic that needs an Android class is moved behind a plain Java
    method the test calls; the section names that method. A test needs no printer or network,
    never sleeps, and never asserts a version string.
12. At least three ACs, at least one of them an error path (a denied permission, a printer that
    does not answer, a malformed setting). An AC only a device can show ends
    `(manual — no red test; verified by <the steps on a device>)`.

## Template

```markdown
<!-- section:AND-k:start -->
### AND-k — wubiq-android-gradle · <capability>
Depends on: <ids>

#### Documentation references
<the README § sections specifying this behaviour>

#### Implementation details
Levels: Java <source level> · minSdk <n> · targetSdk <n>

##### Acceptance criteria
- [ ] AC-1: Given <state or input>, when `<Class>#<method>` <does what>, then <observable result>

##### Test ports
- `<fully.qualified.Class>#<method>(<parameter types>)` ·
  in: <shape> · out: <shape> · existing (`<path>.java`, `Class#method`) | new

##### Red tests (write first; each must fail before implementation)
| AC | Placement | Setup | Test file | Test name | Assertion sketch |
|---|---|---|---|---|---|
| AC-1 | amend \| add to \| new file | <values built in the test> | wubiq-android-gradle/app/src/test/java/<package path>/<Class>Test.java | `<methodName>` | `assertEquals(<expected>, <actual>)` |

##### Code changes
1. `wubiq-android-gradle/app/src/main/java/<package path>/<Class>.java` `<Class>#<method>` —
   <the exact change>

##### Resource and manifest changes   (only when the section has any)
1. `app/src/main/res/values*/strings.xml` — `<name>`: <the text, per locale file>
2. `app/src/main/AndroidManifest.xml` — <the exact element>
3. `<ProGuard file>` — <the keep rule>

##### Green tests
1. The red tests above pass, unmodified (code-issue runs these, targeted:
   `.claude/scripts/section-test.sh AND <Class or Class.method filter>`)
2. `.claude/scripts/suite.sh` — full suite green (review-issue runs this; code-issue runs only
   this section's test classes)
3. `.claude/scripts/gate.sh` clean
<!-- section:AND-k:end -->
```
