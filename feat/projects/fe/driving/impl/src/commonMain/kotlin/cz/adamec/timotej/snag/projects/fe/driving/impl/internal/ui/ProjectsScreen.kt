package cz.adamec.timotej.snag.projects.fe.driving.impl.internal.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import cz.adamec.timotej.snag.projects.fe.driving.impl.internal.vm.ProjectsViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun ProjectsScreen(
    viewModel: ProjectsViewModel = koinViewModel(),
) {
    Text("Projects screen")
}
