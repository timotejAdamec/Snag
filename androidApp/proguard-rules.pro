#
# Copyright (c) 2026 Timotej Adamec
# SPDX-License-Identifier: MIT
#
# This file is part of the thesis:
# "Multiplatform snagging system with code sharing maximisation"
#
# Czech Technical University in Prague
# Faculty of Information Technology
# Department of Software Engineering
#

# --- kotlinx.serialization ---
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

-keep,includedescriptorclasses class cz.adamec.timotej.snag.**$$serializer { *; }
-keepclassmembers class cz.adamec.timotej.snag.** {
    *** Companion;
}
-keepclasseswithmembers class cz.adamec.timotej.snag.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# --- Ktor ---
-keep class io.ktor.** { *; }
-dontwarn io.ktor.**

# --- Koin ---
-keep class org.koin.** { *; }
-dontwarn org.koin.**

# --- OkHttp ---
-dontwarn okhttp3.**
-dontwarn okio.**

# --- General Kotlin ---
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.**
