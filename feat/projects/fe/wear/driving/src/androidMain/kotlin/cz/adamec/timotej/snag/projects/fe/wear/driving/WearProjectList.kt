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

package cz.adamec.timotej.snag.projects.fe.wear.driving

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.Text
import cz.adamec.timotej.snag.projects.app.model.AppProject
import cz.adamec.timotej.snag.projects.fe.common.driving.internal.projects.vm.ProjectsUiState
import cz.adamec.timotej.snag.projects.fe.common.driving.internal.projects.vm.ProjectsViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun WearProjectList(modifier: Modifier = Modifier) {
    val viewModel: ProjectsViewModel = koinViewModel()
    val state: ProjectsUiState by viewModel.state.collectAsStateWithLifecycle()
    WearProjectListContent(
        state = state,
        modifier = modifier,
    )
}

@Composable
internal fun WearProjectListContent(
    state: ProjectsUiState,
    modifier: Modifier = Modifier,
) {
    if (state.projects.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator()
        }
        return
    }
    ScalingLazyColumn(modifier = modifier.fillMaxSize()) {
        items(state.projects) { project ->
            ProjectChip(project = project)
        }
    }
}

@Composable
private fun ProjectChip(project: AppProject) {
    Chip(
        onClick = { },
        label = { Text(text = project.name) },
        secondaryLabel = { Text(text = if (project.isClosed) "Closed" else "Open") },
        colors = ChipDefaults.primaryChipColors(),
    )
}
