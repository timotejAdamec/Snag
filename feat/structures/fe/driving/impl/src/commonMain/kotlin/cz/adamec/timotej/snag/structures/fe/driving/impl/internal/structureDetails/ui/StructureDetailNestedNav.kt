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

import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import cz.adamec.timotej.snag.feat.findings.fe.driving.api.FindingsListRouteFactory
import cz.adamec.timotej.snag.feat.structures.fe.driving.api.StructureDetailBackStack
import cz.adamec.timotej.snag.feat.structures.fe.driving.api.StructureDetailNavRoute
import cz.adamec.timotej.snag.feat.structures.fe.driving.api.StructureFloorPlanRouteFactory
import cz.adamec.timotej.snag.lib.design.fe.scenes.MapListDetailSceneStrategy
import org.koin.compose.koinInject
import org.koin.compose.navigation3.koinEntryProvider
import kotlin.uuid.Uuid

@Composable
internal fun StructureDetailNestedNav(
    structureId: Uuid,
    onExit: () -> Unit,
) {
    val injectedInnerBackStack = koinInject<StructureDetailBackStack>()
    val backStack = remember { mutableStateOf(injectedInnerBackStack.value) }
    val structureFloorPlanRouteFactory = koinInject<StructureFloorPlanRouteFactory>()
    val findingsListRouteFactory = koinInject<FindingsListRouteFactory>()
    val koinEntryProvider = koinEntryProvider<StructureDetailNavRoute>()

    LaunchedEffect(Unit) {
        snapshotFlow { backStack.value.size }
            .collect { size ->
                if (size in 0..1) onExit()
            }
    }

    LaunchedEffect(structureId) {
        backStack.value.clear()
        backStack.value.addAll(
            listOf(
                structureFloorPlanRouteFactory.create(structureId),
                findingsListRouteFactory.create(structureId),
            ),
        )
    }

    if (backStack.value.size <= 1) {
        backStack.value.clear()
        backStack.value.addAll(
            listOf(
                structureFloorPlanRouteFactory.create(structureId),
                findingsListRouteFactory.create(structureId),
            ),
        )
    }

    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    val sceneStrategy =
        remember(windowSizeClass) {
            MapListDetailSceneStrategy<StructureDetailNavRoute>(windowSizeClass)
        }

    NavDisplay(
        backStack = backStack.value,
        entryProvider = koinEntryProvider,
        sceneStrategy = sceneStrategy,
        entryDecorators =
            listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator(),
            ),
    )
}
