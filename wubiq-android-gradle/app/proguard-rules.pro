# Project specific R8 rules. The default proguard-android-optimize.txt plus the
# consumer rules shipped with the AndroidX / vendor libraries cover the rest.
#
# Keep rules are deliberately narrow: Google Play requires most of the DEX to be
# obfuscated, so do not add blanket -keep rules for whole libraries.

# Keep line numbers in crash stack traces; mapping.txt is uploaded with the bundle.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

