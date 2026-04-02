#
# Copyright (c) 2026 Timotej Adamec
# SPDX-License-Identifier: MIT
#

# --- OkHttp (optional platform providers not present on desktop) ---
-dontwarn okhttp3.internal.graal.**
-dontwarn okhttp3.internal.platform.BouncyCastlePlatform
-dontwarn okhttp3.internal.platform.ConscryptPlatform**
-dontwarn okhttp3.internal.platform.OpenJSSEPlatform
-dontwarn org.bouncycastle.**
-dontwarn org.conscrypt.**
-dontwarn org.openjsse.**
-dontwarn org.graalvm.**
-dontwarn com.oracle.svm.**

# --- Ktor ---
-dontwarn io.ktor.**

# --- General ---
-dontwarn kotlin.**
-dontwarn org.koin.**
