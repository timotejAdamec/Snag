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
import cz.adamec.timotej.snag.projects.fe.driving.impl.internal.projects.vm.ProjectsViewModel
import org.koin.compose.viewmodel.koinViewModel
import kotlin.uuid.Uuid

@Composable
internal fun ProjectsScreen(
    onNewProjectClick: () -> Unit,
    onProjectClick: (projectId: Uuid) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProjectsViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    SetFabState(
        fabState =
            FabState.Visible(
                text = "New project",
                onClick = {
                    onNewProjectClick()
                },
            ),
    )

    ShowSnackbarOnError(viewModel.errorsFlow)

    ProjectsContent(
        modifier = modifier,
        onProjectClick = onProjectClick,
        state = state,
    )
}
