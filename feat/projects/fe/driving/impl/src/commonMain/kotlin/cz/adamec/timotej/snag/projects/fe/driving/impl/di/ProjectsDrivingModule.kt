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

package cz.adamec.timotej.snag.projects.fe.driving.impl.di

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation3.scene.DialogSceneStrategy
import cz.adamec.timotej.snag.lib.navigation.fe.SnagBackStack
import cz.adamec.timotej.snag.lib.navigation.fe.SnagNavRoute
import cz.adamec.timotej.snag.projects.fe.driving.api.ProjectCreationRoute
import cz.adamec.timotej.snag.projects.fe.driving.impl.internal.projectDetailsEdit.ui.ProjectDetailsEditScreen
import cz.adamec.timotej.snag.projects.fe.driving.impl.internal.projectDetailsEdit.vm.ProjectDetailsEditViewModel
import cz.adamec.timotej.snag.projects.fe.driving.impl.internal.projects.ui.ProjectsScreen
import cz.adamec.timotej.snag.projects.fe.driving.impl.internal.projects.vm.ProjectsViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.scope.Scope
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation
import kotlin.uuid.Uuid

internal inline fun <reified T : SnagNavRoute> Module.projectsScreenNavigation() =
    navigation<T> {
        ProjectsScreen(
            modifier = Modifier.fillMaxSize(),
            viewModel = koinViewModel(),
            onNewProjectClick = {
                val backStack = get<SnagBackStack>()
                val projectCreationRoute = get<ProjectCreationRoute>()
                backStack.value.add(projectCreationRoute)
            }
        )
    }

internal inline fun <reified T : SnagNavRoute> Module.projectDetailsEditScreenNavigation(
    crossinline getProjectId: (Scope.(T) -> Uuid?) = { null },
) = navigation<T>(
    metadata = DialogSceneStrategy.dialog(),
) { route ->
    ProjectDetailsEditScreen(
        projectId = getProjectId(route),
        onProjectSaved = {
            val backStack = get<SnagBackStack>()
            // TODO navigate to project screen
            backStack.value.removeLastOrNull()
        },
        onCancelClick = {
            val backStack = get<SnagBackStack>()
            backStack.value.removeLastOrNull()
        }
    )
}

val projectsDrivingImplModule =
    module {
        includes(platformModule)
        viewModelOf(::ProjectsViewModel)
        viewModel { (projectId: Uuid?) ->
            ProjectDetailsEditViewModel(
                projectId = projectId,
                getProjectUseCase = get(),
                saveProjectUseCase = get(),
            )
        }
    }

internal expect val platformModule: Module
