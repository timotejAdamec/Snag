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

# SLF4J service provider
-keep class * implements org.slf4j.spi.SLF4JServiceProvider { *; }

# --- General ---
-dontwarn kotlin.**
-dontwarn org.koin.**
