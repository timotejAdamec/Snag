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
data object NonWebProjectsRoute : ProjectsRoute

@Serializable
@Immutable
data object NonWebProjectCreationRoute : ProjectCreationRoute

@Serializable
@Immutable
data class NonWebProjectEditRoute(
    override val projectId: Uuid,
) : ProjectEditRoute

class NonWebProjectEditRouteFactory : ProjectEditRouteFactory {
    override fun create(projectId: Uuid): ProjectEditRoute = NonWebProjectEditRoute(projectId)
}
