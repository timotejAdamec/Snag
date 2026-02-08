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

@file:Suppress("UnnecessaryFullyQualifiedName")

package cz.adamec.timotej.snag.lib.design.fe.scenes

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneStrategy
import androidx.navigation3.scene.SceneStrategyScope
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_EXPANDED_LOWER_BOUND
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_MEDIUM_LOWER_BOUND

class MapListDetailSceneStrategy<T : Any> : SceneStrategy<T> {
    @Suppress("ReturnCount")
    override fun SceneStrategyScope<T>.calculateScene(entries: List<NavEntry<T>>): Scene<T>? {
        val mapEntry =
            entries.findLast {
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

        if (mapEntry == null || listEntry == null && detailEntry == null) return null

        val firstFindingsIndex =
            entries.indexOfFirst {
                it.metadata.containsKey(MapListDetailSceneMetadata.LIST_KEY) ||
                    it.metadata.containsKey(MapListDetailSceneMetadata.DETAIL_KEY)
            }
        if (firstFindingsIndex < 1) return null

        val allSceneEntries =
            buildList {
                add(mapEntry)
                if (listEntry != null) add(listEntry)
                if (detailEntry != null) add(detailEntry)
            }

        return AdaptiveMapListDetailScene(
            key = mapEntry.contentKey,
            entries = allSceneEntries,
            previousEntries = emptyList(),
            hostEntry = mapEntry,
            listEntry = listEntry,
            detailEntry = detailEntry,
        )
    }
}

private const val HALF_WEIGHT = 0.5f
private const val QUARTER_WEIGHT = 0.25f

private class AdaptiveMapListDetailScene<T : Any>(
    override val key: Any,
    override val entries: List<NavEntry<T>>,
    override val previousEntries: List<NavEntry<T>>,
    private val hostEntry: NavEntry<T>,
    private val listEntry: NavEntry<T>?,
    private val detailEntry: NavEntry<T>?,
) : Scene<T> {
    override val content: @Composable () -> Unit = {
        val hostPane = remember { movableContentOf<NavEntry<T>> { it.Content() } }

        val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
        val isExpanded =
            windowSizeClass.isWidthAtLeastBreakpoint(WIDTH_DP_EXPANDED_LOWER_BOUND)
        val isAtLeastMedium =
            windowSizeClass.isWidthAtLeastBreakpoint(WIDTH_DP_MEDIUM_LOWER_BOUND)

        when {
            isExpanded && listEntry != null && detailEntry != null -> {
                Row(modifier = Modifier.fillMaxSize()) {
                    Column(modifier = Modifier.weight(HALF_WEIGHT)) {
                        hostPane(hostEntry)
                    }
                    Column(modifier = Modifier.weight(QUARTER_WEIGHT)) {
                        listEntry.Content()
                    }
                    Column(modifier = Modifier.weight(QUARTER_WEIGHT)) {
                        detailEntry.Content()
                    }
                }
            }

            isAtLeastMedium -> {
                Row(modifier = Modifier.fillMaxSize()) {
                    Column(modifier = Modifier.weight(HALF_WEIGHT)) {
                        hostPane(hostEntry)
                    }
                    Column(modifier = Modifier.weight(HALF_WEIGHT)) {
                        if (detailEntry != null) {
                            detailEntry.Content()
                        } else {
                            listEntry?.Content()
                        }
                    }
                }
            }

            else -> {
                key(detailEntry != null) {
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
                            if (detailEntry != null) {
                                detailEntry.Content()
                            } else {
                                listEntry?.Content()
                            }
                        },
                    ) {
                        hostPane(hostEntry)
                    }
                }
            }
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
