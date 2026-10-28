# Project specific R8 rules. The default proguard-android-optimize.txt plus the
# consumer rules shipped with the AndroidX / vendor libraries cover the rest.
#
# Keep rules are deliberately narrow: Google Play requires most of the DEX to be
# obfuscated, so do not add blanket -keep rules for whole libraries.

# Keep line numbers in crash stack traces; mapping.txt is uploaded with the bundle.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# wubiq-client.jar bundles commons-logging 1.1.1, compiled for Java 1.1: its class literals
# and its factory / log adapter discovery all use Class.forName, so it must not be renamed.
# The adapters for log4j, Avalon, LogKit and servlets are never selected on Android.
-keep class org.apache.commons.logging.** { *; }
-dontwarn org.apache.log4j.**
-dontwarn org.apache.avalon.framework.logger.**
-dontwarn org.apache.log.**
-dontwarn javax.servlet.**

# wubiq-client.jar: IOUtils.resetInputStream only logs through commons-lang3, inside a
# catch block that ByteArrayInputStream.reset() never reaches. commons-lang3 is not bundled.
-dontwarn org.apache.commons.lang3.exception.ExceptionUtils
