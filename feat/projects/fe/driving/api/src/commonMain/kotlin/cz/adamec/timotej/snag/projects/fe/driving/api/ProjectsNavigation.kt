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

import cz.adamec.timotej.snag.lib.navigation.fe.SnagNavRoute
import kotlin.uuid.Uuid

interface ProjectsRoute : SnagNavRoute

//interface ProjectCreationRoute : SnagNavRoute {
//    val projectId: Uuid?
//}

interface ProjectCreationRoute : SnagNavRoute

interface ProjectEditRoute : SnagNavRoute {
    val projectId: Uuid
}

interface ProjectEditRouteFactory {
    fun create(projectId: Uuid): ProjectEditRoute
}
