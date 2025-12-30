package cz.adamec.timotej.snag.projects.fe.driving.impl.internal.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.adamec.timotej.snag.projects.fe.app.GetProjectsUseCase
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

internal class ProjectsViewModel(
    getProjectsUseCase: GetProjectsUseCase,
) : ViewModel() {
    val state: StateFlow<ProjectsUiState> = getProjectsUseCase().map {
        ProjectsUiState(
            projects = it.toPersistentList(),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ProjectsUiState(),
    )
}
