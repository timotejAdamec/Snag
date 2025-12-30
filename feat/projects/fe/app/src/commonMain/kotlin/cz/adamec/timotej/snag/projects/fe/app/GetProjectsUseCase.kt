package cz.adamec.timotej.snag.projects.fe.app

import cz.adamec.timotej.snag.projects.business.Project
import cz.adamec.timotej.snag.projects.fe.ports.ProjectsLocalDataSource
import kotlinx.coroutines.flow.Flow

class GetProjectsUseCase(
    private val localDataSource: ProjectsLocalDataSource,
) {
    operator fun invoke(): Flow<List<Project>> {
        return localDataSource.getProjects()
    }
}
