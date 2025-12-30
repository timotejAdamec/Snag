package cz.adamec.timotej.snag.projects.fe.ports

import cz.adamec.timotej.snag.projects.business.Project
import kotlinx.coroutines.flow.Flow

interface ProjectsLocalDataSource {

    fun getProjects(): Flow<List<Project>>
}
