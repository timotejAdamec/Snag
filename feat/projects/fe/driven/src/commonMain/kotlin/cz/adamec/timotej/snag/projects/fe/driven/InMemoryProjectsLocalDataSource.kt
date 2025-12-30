package cz.adamec.timotej.snag.projects.fe.driven

import cz.adamec.timotej.snag.projects.business.ClientImpl
import cz.adamec.timotej.snag.projects.business.InspectionImpl
import cz.adamec.timotej.snag.projects.business.InspectorImpl
import cz.adamec.timotej.snag.projects.business.Project
import cz.adamec.timotej.snag.projects.business.ProjectImpl
import cz.adamec.timotej.snag.projects.fe.ports.ProjectsLocalDataSource
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant

class InMemoryProjectsLocalDataSource : ProjectsLocalDataSource {

    private val projects = mutableListOf<Project>()

    init {
        val now = Instant.parse("2023-10-27T10:00:00Z")
        projects.addAll(
            listOf(
                ProjectImpl(
                    id = 1,
                    name = "Central Park Renovation",
                    address = "New York, NY 10024",
                    client = ClientImpl(1, "City Parks Dept", "830 5th Ave"),
                    inspector = InspectorImpl(1, "John Doe", "+12125550199"),
                    inspection = InspectionImpl(
                        start = now,
                        end = now + 2.hours,
                        otherParticipants = persistentListOf("Alice Freeman", "Bob Smith"),
                        climateConditions = "Sunny, 20°C",
                        note = "Initial site survey completed."
                    ),
                    findingCategories = persistentListOf("Safety", "Landscaping")
                ),
                ProjectImpl(
                    id = 2,
                    name = "Golden Gate Bridge Painting",
                    address = "San Francisco, CA 94129",
                    client = ClientImpl(2, "Caltrans", "1120 N St, Sacramento"),
                    inspector = InspectorImpl(2, "Jane Smith", "+14155550122"),
                    inspection = null,
                    findingCategories = persistentListOf("Structure", "Paint Quality")
                ),
                ProjectImpl(
                    id = 3,
                    name = "Space Needle Maintenance",
                    address = "400 Broad St, Seattle, WA 98109",
                    client = ClientImpl(3, "Space Needle Corp", null),
                    inspector = InspectorImpl(3, "Bob Wilson", "+12065550144"),
                    inspection = null,
                    findingCategories = persistentListOf("Elevators", "Structural")
                )
            )
        )
    }

    override fun getProjects(): Flow<List<Project>> {
        return flowOf(projects)
    }
}
