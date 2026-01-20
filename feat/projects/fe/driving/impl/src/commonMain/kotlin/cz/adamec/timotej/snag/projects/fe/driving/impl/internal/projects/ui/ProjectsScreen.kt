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

package cz.adamec.timotej.snag.projects.fe.driving.impl.internal.projects.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cz.adamec.timotej.snag.lib.design.fe.error.ShowSnackbarOnError
import cz.adamec.timotej.snag.lib.design.fe.scaffold.FabState
import cz.adamec.timotej.snag.lib.design.fe.scaffold.SetFabState
import cz.adamec.timotej.snag.lib.design.fe.scaffold.SetTitle
import cz.adamec.timotej.snag.lib.navigation.fe.SnagBackStack
import cz.adamec.timotej.snag.projects.fe.driving.api.ProjectCreationRoute
import cz.adamec.timotej.snag.projects.fe.driving.api.ProjectEditRouteFactory
import cz.adamec.timotej.snag.projects.fe.driving.impl.internal.projects.vm.ProjectsViewModel
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import snag.feat.projects.fe.driving.impl.generated.resources.Res
import snag.feat.projects.fe.driving.impl.generated.resources.projects_title

@Composable
internal fun ProjectsScreen(
    backStack: SnagBackStack,
    modifier: Modifier = Modifier,
    projectCreationRoute: ProjectCreationRoute = koinInject(),
    projectEditRouteFactory: ProjectEditRouteFactory = koinInject(),
    viewModel: ProjectsViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    SetTitle(stringResource = Res.string.projects_title)
    SetFabState(
        fabState = FabState.Visible(
            text = "New project",
            onClick = {
                backStack.value.add(projectCreationRoute)
            },
        )
    )

    ShowSnackbarOnError(viewModel.errorsFlow)

    ProjectsContent(
        modifier = modifier,
        onProjectClick = {
            val newRoute = projectEditRouteFactory.create(it)
            backStack.value.add(newRoute)
        },
        state = state,
    )
}
