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

/**
 * Platform-reach analysis for the sharing report.
 *
 * The naive source-set name (e.g. `commonMain`) is ambiguous: it reaches a different set of
 * platforms depending on which convention plugin the module applies. `commonMain` of a
 * full-platform KMP module (applying `snag.multiplatform.module` or its contract variant)
 * compiles into all six platforms — android, ios, jvm-desktop, js, wasmJs, AND the JVM
 * backend via transitive dependency — while `commonMain` of a frontend-only KMP module
 * (applying any of the `snag.(driving|driven|network).frontend.multiplatform.module` family
 * or the base `snag.frontend.multiplatform.module`) compiles only into the five frontends
 * because the category rules forbid the backend from depending on frontend modules.
 *
 * This file derives a **platform-set label** from `(plugin_applied, source_set)` that collapses
 * the (module × source set) pair into a stable "reach class". The label is the primary
 * aggregation axis in §4.2 of the thesis: two rows with the same platform-set label reach
 * exactly the same set of platforms and can be summed.
 *
 * The label vocabulary is:
 *
 * | Label              | Reaches                                                              | Size |
 * |--------------------|----------------------------------------------------------------------|------|
 * | `all`              | android + ios + jvm_desktop + js + wasmJs + jvm_backend              | 6    |
 * | `frontend`         | android + ios + jvm_desktop + js + wasmJs                            | 5    |
 * | `nonAndroid_shared`| ios + jvm_desktop + js + wasmJs + jvm_backend                        | 5    |
 * | `nonWeb_shared`    | android + ios + jvm_desktop + jvm_backend                            | 4    |
 * | `nonAndroid_fe`    | ios + jvm_desktop + js + wasmJs                                      | 4    |
 * | `nonJvm`           | android + ios + js + wasmJs                                          | 4    |
 * | `nonWeb_fe`        | android + ios + jvm_desktop                                          | 3    |
 * | `mobile`           | android + ios                                                        | 2    |
 * | `web`              | js + wasmJs                                                          | 2    |
 * | `jvm_shared`       | jvm_desktop + jvm_backend                                            | 2    |
 * | `backend`          | jvm_backend                                                          | 1    |
 * | `jvm_desktop`      | jvm_desktop                                                          | 1    |
 * | `android`          | android                                                              | 1    |
 * | `ios`              | ios                                                                  | 1    |
 * | `js`               | js                                                                   | 1    |
 * | `wasmJs`           | wasmJs                                                                | 1    |
 *
 * Test source sets and otherwise-unclassified rows get an empty platform-set label and are
 * excluded from the primary reach aggregation.
 */

internal enum class PluginFamily {
    /** Full-platform KMP — compiles to all 6 platforms including the JVM backend. */
    FULL,

    /** Frontend-only KMP — compiles to the 5 frontend platforms, never the backend. */
    FRONTEND,

    /** Plain Kotlin-JVM backend — compiles only to the JVM backend. */
    BACKEND,

    /** Android application — reaches only android. */
    ANDROID_APP,

    /** No recognizable plugin family; platform reach is unknown. */
    UNKNOWN,
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

/**
 * Marker plugins probed in addition to the canonical Snag plugins, used to classify app
 * modules that don't apply a Snag convention plugin (e.g. `:androidApp` applying only
 * `com.android.application`). Order is irrelevant — membership is enough.
 */
internal val PLATFORM_MARKER_PLUGIN_IDS = listOf(
    "com.android.application",
)

internal fun pluginFamilyOf(appliedPluginIds: Collection<String>): PluginFamily {
    val applied = appliedPluginIds.toSet()

    // Snag convention plugins stack — e.g. applying `snag.driving.frontend.multiplatform.module`
    // also transitively applies `snag.frontend.multiplatform.module` and
    // `snag.multiplatform.module`. A naïve `any { it in FULL_PLATFORM_SNAG_PLUGINS }` check
    // would therefore classify every frontend module as FULL because the base multiplatform
    // plugin is always present. The correct family is the one associated with the MOST
    // SPECIFIC Snag plugin applied — the last entry in `CANONICAL_SNAG_PLUGIN_IDS` that is
    // also in the applied set.
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

/**
 * Platform-set labels in ascending reach order (most specific → most shared). The heatmap
 * in §4.2 uses the reversed order so the most-shared bucket lands on the left of the figure.
 */
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

/**
 * Compute the platform-set label for a module's source set.
 *
 * The reach of a (module × source set) pair is determined by two factors:
 *
 * 1. **What the KMP plugin compiles** — the plugin family (full / frontend / backend) defines
 *    which target binaries the source set contributes to.
 *
 * 2. **What the architectural rules allow consumers to be** — Snag's platform-direction rule
 *    forbids `be` modules from depending on `fe` modules and vice versa. A module whose path
 *    carries `platform=fe` is architecturally scoped to the frontend *regardless* of which
 *    plugin it applies, because no backend module can legally depend on it. The same holds
 *    symmetrically for `be` modules.
 *
 * We encode this by combining the path-derived `modulePlatform` ("fe", "be", "common", or
 * blank for app/infra/uncategorized modules) with the plugin family into an **effective
 * family** that is then mapped to a concrete reach label. When `modulePlatform` forces a
 * narrower scope than the plugin would suggest (e.g. a `:lib:X:fe:api` module applying the
 * full-platform base plugin), the path wins: its jvmMain reach collapses from `jvm_shared`
 * to `jvm_desktop`, because although the KMP plugin compiles a JVM artifact, the category
 * rules mean only the frontend's JVM desktop target can actually consume it.
 *
 * Returns an empty string for test source sets and unknown combinations — the caller treats
 * an empty label as "exclude from reach aggregation".
 */
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
