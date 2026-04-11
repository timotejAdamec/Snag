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

import cz.adamec.timotej.snag.buildsrc.configuration.architecture.AppModule
import cz.adamec.timotej.snag.buildsrc.configuration.architecture.CoreModule
import cz.adamec.timotej.snag.buildsrc.configuration.architecture.Encapsulation
import cz.adamec.timotej.snag.buildsrc.configuration.architecture.FeatModule
import cz.adamec.timotej.snag.buildsrc.configuration.architecture.FeaturesSharedModule
import cz.adamec.timotej.snag.buildsrc.configuration.architecture.HexLayer
import cz.adamec.timotej.snag.buildsrc.configuration.architecture.InfraModule
import cz.adamec.timotej.snag.buildsrc.configuration.architecture.LibModule
import cz.adamec.timotej.snag.buildsrc.configuration.architecture.ModuleIdentity
import cz.adamec.timotej.snag.buildsrc.configuration.architecture.Platform
import cz.adamec.timotej.snag.buildsrc.configuration.architecture.parseModulePath

/**
 * Canonical Snag convention plugin IDs in **ascending specificity order**: the entries earlier
 * in the list are more general, later entries are more specific. Snag's convention plugins
 * stack — e.g. `snag.driving.frontend.multiplatform.module` internally applies
 * `snag.frontend.multiplatform.module` which in turn applies `snag.multiplatform.module` — so
 * `pluginManager.hasPlugin(...)` reports multiple matches for a single module. The row builder
 * picks the most specific match (the last entry in this list that is also applied) as the
 * `plugin_applied` cell.
 */
internal val CANONICAL_SNAG_PLUGIN_IDS = listOf(
    "libs.plugins.snag.multiplatform.module",
    "libs.plugins.snag.frontend.multiplatform.module",
    "libs.plugins.snag.driving.frontend.multiplatform.module",
    "libs.plugins.snag.network.frontend.multiplatform.module",
    "libs.plugins.snag.driven.frontend.multiplatform.module",
    "libs.plugins.snag.backend.module",
    "libs.plugins.snag.driven.backend.module",
    "libs.plugins.snag.impl.driving.backend.module",
    "libs.plugins.snag.contract.driving.backend.multiplatform.module",
)

internal data class SourceSetDir(
    val name: String,
    val absolutePath: String,
) : java.io.Serializable

internal data class SharingReportRow(
    val modulePath: String,
    val category: String,
    val feature: String,
    val platform: String,
    val hexLayer: String,
    val encapsulation: String,
    val pluginApplied: String,
    val sourceSet: String,
    val sourceSetDir: String,
    val platformSet: String,
)

internal object SharingReportRowBuilder {

    /**
     * Build one row per existing source-set directory for a single Gradle subproject.
     *
     * @param modulePath the Gradle project path (e.g. `:feat:projects:fe:business:model`).
     * @param appliedPluginIds **all** plugin IDs applied to the module — both canonical Snag
     *        convention plugins and auxiliary marker plugins such as `com.android.application`.
     *        The builder filters this list twice: once to pick the most-specific Snag plugin
     *        for the `plugin_applied` column, and once to derive the plugin family used in
     *        `platform_set` computation.
     * @param sourceSetDirs the on-disk source-set directories for the module.
     */
    fun buildRows(
        modulePath: String,
        appliedPluginIds: List<String>,
        sourceSetDirs: List<SourceSetDir>,
    ): List<SharingReportRow> {
        if (sourceSetDirs.isEmpty()) return emptyList()

        val classification = parseModulePath(modulePath).toClassification()
        val pluginApplied = resolveSnagPluginApplied(appliedPluginIds)
        val pluginFamily = pluginFamilyOf(appliedPluginIds)

        return sourceSetDirs.map { dir ->
            SharingReportRow(
                modulePath = modulePath,
                category = classification.category,
                feature = classification.feature,
                platform = classification.platform,
                hexLayer = classification.hexLayer,
                encapsulation = classification.encapsulation,
                pluginApplied = pluginApplied,
                sourceSet = dir.name,
                sourceSetDir = dir.absolutePath,
                platformSet = platformSetLabel(
                    pluginFamily = pluginFamily,
                    modulePlatform = classification.platform,
                    sourceSet = dir.name,
                ),
            )
        }
    }

    private fun resolveSnagPluginApplied(appliedPluginIds: List<String>): String {
        val applied = appliedPluginIds.toSet()
        return CANONICAL_SNAG_PLUGIN_IDS.lastOrNull { it in applied } ?: ""
    }
}

private data class Classification(
    val category: String,
    val feature: String,
    val platform: String,
    val hexLayer: String,
    val encapsulation: String,
)

private fun ModuleIdentity.toClassification(): Classification = when (this) {
    is CoreModule -> Classification(
        category = "core",
        feature = "",
        platform = platform.asCsv(),
        hexLayer = "",
        encapsulation = encapsulation.asCsv(),
    )
    is LibModule -> Classification(
        category = "lib",
        feature = "",
        platform = platform.asCsv(),
        hexLayer = "",
        encapsulation = encapsulation.asCsv(),
    )
    is FeatModule -> Classification(
        category = "feat",
        feature = feature,
        platform = platform.asCsv(),
        hexLayer = hexLayer.asCsv(),
        encapsulation = encapsulation.asCsv(),
    )
    is FeaturesSharedModule -> Classification(
        category = "featuresShared",
        feature = feature,
        platform = platform.asCsv(),
        hexLayer = hexLayer.asCsv(),
        encapsulation = encapsulation.asCsv(),
    )
    is AppModule -> Classification(
        category = "app",
        feature = "",
        platform = "",
        hexLayer = "",
        encapsulation = "",
    )
    is InfraModule -> Classification(
        category = "infra",
        feature = "",
        platform = "",
        hexLayer = "",
        encapsulation = "",
    )
}

private fun Platform?.asCsv(): String = when (this) {
    Platform.FE -> "fe"
    Platform.BE -> "be"
    Platform.COMMON -> "common"
    null -> ""
}

private fun HexLayer?.asCsv(): String = when (this) {
    HexLayer.BUSINESS -> "business"
    HexLayer.APP -> "app"
    HexLayer.DRIVING -> "driving"
    HexLayer.DRIVEN -> "driven"
    HexLayer.PORTS -> "ports"
    null -> ""
}

private fun Encapsulation?.asCsv(): String = when (this) {
    Encapsulation.API -> "api"
    Encapsulation.IMPL -> "impl"
    Encapsulation.TEST -> "test"
    Encapsulation.CONTRACT -> "contract"
    null -> ""
}
