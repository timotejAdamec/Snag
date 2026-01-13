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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cz.adamec.timotej.snag.lib.design.fe.error.ShowErrorSnackbarIfNeeded
import cz.adamec.timotej.snag.lib.design.fe.scaffold.SetTitle
import cz.adamec.timotej.snag.projects.fe.driving.api.OnProjectClick
import cz.adamec.timotej.snag.projects.fe.driving.impl.internal.ui.components.ProjectCard
import cz.adamec.timotej.snag.projects.fe.driving.impl.internal.vm.ProjectsUiState
import snag.feat.projects.fe.driving.impl.generated.resources.Res
import snag.feat.projects.fe.driving.impl.generated.resources.projects_title

@Composable
internal fun ProjectsContent(
    state: ProjectsUiState,
    onProjectClick: OnProjectClick,
    modifier: Modifier = Modifier,
) {
    SetTitle(Res.string.projects_title)
    ShowErrorSnackbarIfNeeded(state.error)

    LazyVerticalGrid(
        modifier = modifier,
        columns = GridCells.Adaptive(minSize = 360.dp),
        contentPadding =
            PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 16.dp,
                bottom = 48.dp,
            ),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(
            items = state.projects,
            key = { it.id },
        ) { project ->
            ProjectCard(
                modifier = Modifier,
                onClick = { onProjectClick(project.id) },
                project = project,
            )
        }
        item {
            AnimatedVisibility(
                visible = state.isLoading,
                exit = fadeOut(),
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                ) {
                    ContainedLoadingIndicator()
                }
            }
        }
    }
}


