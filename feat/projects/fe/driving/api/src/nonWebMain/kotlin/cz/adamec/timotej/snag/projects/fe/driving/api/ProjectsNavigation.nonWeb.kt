package cz.adamec.timotej.snag.projects.fe.driving.api

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

@Serializable
@Immutable
data object ProjectsRouteImpl : ProjectsRoute {
    const val URL_NAME = "projects"
}
