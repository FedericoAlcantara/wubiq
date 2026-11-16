## wubiq-common

This is the wubiq-common module. It contains all the common code to other parts of the project.

### Language level

wubiq-common is compiled for Java 8 (`release="8"` in `build.xml`). wubiq-server loads it on Java 8 and the packaged client loads it on its bundled Java 25. Code in this module uses Java 8 syntax and classes only.

### Mobile device table

`net.sf.wubiq.android.MobileDevices` is the device table wubiq-server reads. The Android app has its own copy in `wubiq-android-gradle/app/src/main/java/net/sf/wubiq/android/MobileDevices.java`. Add a device to both files in the same change, with the same name: the app sends only the device name.

The constants in `CommandKeys`, `ParameterKeys` and `DirectConnectKeys` are the wire protocol. The app has its own copy of them too; change a constant in both places.

### Tests

Unit tests live in `tests-src` and use JUnit 4. A test class is named `*Test`.

```
ant test
```

compiles the sources and the tests with JDK 8 and runs every `*Test` class. The JUnit jars come from `../wubiq-dotmatrix-witness/lib-test`.
