package cz.adamec.timotej.snag.projects.fe.driving.impl.internal.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cz.adamec.timotej.snag.projects.fe.driving.api.OnProjectClick
import cz.adamec.timotej.snag.projects.fe.driving.impl.internal.ui.components.ProjectCard
import cz.adamec.timotej.snag.projects.fe.driving.impl.internal.vm.ProjectsViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun ProjectsScreen(
    modifier: Modifier = Modifier,
    viewModel: ProjectsViewModel = koinViewModel(),
    onProjectClick: OnProjectClick,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LazyVerticalGrid(
        modifier = modifier,
        columns = GridCells.Adaptive(minSize = 360.dp),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            bottom = 16.dp,
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
