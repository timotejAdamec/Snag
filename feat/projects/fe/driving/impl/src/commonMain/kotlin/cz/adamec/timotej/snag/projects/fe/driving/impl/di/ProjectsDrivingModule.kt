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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.scene.DialogSceneStrategy
import cz.adamec.timotej.snag.lib.design.fe.dialog.fullscreenDialogProperties
import cz.adamec.timotej.snag.feat.structures.fe.driving.api.StructureCreationRouteFactory
import cz.adamec.timotej.snag.feat.structures.fe.driving.api.StructureDetailRouteFactory
import cz.adamec.timotej.snag.lib.navigation.fe.SnagBackStack
import cz.adamec.timotej.snag.projects.fe.driving.api.ProjectCreationRoute
import cz.adamec.timotej.snag.projects.fe.driving.api.ProjectDetailRoute
import cz.adamec.timotej.snag.projects.fe.driving.api.ProjectDetailRouteFactory
import cz.adamec.timotej.snag.projects.fe.driving.api.ProjectEditRoute
import cz.adamec.timotej.snag.projects.fe.driving.api.ProjectEditRouteFactory
import cz.adamec.timotej.snag.projects.fe.driving.api.ProjectsRoute
import cz.adamec.timotej.snag.projects.fe.driving.impl.internal.projectDetails.ui.ProjectDetailsScreen
import cz.adamec.timotej.snag.projects.fe.driving.impl.internal.projectDetails.vm.ProjectDetailsViewModel
import cz.adamec.timotej.snag.projects.fe.driving.impl.internal.projectDetailsEdit.ui.ProjectDetailsEditScreen
import cz.adamec.timotej.snag.projects.fe.driving.impl.internal.projectDetailsEdit.vm.ProjectDetailsEditViewModel
import cz.adamec.timotej.snag.projects.fe.driving.impl.internal.projects.ui.ProjectsScreen
import cz.adamec.timotej.snag.projects.fe.driving.impl.internal.projects.vm.ProjectsViewModel
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.scope.Scope
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation
import kotlin.uuid.Uuid

internal inline fun <reified T : ProjectsRoute> Module.projectsScreenNavigation() =
    navigation<T> {
        val projectDetailRouteFactory = koinInject<ProjectDetailRouteFactory>()
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
                val projectDetailRoute = projectDetailRouteFactory.create(it)
                backStack.value.add(projectDetailRoute)
            },
        )
    }

@Suppress("FunctionNameMaxLength")
internal inline fun <reified T : ProjectCreationRoute> Module.projectCreationScreenNavigation() =
    navigation<T>(
        metadata = DialogSceneStrategy.dialog(fullscreenDialogProperties()),
    ) { _ ->
        val projectDetailRouteFactory = koinInject<ProjectDetailRouteFactory>()
        ProjectDetailsEditScreenInjection(
            onSaveProject = { savedProjectId ->
                val backStack = get<SnagBackStack>()
                val destinationRoute = projectDetailRouteFactory.create(savedProjectId)
                backStack.removeLastSafely()
                backStack.value.add(destinationRoute)
            },
        )
    }

internal inline fun <reified T : ProjectEditRoute> Module.projectEditScreenNavigation() =
    navigation<T>(
        metadata = DialogSceneStrategy.dialog(fullscreenDialogProperties()),
    ) { route ->
        ProjectDetailsEditScreenInjection(
            projectId = route.projectId,
            onSaveProject = { _ ->
                val backStack = get<SnagBackStack>()
                backStack.removeLastSafely()
            },
        )
    }

@Composable
@Suppress("FunctionNameMaxLength")
private fun Scope.ProjectDetailsEditScreenInjection(
    projectId: Uuid? = null,
    onSaveProject: (savedProjectId: Uuid) -> Unit,
) {
    ProjectDetailsEditScreen(
        projectId = projectId,
        onSaveProject = { savedProjectId ->
            onSaveProject(savedProjectId)
        },
        onCancelClick = {
            val backStack = get<SnagBackStack>()
            backStack.removeLastSafely()
        },
    )
}

internal inline fun <reified T : ProjectDetailRoute> Module.projectDetailsScreenNavigation() =
    navigation<T> { route ->
        val newStructureRouteFactory = koinInject<StructureCreationRouteFactory>()
        val structureDetailRouteFactory = koinInject<StructureDetailRouteFactory>()
        val projectEditRouteFactory = koinInject<ProjectEditRouteFactory>()
        ProjectDetailsScreen(
            projectId = route.projectId,
            onNewStructureClick = {
                val backStack = get<SnagBackStack>()
                backStack.value.add(newStructureRouteFactory.create(route.projectId))
            },
            onStructureClick = { structureId ->
                val backStack = get<SnagBackStack>()
                backStack.value.add(structureDetailRouteFactory.create(structureId))
            },
            onBack = {
                val backStack = get<SnagBackStack>()
                backStack.removeLastSafely()
            },
            onEditClick = {
                val backStack = get<SnagBackStack>()
                backStack.value.add(projectEditRouteFactory.create(route.projectId))
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
                deleteProjectUseCase = get(),
                getStructuresUseCase = get(),
            )
        }
    }

internal expect val platformModule: Module
