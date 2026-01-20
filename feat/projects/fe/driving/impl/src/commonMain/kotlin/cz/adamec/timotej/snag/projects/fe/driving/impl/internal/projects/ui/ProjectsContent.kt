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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.LoadingIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cz.adamec.timotej.snag.lib.design.fe.scaffold.CollapsableTopAppBarScaffold
import cz.adamec.timotej.snag.projects.fe.driving.impl.internal.projects.ui.components.ProjectListItem
import cz.adamec.timotej.snag.projects.fe.driving.impl.internal.projects.vm.ProjectsUiState
import org.jetbrains.compose.resources.stringResource
import snag.feat.projects.fe.driving.impl.generated.resources.Res
import snag.feat.projects.fe.driving.impl.generated.resources.projects_title
import kotlin.uuid.Uuid

@Composable
internal fun ProjectsContent(
    state: ProjectsUiState,
    onProjectClick: (projectId: Uuid) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    CollapsableTopAppBarScaffold(
        title = stringResource(Res.string.projects_title)
    ) { paddingValues ->
        LazyVerticalGrid(
            modifier = modifier.padding(paddingValues),
            columns = GridCells.Adaptive(minSize = 360.dp),
            contentPadding =
                PaddingValues(
                    start = 8.dp,
                    end = 8.dp,
                    top = 8.dp,
                    bottom = 48.dp,
                ),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(
                items = state.projects,
                key = { it.id },
            ) { project ->
                ProjectListItem(
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
                        LoadingIndicator()
                    }
                }
            }
        }
    }
}
