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

# --- ServiceLoader preservation (META-INF/services) ---
-adaptresourcefilecontents META-INF/services/**
-keepdirectories META-INF/services

# Ktor HTTP client engine
-keep class io.ktor.client.engine.HttpClientEngineContainer { *; }
-keep class io.ktor.client.engine.okhttp.OkHttpEngineContainer { *; }
-keep class io.ktor.client.engine.okhttp.OkHttp { *; }

# Ktor content negotiation / kotlinx.serialization ServiceLoader providers
-keep class io.ktor.serialization.kotlinx.KotlinxSerializationExtensionProvider { *; }
-keep class io.ktor.serialization.kotlinx.json.KotlinxSerializationJsonExtensionProvider { *; }
-keep class * implements io.ktor.serialization.kotlinx.KotlinxSerializationExtensionProvider { *; }

# kotlinx-coroutines Swing main dispatcher
-keep class * implements kotlinx.coroutines.internal.MainDispatcherFactory { *; }

# SQLDelight JDBC driver
-keep class org.sqlite.** { *; }
-keep class * implements java.sql.Driver { *; }

# OkHttp + okio: ProGuard's optimizer may specialize Okio.buffer(Sink) into a
# variant whose declared return type RealBufferedSink mismatches the actual
# returned BufferedSink, producing a JVM VerifyError on first network IO.
# Pin these libraries from optimization to keep their bytecode signatures intact.
-keep class okio.** { *; }
-keepclassmembers class okio.** { *; }
-keep class okhttp3.** { *; }
-keepclassmembers class okhttp3.** { *; }
-dontwarn okio.**
-dontwarn okhttp3.**

# SLF4J service provider
-keep class * implements org.slf4j.spi.SLF4JServiceProvider { *; }

# Coil 3 image loader: ServiceLoader targets, fetchers and decoders rely on
# reflection / generic type info; minification can strip them silently and
# break image loading in release builds.
-keep class coil3.** { *; }
-keepclassmembers class coil3.** { *; }
-dontwarn coil3.**
-keep class * implements coil3.util.ServiceLoaderComponentRegistry$Registrar { *; }
-keep class com.github.panpf.zoomimage.** { *; }
-dontwarn com.github.panpf.zoomimage.**

# --- General ---
-dontwarn kotlin.**
-dontwarn org.koin.**
