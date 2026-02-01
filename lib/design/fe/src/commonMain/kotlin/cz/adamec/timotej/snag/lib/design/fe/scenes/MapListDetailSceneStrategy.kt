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

package cz.adamec.timotej.snag.lib.design.fe.scenes

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

class MapListDetailSceneStrategy<T : Any>(
    private val windowSizeClass: WindowSizeClass,
) : SceneStrategy<T> {
    override fun SceneStrategyScope<T>.calculateScene(
        entries: List<NavEntry<T>>,
    ): Scene<T>? {
        val mapEntry = entries.findLast {
            it.metadata.containsKey(MapListDetailSceneMetadata.MAP_KEY)
        }
        val listEntry =
            entries.findLast {
                it.metadata.containsKey(MapListDetailSceneMetadata.LIST_KEY)
            }
        val detailEntry =
            entries.findLast {
                it.metadata.containsKey(MapListDetailSceneMetadata.DETAIL_KEY)
            }

        if (mapEntry == null || (listEntry == null && detailEntry == null)) return null

        val firstFindingsIndex =
            entries.indexOfFirst {
                it.metadata.containsKey(MapListDetailSceneMetadata.LIST_KEY) ||
                        it.metadata.containsKey(MapListDetailSceneMetadata.DETAIL_KEY)
            }
        if (firstFindingsIndex < 1) return null

        val findingsPanel = detailEntry ?: listEntry ?: return null

        val isExpanded =
            windowSizeClass.isWidthAtLeastBreakpoint(WIDTH_DP_EXPANDED_LOWER_BOUND)
        val isMedium =
            windowSizeClass.isWidthAtLeastBreakpoint(WIDTH_DP_MEDIUM_LOWER_BOUND)

        val allSceneEntries =
            buildList {
                add(mapEntry)
                if (listEntry != null) add(listEntry)
                if (detailEntry != null) add(detailEntry)
            }

        return when {
            isExpanded && listEntry != null && detailEntry != null ->
                ThreePaneScene(
                    key = mapEntry.contentKey,
                    entries = allSceneEntries,
                    previousEntries = emptyList(),
                    hostEntry = mapEntry,
                    listEntry = listEntry,
                    detailEntry = detailEntry,
                )

            isMedium ->
                TwoPaneScene(
                    key = mapEntry.contentKey,
                    entries = allSceneEntries,
                    previousEntries = emptyList(),
                    hostEntry = mapEntry,
                    panelEntry = findingsPanel,
                )

            else ->
                BottomSheetScene(
                    key = mapEntry.contentKey,
                    entries = allSceneEntries,
                    previousEntries = emptyList(),
                    hostEntry = mapEntry,
                    sheetEntry = findingsPanel,
                )
        }
    }
}

private class ThreePaneScene<T : Any>(
    override val key: Any,
    override val entries: List<NavEntry<T>>,
    override val previousEntries: List<NavEntry<T>> = emptyList(),
    private val listEntry: NavEntry<T>,
    private val hostEntry: NavEntry<T>,
    private val detailEntry: NavEntry<T>,
) : Scene<T> {
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

private class TwoPaneScene<T : Any>(
    override val key: Any,
    override val entries: List<NavEntry<T>>,
    override val previousEntries: List<NavEntry<T>>,
    private val hostEntry: NavEntry<T>,
    private val panelEntry: NavEntry<T>,
) : Scene<T> {
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

private class BottomSheetScene<T : Any>(
    override val key: Any,
    override val entries: List<NavEntry<T>>,
    override val previousEntries: List<NavEntry<T>>,
    private val hostEntry: NavEntry<T>,
    private val sheetEntry: NavEntry<T>,
) : Scene<T> {
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

object MapListDetailSceneMetadata {
    const val MAP_KEY = "MapListDetail-Map"
    const val LIST_KEY = "MapListDetail-List"
    const val DETAIL_KEY = "MapListDetail-Detail"

    fun mapPane(): Map<String, Any> = mapOf(MAP_KEY to true)

    fun listPane(): Map<String, Any> = mapOf(LIST_KEY to true)

    fun detailPane(): Map<String, Any> = mapOf(DETAIL_KEY to true)
}
