package cz.adamec.timotej.snag.projects.fe.driven

import cz.adamec.timotej.snag.projects.business.ClientImpl
import cz.adamec.timotej.snag.projects.business.InspectorImpl
import cz.adamec.timotej.snag.projects.business.Project
import cz.adamec.timotej.snag.projects.business.ProjectImpl
import cz.adamec.timotej.snag.projects.fe.ports.ProjectsLocalDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flowOf

class InMemoryProjectsLocalDataSource : ProjectsLocalDataSource {

    private val projects = mutableListOf<Project>()

    init {
        projects.addAll(
            listOf(
                ProjectImpl(
                    id = 1,
                    name = "Central Park Renovation",
                    address = "New York, NY 10024",
                    client = ClientImpl(1, "City Parks Dept", "830 5th Ave"),
                    inspector = InspectorImpl(1, "John Doe", "+12125550199")
                ),
                ProjectImpl(
                    id = 2,
                    name = "Golden Gate Bridge Painting",
                    address = "San Francisco, CA 94129",
                    client = ClientImpl(2, "Caltrans", "1120 N St, Sacramento"),
                    inspector = InspectorImpl(2, "Jane Smith", "+14155550122")
                ),
                ProjectImpl(
                    id = 3,
                    name = "Space Needle Maintenance",
                    address = "400 Broad St, Seattle, WA 98109",
                    client = ClientImpl(3, "Space Needle Corp", null),
                    inspector = InspectorImpl(3, "Bob Wilson", "+12065550144")
                )
            )
        )
    }

    override fun getProjects(): Flow<List<Project>> {
       return flowOf(projects)
    }
}
