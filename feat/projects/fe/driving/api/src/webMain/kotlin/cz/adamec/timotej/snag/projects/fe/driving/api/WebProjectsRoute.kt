/*
 * Copyright (c) 2026 Timotej Adamec
 * SPDX-License-Identifier: MIT
 *
 * This file is part of the thesis:
 * "Multiplatform snagging system with code sharing maximisation"
 *
 * Czech Technical University in Prague
 * Faculty of Information Technology
 * Department of Software Engineering
 */

package cz.adamec.timotej.snag.projects.fe.driving.api

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
@Immutable
data object WebProjectsRoute : ProjectsRoute {
    const val URL_NAME = "projects"
}

@Serializable
@Immutable
data object WebProjectCreationRoute : ProjectCreationRoute {
    const val URL_NAME = "new-project"
}

@Serializable
@Immutable
data class WebProjectEditRoute(
    override val projectId: Uuid,
) : ProjectEditRoute {
    companion object {
        const val URL_NAME = "edit-project"
    }
}

@Serializable
@Immutable
data class WebProjectDetailRoute(
    override val projectId: Uuid,
) : ProjectDetailRoute {
    companion object {
        const val URL_NAME = "project-detail"
    }
}

class WebProjectEditRouteFactory : ProjectEditRouteFactory {
    override fun create(projectId: Uuid): ProjectEditRoute = WebProjectEditRoute(projectId)
}
