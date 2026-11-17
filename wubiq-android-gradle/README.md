## wubiq-android-gradle

This is a the android module. Changes to android mobile apps are to be performed here.

### Shared classes

The app does not depend on wubiq-common or wubiq-client. The classes it shares with the server are app sources, under `app/src/main/java/net/sf/wubiq/` in the packages `android` (`Mobile*`), `clients`, `common`, `enums`, `exceptions` and `utils`; they log through `java.util.logging`.

Two things must agree with wubiq-server, and are changed in both places in the same change:
- the device table, `android/MobileDevices.java`: the app sends only the device name, and the server looks the name up in the copy in `wubiq-common`;
- the constants of `common/CommandKeys.java`, `common/ParameterKeys.java` and `common/DirectConnectKeys.java`, and the order of `enums/DirectConnectCommand.java`: they are the wire protocol.

Everything else in those packages belongs to the app.
