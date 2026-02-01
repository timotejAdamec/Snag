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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.window.DialogProperties
import androidx.navigation3.scene.DialogSceneStrategy
import cz.adamec.timotej.snag.feat.findings.fe.driving.api.FindingDetailRoute
import cz.adamec.timotej.snag.feat.findings.fe.driving.api.FindingsListRouteFactory
import cz.adamec.timotej.snag.feat.structures.fe.driving.api.StructureCreationRoute
import cz.adamec.timotej.snag.feat.structures.fe.driving.api.StructureDetailRoute
import cz.adamec.timotej.snag.feat.structures.fe.driving.api.StructureEditRoute
import cz.adamec.timotej.snag.lib.navigation.fe.SnagBackStack
import cz.adamec.timotej.snag.structures.fe.driving.impl.internal.structureDetails.ui.StructureDetailsScreen
import cz.adamec.timotej.snag.structures.fe.driving.impl.internal.structureDetails.vm.StructureDetailsViewModel
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

internal inline fun <reified T : StructureDetailRoute> Module.structureDetailScreenNav() =
    navigation<T> { route ->
        val backStack: SnagBackStack = koinInject()
        val findingsListRouteFactory = koinInject<FindingsListRouteFactory>()
        LaunchedEffect(Unit) {
            backStack.value.add(findingsListRouteFactory.create(route.structureId))
        }
        StructureDetailsScreen(
            structureId = route.structureId,
            getSelectedFindingId = {
                backStack.value
                    .filterIsInstance<FindingDetailRoute>()
                    .lastOrNull()
                    ?.findingId
            },
            onBack = {
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
        viewModel { (structureId: Uuid) ->
            StructureDetailsViewModel(
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
