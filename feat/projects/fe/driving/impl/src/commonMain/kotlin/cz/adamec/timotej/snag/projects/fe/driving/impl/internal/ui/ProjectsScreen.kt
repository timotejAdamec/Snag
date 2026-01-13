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

package cz.adamec.timotej.snag.projects.fe.driving.impl.internal.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cz.adamec.timotej.snag.projects.fe.driving.api.OnProjectClick
import cz.adamec.timotej.snag.projects.fe.driving.impl.internal.vm.ProjectsViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun ProjectsScreen(
    onProjectClick: OnProjectClick,
    modifier: Modifier = Modifier,
    viewModel: ProjectsViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ProjectsContent(
        modifier = modifier,
        onProjectClick = onProjectClick,
        onShowErrorMessage = { viewModel.onErrorMessageShown() },
        state = state,
    )
}
