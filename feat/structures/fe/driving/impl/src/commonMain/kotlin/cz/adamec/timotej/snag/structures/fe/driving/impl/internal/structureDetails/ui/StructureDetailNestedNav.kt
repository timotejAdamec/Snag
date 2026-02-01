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
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import cz.adamec.timotej.snag.feat.findings.fe.driving.api.FindingDetailRoute
import cz.adamec.timotej.snag.feat.findings.fe.driving.api.FindingsListRouteFactory
import cz.adamec.timotej.snag.feat.structures.fe.driving.api.StructureDetailBackStack
import cz.adamec.timotej.snag.feat.structures.fe.driving.api.StructureFloorPlanRoute
import cz.adamec.timotej.snag.lib.navigation.fe.SnagNavRoute
import kotlinx.serialization.Serializable
import org.koin.compose.koinInject
import org.koin.compose.navigation3.koinEntryProvider
import kotlin.uuid.Uuid

@Serializable
@Immutable
internal data class InternalStructureFloorPlanRoute(
    override val structureId: Uuid,
) : StructureFloorPlanRoute

@Composable
internal fun StructureDetailNestedNav(
    structureId: Uuid,
    onExitFlow: () -> Unit,
) {
    val innerBackStack = koinInject<StructureDetailBackStack>()
    val findingsListRouteFactory = koinInject<FindingsListRouteFactory>()
    val koinProvider = koinEntryProvider<SnagNavRoute>()

    LaunchedEffect(structureId) {
        innerBackStack.value.clear()
        innerBackStack.value.addAll(
            listOf(
                InternalStructureFloorPlanRoute(structureId),
                findingsListRouteFactory.create(structureId),
            ),
        )
    }

    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    val sceneStrategy =
        remember(windowSizeClass) {
            StructureFloorPlanSceneStrategy(windowSizeClass)
        }

    NavDisplay(
        backStack = innerBackStack.value,
        entryProvider = { route ->
            when (route) {
                is StructureFloorPlanRoute ->
                    NavEntry(route) {
                        StructureDetailsScreen(
                            structureId = route.structureId,
                            getSelectedFindingId = {
                                innerBackStack.value
                                    .filterIsInstance<FindingDetailRoute>()
                                    .lastOrNull()
                                    ?.findingId
                            },
                            onBack = onExitFlow,
                        )
                    }

                else -> koinProvider(route)
            }
        },
        sceneStrategy = sceneStrategy,
        entryDecorators =
            listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator(),
            ),
    )
}
