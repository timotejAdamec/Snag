package cz.adamec.timotej.snag.projects.fe.driving.impl.internal.vm

import cz.adamec.timotej.snag.projects.business.Project
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class ProjectsUiState(
    val projects: ImmutableList<Project> = persistentListOf(),
)
