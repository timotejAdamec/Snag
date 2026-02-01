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

package cz.adamec.timotej.snag.structures.fe.driving.impl.internal.structureDetails.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneStrategy
import androidx.navigation3.scene.SceneStrategyScope
import androidx.window.core.layout.WindowSizeClass
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_EXPANDED_LOWER_BOUND
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_MEDIUM_LOWER_BOUND
import cz.adamec.timotej.snag.feat.findings.fe.driving.api.FindingsSceneMetadata
import cz.adamec.timotej.snag.lib.navigation.fe.SnagNavRoute

internal class StructureFloorPlanSceneStrategy(
    private val windowSizeClass: WindowSizeClass,
) : SceneStrategy<SnagNavRoute> {
    override fun SceneStrategyScope<SnagNavRoute>.calculateScene(entries: List<NavEntry<SnagNavRoute>>): Scene<SnagNavRoute>? {
        val listEntry =
            entries.findLast {
                it.metadata.containsKey(FindingsSceneMetadata.FINDINGS_LIST_KEY)
            }
        val detailEntry =
            entries.findLast {
                it.metadata.containsKey(FindingsSceneMetadata.FINDING_DETAIL_KEY)
            }

        if (listEntry == null && detailEntry == null) return null

        val firstFindingsIndex =
            entries.indexOfFirst {
                it.metadata.containsKey(FindingsSceneMetadata.FINDINGS_LIST_KEY) ||
                    it.metadata.containsKey(FindingsSceneMetadata.FINDING_DETAIL_KEY)
            }
        if (firstFindingsIndex < 1) return null

        val hostEntry = entries[firstFindingsIndex - 1]
        val findingsPanel = detailEntry ?: listEntry ?: return null

        val isExpanded =
            windowSizeClass.isWidthAtLeastBreakpoint(WIDTH_DP_EXPANDED_LOWER_BOUND)
        val isMedium =
            windowSizeClass.isWidthAtLeastBreakpoint(WIDTH_DP_MEDIUM_LOWER_BOUND)

        val allSceneEntries =
            buildList {
                add(hostEntry)
                if (listEntry != null) add(listEntry)
                if (detailEntry != null) add(detailEntry)
            }
        val previousEntries = entries.take(firstFindingsIndex)

        return when {
            isExpanded && listEntry != null && detailEntry != null ->
                ThreePaneScene(
                    key = hostEntry.contentKey,
                    entries = allSceneEntries,
                    previousEntries = previousEntries,
                    listEntry = listEntry,
                    hostEntry = hostEntry,
                    detailEntry = detailEntry,
                )

            isMedium ->
                TwoPaneScene(
                    key = hostEntry.contentKey,
                    entries = allSceneEntries,
                    previousEntries = previousEntries,
                    hostEntry = hostEntry,
                    panelEntry = findingsPanel,
                )

            else ->
                BottomSheetScene(
                    key = hostEntry.contentKey,
                    entries = allSceneEntries,
                    previousEntries = previousEntries,
                    hostEntry = hostEntry,
                    sheetEntry = findingsPanel,
                )
        }
    }
}

private class ThreePaneScene(
    override val key: Any,
    override val entries: List<NavEntry<SnagNavRoute>>,
    override val previousEntries: List<NavEntry<SnagNavRoute>>,
    private val listEntry: NavEntry<SnagNavRoute>,
    private val hostEntry: NavEntry<SnagNavRoute>,
    private val detailEntry: NavEntry<SnagNavRoute>,
) : Scene<SnagNavRoute> {
    override val content: @Composable () -> Unit = {
        Row(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.weight(0.5f)) {
                hostEntry.Content()
            }
            Column(modifier = Modifier.weight(0.25f)) {
                listEntry.Content()
            }
            Column(modifier = Modifier.weight(0.25f)) {
                detailEntry.Content()
            }
        }
    }
}

private class TwoPaneScene(
    override val key: Any,
    override val entries: List<NavEntry<SnagNavRoute>>,
    override val previousEntries: List<NavEntry<SnagNavRoute>>,
    private val hostEntry: NavEntry<SnagNavRoute>,
    private val panelEntry: NavEntry<SnagNavRoute>,
) : Scene<SnagNavRoute> {
    override val content: @Composable () -> Unit = {
        Row(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.weight(0.5f)) {
                hostEntry.Content()
            }
            Column(modifier = Modifier.weight(0.5f)) {
                panelEntry.Content()
            }
        }
    }
}

private class BottomSheetScene(
    override val key: Any,
    override val entries: List<NavEntry<SnagNavRoute>>,
    override val previousEntries: List<NavEntry<SnagNavRoute>>,
    private val hostEntry: NavEntry<SnagNavRoute>,
    private val sheetEntry: NavEntry<SnagNavRoute>,
) : Scene<SnagNavRoute> {
    override val content: @Composable () -> Unit = {
        val scaffoldState =
            rememberBottomSheetScaffoldState(
                bottomSheetState =
                    rememberStandardBottomSheetState(
                        initialValue = SheetValue.PartiallyExpanded,
                    ),
            )
        BottomSheetScaffold(
            scaffoldState = scaffoldState,
            sheetPeekHeight = 128.dp,
            sheetContent = {
                sheetEntry.Content()
            },
        ) {
            hostEntry.Content()
        }
    }
}
