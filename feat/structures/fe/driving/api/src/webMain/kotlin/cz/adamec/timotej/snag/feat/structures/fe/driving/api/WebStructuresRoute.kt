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

package cz.adamec.timotej.snag.feat.structures.fe.driving.api

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
@Immutable
data class WebStructureCreationRoute(
    override val projectId: Uuid,
) : StructureCreationRoute {
    companion object {
        const val URL_NAME = "new-structure"
    }
}

@Serializable
@Immutable
data class WebStructureEditRoute(
    override val projectId: Uuid,
    override val structureId: Uuid,
) : StructureEditRoute {
    companion object {
        const val URL_NAME = "edit-structure"
    }
}

@Serializable
@Immutable
data class WebStructureFloorPlanRoute(
    override val projectId: Uuid,
    override val structureId: Uuid,
) : StructureFloorPlanRoute

class WebStructureCreationRouteFactory : StructureCreationRouteFactory {
    override fun create(projectId: Uuid): StructureCreationRoute = WebStructureCreationRoute(projectId)
}

class WebStructureEditRouteFactory : StructureEditRouteFactory {
    override fun create(
        projectId: Uuid,
        structureId: Uuid,
    ): StructureEditRoute =
        WebStructureEditRoute(
            projectId = projectId,
            structureId = structureId,
        )
}

class WebStructureFloorPlanRouteFactory : StructureFloorPlanRouteFactory {
    override fun create(
        projectId: Uuid,
        structureId: Uuid,
    ): StructureFloorPlanRoute =
        WebStructureFloorPlanRoute(
            projectId = projectId,
            structureId = structureId,
        )
}

@Serializable
@Immutable
data class WebStructureDetailNavRoute(
    override val projectId: Uuid,
    override val structureId: Uuid,
) : StructureDetailNavRoute {
    companion object {
        const val URL_NAME = "structure-detail"
    }
}

class WebStructureDetailRouteFactory : StructureDetailRouteFactory {
    override fun create(
        projectId: Uuid,
        structureId: Uuid,
    ): WebStructureDetailNavRoute =
        WebStructureDetailNavRoute(
            projectId = projectId,
            structureId = structureId,
        )
}
