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

package cz.adamec.timotej.snag.structures.fe.driving.impl.di

import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.window.DialogProperties
import androidx.navigation3.scene.DialogSceneStrategy
import cz.adamec.timotej.snag.feat.findings.fe.driving.api.FindingDetailRoute
import cz.adamec.timotej.snag.feat.findings.fe.driving.api.FindingDetailRouteFactory
import cz.adamec.timotej.snag.feat.structures.fe.driving.api.StructureCreationRoute
import cz.adamec.timotej.snag.feat.structures.fe.driving.api.StructureDetailBackStack
import cz.adamec.timotej.snag.feat.structures.fe.driving.api.StructureDetailNavRoute
import cz.adamec.timotej.snag.feat.structures.fe.driving.api.StructureEditRoute
import cz.adamec.timotej.snag.feat.structures.fe.driving.api.StructureEditRouteFactory
import cz.adamec.timotej.snag.feat.structures.fe.driving.api.StructureFloorPlanRoute
import cz.adamec.timotej.snag.lib.design.fe.scenes.MapListDetailSceneMetadata
import cz.adamec.timotej.snag.lib.navigation.fe.SnagBackStack
import cz.adamec.timotej.snag.structures.fe.driving.impl.internal.structureDetails.ui.StructureDetailNestedNav
import cz.adamec.timotej.snag.structures.fe.driving.impl.internal.floorPlan.ui.StructureFloorPlanScreen
import cz.adamec.timotej.snag.structures.fe.driving.impl.internal.floorPlan.vm.StructureFloorPlanViewModel
import cz.adamec.timotej.snag.structures.fe.driving.impl.internal.structureDetailsEdit.ui.StructureDetailsEditScreen
import cz.adamec.timotej.snag.structures.fe.driving.impl.internal.structureDetailsEdit.vm.StructureDetailsEditViewModel
import org.koin.compose.koinInject
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.core.scope.Scope
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation
import kotlin.uuid.Uuid

internal inline fun <reified T : StructureCreationRoute> Module.structureCreateScreenNav() =
    navigation<T>(
        metadata = DialogSceneStrategy.dialog(DialogProperties(usePlatformDefaultWidth = false)),
    ) { route ->
        StructureEditScreenSetup(
            projectId = route.projectId,
            onSaveStructure = { _ ->
                val backStack = get<SnagBackStack>()
                backStack.removeLastSafely()
            },
        )
    }

internal inline fun <reified T : StructureEditRoute> Module.structureEditScreenNavigation() =
    navigation<T>(
        metadata = DialogSceneStrategy.dialog(DialogProperties(usePlatformDefaultWidth = false)),
    ) { route ->
        StructureEditScreenSetup(
            structureId = route.structureId,
            onSaveStructure = { _ ->
                val backStack = get<SnagBackStack>()
                backStack.removeLastSafely()
            },
        )
    }

@Suppress("FunctionNameMaxLength")
internal inline fun <reified T : StructureFloorPlanRoute> Module.structureFloorPlanScreenNavigation() =
    navigation<T>(
        metadata = MapListDetailSceneMetadata.mapPane(),
    ) { route ->
        val structureDetailBackStack = get<StructureDetailBackStack>()
        val structureEditRouteFactory = koinInject<StructureEditRouteFactory>()
        val selectedFindingId by derivedStateOf {
            structureDetailBackStack.value
                .filterIsInstance<FindingDetailRoute>()
                .lastOrNull()
                ?.findingId
        }
        StructureFloorPlanScreen(
            structureId = route.structureId,
            selectedFindingId = selectedFindingId,
            onBack = {
                val rootBackStack = get<SnagBackStack>()
                rootBackStack.removeLastSafely()
            },
            onEditClick = {
                val rootBackStack = get<SnagBackStack>()
                rootBackStack.value.add(structureEditRouteFactory.create(route.structureId))
            },
            onFindingClick = { findingId ->
                val factory = get<FindingDetailRouteFactory>()
                if (structureDetailBackStack.value.lastOrNull() is FindingDetailRoute) {
                    structureDetailBackStack.removeLastSafely()
                }
                structureDetailBackStack.value.add(
                    factory.create(
                        structureId = route.structureId,
                        findingId = findingId,
                    ),
                )
            },
        )
    }

internal inline fun <reified T : StructureDetailNavRoute> Module.structureDetailScreenNav() =
    navigation<T> { route ->
        StructureDetailNestedNav(
            structureId = route.structureId,
            onExit = {
                val backStack = get<SnagBackStack>()
                backStack.removeLastSafely()
            },
        )
    }

@Composable
private fun Scope.StructureEditScreenSetup(
    onSaveStructure: (savedStructureId: Uuid) -> Unit,
    structureId: Uuid? = null,
    projectId: Uuid? = null,
) {
    StructureDetailsEditScreen(
        structureId = structureId,
        projectId = projectId,
        onSaveStructure = { savedStructureId, _ ->
            onSaveStructure(savedStructureId)
        },
        onCancelClick = {
            val backStack = get<SnagBackStack>()
            backStack.removeLastSafely()
        },
    )
}

val structuresDrivingImplModule =
    module {
        includes(platformModule)
        single { StructureDetailBackStack(mutableStateListOf()) }
        viewModel { (structureId: Uuid) ->
            StructureFloorPlanViewModel(
                structureId = structureId,
                getStructureUseCase = get(),
                deleteStructureUseCase = get(),
                getFindingsUseCase = get(),
            )
        }
        viewModel { (structureId: Uuid?, projectId: Uuid?) ->
            StructureDetailsEditViewModel(
                structureId = structureId,
                projectId = projectId,
                getStructureUseCase = get(),
                saveStructureUseCase = get(),
            )
        }
    }

internal expect val platformModule: Module
