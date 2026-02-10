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

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
@Immutable
data class NonWebInspectionCreationRoute(
    override val projectId: Uuid,
) : InspectionCreationRoute

@Serializable
@Immutable
data class NonWebInspectionEditRoute(
    override val inspectionId: Uuid,
) : InspectionEditRoute

class NonWebInspectionCreationRouteFactory : InspectionCreationRouteFactory {
    override fun create(projectId: Uuid): InspectionCreationRoute = NonWebInspectionCreationRoute(projectId)
}

class NonWebInspectionEditRouteFactory : InspectionEditRouteFactory {
    override fun create(inspectionId: Uuid): InspectionEditRoute = NonWebInspectionEditRoute(inspectionId)
}
