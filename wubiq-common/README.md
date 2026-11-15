## wubiq-common

This is the wubiq-common module. It contains all the common code to other parts of the project.

### Tests

Unit tests live in `tests-src` and use JUnit 4. A test class is named `*Test`.

```
ant test
```

compiles the sources and the tests with JDK 8 and runs every `*Test` class. The JUnit jars come from `../wubiq-dotmatrix-witness/lib-test`.
