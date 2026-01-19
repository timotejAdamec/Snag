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
import cz.adamec.timotej.snag.projects.fe.driving.api.ProjectCreationRoute
import cz.adamec.timotej.snag.projects.fe.driving.impl.internal.project.ui.NewProjectScreen
import cz.adamec.timotej.snag.projects.fe.driving.impl.internal.projects.ui.ProjectsScreen
import cz.adamec.timotej.snag.projects.fe.driving.impl.internal.projects.vm.ProjectsViewModel
import cz.adamec.timotej.snag.projects.fe.driving.impl.internal.project.ui.ProjectEditScreen
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.scope.Scope
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation

@Suppress("ktlint:compose:modifier-missing-check")
@Composable
fun Scope.ProjectsScreenInjection() {
    ProjectsScreen(
        modifier = Modifier.fillMaxSize(),
        viewModel = koinViewModel(),
        backStack = get(),
    )
}

val projectsDrivingImplModule =
    module {
        includes(platformModule)
        viewModelOf(::ProjectsViewModel)
        navigation<ProjectCreationRoute> { route ->
            NewProjectScreen(
                modifier = Modifier.fillMaxSize(),
            )
        }
    }

internal expect val platformModule: Module
