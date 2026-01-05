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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cz.adamec.timotej.snag.lib.design.fe.scaffold.LocalAppScaffoldState
import cz.adamec.timotej.snag.projects.fe.driving.api.OnProjectClick
import cz.adamec.timotej.snag.projects.fe.driving.impl.internal.ui.components.ProjectCard
import cz.adamec.timotej.snag.projects.fe.driving.impl.internal.vm.ProjectsViewModel
import org.jetbrains.compose.resources.getString
import org.koin.compose.viewmodel.koinViewModel
import snag.feat.projects.fe.driving.impl.generated.resources.Res
import snag.feat.projects.fe.driving.impl.generated.resources.projects_title

@Composable
internal fun ProjectsScreen(
    modifier: Modifier = Modifier,
    viewModel: ProjectsViewModel = koinViewModel(),
    onProjectClick: OnProjectClick,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val scaffoldState = LocalAppScaffoldState.current

    LaunchedEffect(Unit) {
        scaffoldState.title.value = getString(Res.string.projects_title)
    }

    LazyVerticalGrid(
        modifier = modifier,
        columns = GridCells.Adaptive(minSize = 360.dp),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 16.dp,
            bottom = 48.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(state.projects) { project ->
            ProjectCard(
                modifier = Modifier,
                project = project,
                onClick = { onProjectClick(project.id) }
            )
        }
    }
}
