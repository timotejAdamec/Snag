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

package cz.adamec.timotej.snag.feat.inspections.fe.driving.api

import cz.adamec.timotej.snag.projects.fe.driving.api.ProjectsNavRoute
import kotlin.uuid.Uuid

interface InspectionCreationRoute : ProjectsNavRoute {
    val projectId: Uuid
}

interface InspectionEditRoute : ProjectsNavRoute {
    val inspectionId: Uuid
}

interface InspectionCreationRouteFactory {
    fun create(projectId: Uuid): InspectionCreationRoute
}

interface InspectionEditRouteFactory {
    fun create(inspectionId: Uuid): InspectionEditRoute
}
