/*
 * Copyright (c) 2026 Timotej Adamec
 * SPDX-License-Identifier: MIT
 *
 * This file is part of the thesis:
 * "Multiplatform snagging system with code sharing maximisation"
 *
 * Czech Technical University in Prague
 * Faculty of Information Technology
 * Department of Software Engineering
 */

package cz.adamec.timotej.snag.buildsrc.configuration.analysis

// Derives a platform-set label from (plugin_applied, source_set). Source-set names alone are
// ambiguous — `commonMain` of a full-platform KMP module reaches 6 platforms, `commonMain` of a
// frontend-only module reaches 5. The label is the primary aggregation axis in §4.2.

internal enum class PluginFamily {
    FULL, FRONTEND, BACKEND, ANDROID_APP, UNKNOWN,
}

private val FULL_PLATFORM_SNAG_PLUGINS = setOf(
    "libs.plugins.snag.multiplatform.module",
    "libs.plugins.snag.contract.driving.backend.multiplatform.module",
)

private val FRONTEND_SNAG_PLUGINS = setOf(
    "libs.plugins.snag.frontend.multiplatform.module",
    "libs.plugins.snag.driving.frontend.multiplatform.module",
    "libs.plugins.snag.network.frontend.multiplatform.module",
    "libs.plugins.snag.driven.frontend.multiplatform.module",
)

private val BACKEND_SNAG_PLUGINS = setOf(
    "libs.plugins.snag.backend.module",
    "libs.plugins.snag.driven.backend.module",
    "libs.plugins.snag.impl.driving.backend.module",
)

// Probed alongside canonical Snag plugins to classify app modules that apply no Snag plugin.
internal val PLATFORM_MARKER_PLUGIN_IDS = listOf(
    "com.android.application",
)

internal fun pluginFamilyOf(appliedPluginIds: Collection<String>): PluginFamily {
    val applied = appliedPluginIds.toSet()

    // Snag plugins stack (a specific plugin transitively applies more general ones), so pick
    // the most specific — the last entry in CANONICAL_SNAG_PLUGIN_IDS that is applied.
    val mostSpecificSnagPlugin = CANONICAL_SNAG_PLUGIN_IDS.lastOrNull { it in applied }
    if (mostSpecificSnagPlugin != null) {
        return when (mostSpecificSnagPlugin) {
            in FULL_PLATFORM_SNAG_PLUGINS -> PluginFamily.FULL
            in FRONTEND_SNAG_PLUGINS -> PluginFamily.FRONTEND
            in BACKEND_SNAG_PLUGINS -> PluginFamily.BACKEND
            else -> PluginFamily.UNKNOWN
        }
    }

    return when {
        "com.android.application" in applied -> PluginFamily.ANDROID_APP
        else -> PluginFamily.UNKNOWN
    }
}

// Ascending reach (1-platform → 6-platform). §4.2 heatmap uses the reverse.
internal val PLATFORM_SET_ASCENDING_REACH = listOf(
    // 1-platform
    "backend",
    "jvm_desktop",
    "android",
    "ios",
    "js",
    "wasmJs",
    // 2-platform
    "mobile",
    "web",
    "jvm_shared",
    // 3-platform
    "nonWeb_fe",
    // 4-platform
    "nonWeb_shared",
    "nonAndroid_fe",
    "nonJvm",
    // 5-platform
    "frontend",
    "nonAndroid_shared",
    // 6-platform
    "all",
)

// `modulePlatform` (from path) overrides `pluginFamily` when narrower: a :fe: module can only
// be consumed by frontends even if its plugin compiles a JVM artifact. Returns "" for test
// source sets and unknown combinations — caller excludes empty labels from reach aggregation.
internal fun platformSetLabel(
    pluginFamily: PluginFamily,
    modulePlatform: String,
    sourceSet: String,
): String {
    val effectiveFamily = when (modulePlatform) {
        "fe" -> PluginFamily.FRONTEND
        "be" -> PluginFamily.BACKEND
        else -> pluginFamily
    }
    return effectiveFamily.labelForSourceSet(sourceSet)
}

private fun PluginFamily.labelForSourceSet(sourceSet: String): String = when (this) {
    PluginFamily.FULL -> when (sourceSet) {
        "commonMain" -> "all"
        "nonWebMain" -> "nonWeb_shared"
        "mobileMain" -> "mobile"
        "nonAndroidMain" -> "nonAndroid_shared"
        "nonJvmMain" -> "nonJvm"
        "webMain" -> "web"
        "androidMain" -> "android"
        "iosMain" -> "ios"
        "jvmMain" -> "jvm_shared"
        "jsMain" -> "js"
        "wasmJsMain" -> "wasmJs"
        else -> ""
    }
    PluginFamily.FRONTEND -> when (sourceSet) {
        "commonMain" -> "frontend"
        "nonWebMain" -> "nonWeb_fe"
        "mobileMain" -> "mobile"
        "nonAndroidMain" -> "nonAndroid_fe"
        "nonJvmMain" -> "nonJvm"
        "webMain" -> "web"
        "androidMain" -> "android"
        "iosMain" -> "ios"
        "jvmMain" -> "jvm_desktop"
        "jsMain" -> "js"
        "wasmJsMain" -> "wasmJs"
        else -> ""
    }
    PluginFamily.BACKEND -> when (sourceSet) {
        "main" -> "backend"
        else -> ""
    }
    PluginFamily.ANDROID_APP -> when (sourceSet) {
        "main" -> "android"
        else -> ""
    }
    PluginFamily.UNKNOWN -> ""
}
