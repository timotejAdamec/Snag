package cz.adamec.timotej.snag.projects.fe.driving.api

import cz.adamec.timotej.snag.lib.navigation.NavRoute
import kotlin.uuid.Uuid

interface ProjectsRoute : NavRoute

interface OnProjectClick {
    operator fun invoke(projectId: Uuid)
}
