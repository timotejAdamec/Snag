package cz.adamec.timotej.snag.projects.fe.driving.api

import kotlinx.serialization.Serializable

@Serializable
data object WebProjectsRoute : ProjectsRoute {
    val urlName = "projects"
}
