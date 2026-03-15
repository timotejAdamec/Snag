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

import kotlin.uuid.Uuid

interface ProjectsRoute : ProjectsNavRoute

interface ProjectCreationRoute : ProjectsNavRoute

interface ProjectEditRoute : ProjectsNavRoute {
    val projectId: Uuid
}

interface ProjectDetailRoute : ProjectsNavRoute {
    val projectId: Uuid
}

interface ProjectEditRouteFactory {
    fun create(projectId: Uuid): ProjectEditRoute
}

interface ProjectDetailRouteFactory {
    fun create(projectId: Uuid): ProjectDetailRoute
}
