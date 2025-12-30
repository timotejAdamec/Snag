package cz.adamec.timotej.snag.projects.fe.driving.impl.internal.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cz.adamec.timotej.snag.projects.fe.driving.impl.internal.vm.ProjectsViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun ProjectsScreen(
    modifier: Modifier = Modifier,
    viewModel: ProjectsViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LazyVerticalGrid(
        modifier = modifier,
        columns = GridCells.Adaptive(minSize = 120.dp),
    ) {
        items(state.projects.size) { index ->
            Text(text = state.projects[index].toString())
        }
    }
}
