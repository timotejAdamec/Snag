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
import cz.adamec.timotej.snag.projects.fe.driving.api.ProjectDetailRoute
import cz.adamec.timotej.snag.projects.fe.driving.impl.internal.projectDetails.ui.ProjectDetailsScreen
import cz.adamec.timotej.snag.projects.fe.driving.impl.internal.projectDetails.vm.ProjectDetailsViewModel
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

internal inline fun <reified T : SnagNavRoute> Module.projectsScreenNavigation(
    crossinline getProjectDetailRoute: (projectId: Uuid) -> ProjectDetailRoute,
) = navigation<T> {
    ProjectsScreen(
        modifier = Modifier.fillMaxSize(),
        viewModel = koinViewModel(),
        onNewProjectClick = {
            val backStack = get<SnagBackStack>()
            val projectCreationRoute = get<ProjectCreationRoute>()
            backStack.value.add(projectCreationRoute)
        },
        onProjectClick = {
            val backStack = get<SnagBackStack>()
            val projectDetailRoute = getProjectDetailRoute(it)
            backStack.value.add(projectDetailRoute)
        }
    )
}

internal inline fun <reified T : SnagNavRoute> Module.projectDetailsEditScreenNavigation(
    crossinline getProjectDetailRoute: (savedProjectId: Uuid) -> ProjectDetailRoute,
    crossinline getProjectId: (Scope.(T) -> Uuid?) = { null },
) = navigation<T>(
    metadata = DialogSceneStrategy.dialog(),
) { route ->
    ProjectDetailsEditScreen(
        projectId = getProjectId(route),
        onProjectSaved = { savedProjectId ->
            val backStack = get<SnagBackStack>()
            val projectDetailRoute = getProjectDetailRoute(savedProjectId)
            backStack.value.add(projectDetailRoute)
        },
        onCancelClick = {
            val backStack = get<SnagBackStack>()
            backStack.value.removeLastOrNull()
        }
    )
}

internal inline fun <reified T : SnagNavRoute> Module.projectDetailsScreenNavigation(
    crossinline getProjectId: (Scope.(T) -> Uuid),
) = navigation<T> { route ->
    ProjectDetailsScreen(
        projectId = getProjectId(route),
        onBack = {
            val backStack = get<SnagBackStack>()
            backStack.value.removeLastOrNull()
        },
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
        viewModel { (projectId: Uuid) ->
            ProjectDetailsViewModel(
                projectId = projectId,
                getProjectUseCase = get(),
                deleteProjectUseCase = get()
            )
        }
    }

internal expect val platformModule: Module
