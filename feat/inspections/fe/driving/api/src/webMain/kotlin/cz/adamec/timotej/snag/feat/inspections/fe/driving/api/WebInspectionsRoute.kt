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
data class WebInspectionCreationRoute(
    override val projectId: Uuid,
) : InspectionCreationRoute {
    companion object {
        const val URL_NAME = "new-inspection"
    }
}

@Serializable
@Immutable
data class WebInspectionEditRoute(
    override val inspectionId: Uuid,
) : InspectionEditRoute {
    companion object {
        const val URL_NAME = "edit-inspection"
    }
}

class WebInspectionCreationRouteFactory : InspectionCreationRouteFactory {
    override fun create(projectId: Uuid): InspectionCreationRoute = WebInspectionCreationRoute(projectId)
}

class WebInspectionEditRouteFactory : InspectionEditRouteFactory {
    override fun create(inspectionId: Uuid): InspectionEditRoute = WebInspectionEditRoute(inspectionId)
}
