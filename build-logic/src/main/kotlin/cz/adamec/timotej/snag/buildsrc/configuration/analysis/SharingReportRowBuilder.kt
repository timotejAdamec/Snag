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
import cz.adamec.timotej.snag.buildsrc.configuration.architecture.FeatSharedModule
import cz.adamec.timotej.snag.buildsrc.configuration.architecture.HexLayer
import cz.adamec.timotej.snag.buildsrc.configuration.architecture.SingleFeatModule
import cz.adamec.timotej.snag.buildsrc.configuration.architecture.InfraModule
import cz.adamec.timotej.snag.buildsrc.configuration.architecture.LibModule
import cz.adamec.timotej.snag.buildsrc.configuration.architecture.ModuleIdentity
import cz.adamec.timotej.snag.buildsrc.configuration.architecture.Platform
import cz.adamec.timotej.snag.buildsrc.configuration.architecture.parseModulePath

// Ordered general → specific. Snag plugins stack, so `lastOrNull { applied }` picks the most
// specific match for the `plugin_applied` cell.
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
    // Repo-relative, forward-slash separators. Used by `analysis/feature_retro.py` for the
    // path→unit longest-prefix mapping so the Python side does not have to reimplement the
    // module path grammar.
    val relativePath: String,
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
    val sourceSetDirRel: String,
    val platformSet: String,
)

internal object SharingReportRowBuilder {

    // `appliedPluginIds` must include both Snag convention plugins and marker plugins
    // (e.g. `com.android.application`) — both are probed during row construction.
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
                sourceSetDirRel = dir.relativePath,
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
    is SingleFeatModule -> Classification(
        category = "feat",
        feature = feature,
        platform = platform.asCsv(),
        hexLayer = hexLayer.asCsv(),
        encapsulation = encapsulation.asCsv(),
    )
    is FeatSharedModule -> Classification(
        category = "featShared",
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
