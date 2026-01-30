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

import androidx.compose.ui.window.DialogProperties
import androidx.navigation3.scene.DialogSceneStrategy
import cz.adamec.timotej.snag.lib.navigation.fe.SnagBackStack
import cz.adamec.timotej.snag.lib.navigation.fe.SnagNavRoute
import cz.adamec.timotej.snag.projects.fe.driving.api.ProjectDetailRoute
import cz.adamec.timotej.snag.structures.fe.driving.impl.internal.structureDetailsEdit.ui.StructureDetailsEditScreen
import cz.adamec.timotej.snag.structures.fe.driving.impl.internal.structureDetailsEdit.vm.StructureDetailsEditViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.core.scope.Scope
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation
import kotlin.uuid.Uuid

internal inline fun <reified T : SnagNavRoute> Module.structureCreationScreenNavigation(
    crossinline getProjectDetailRoute: (projectId: Uuid) -> ProjectDetailRoute,
    crossinline getProjectId: (Scope.(T) -> Uuid),
) = navigation<T>(
    metadata = DialogSceneStrategy.dialog(DialogProperties(usePlatformDefaultWidth = false)),
) { route ->
    StructureDetailsEditScreen(
        structureId = null,
        projectId = getProjectId(route),
        onSaveStructure = { _, _ ->
            val backStack = get<SnagBackStack>()
            backStack.removeLastSafely()
        },
        onCancelClick = {
            val backStack = get<SnagBackStack>()
            backStack.removeLastSafely()
        },
    )
}

internal inline fun <reified T : SnagNavRoute> Module.structureEditScreenNavigation(
    crossinline getProjectDetailRoute: (projectId: Uuid) -> ProjectDetailRoute,
    crossinline getStructureId: (Scope.(T) -> Uuid),
) = navigation<T>(
    metadata = DialogSceneStrategy.dialog(DialogProperties(usePlatformDefaultWidth = false)),
) { route ->
    StructureDetailsEditScreen(
        structureId = getStructureId(route),
        projectId = null,
        onSaveStructure = { _, _ ->
            val backStack = get<SnagBackStack>()
            backStack.removeLastSafely()
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
