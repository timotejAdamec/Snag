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

package cz.adamec.timotej.snag.feat.findings.fe.driving.api

import androidx.compose.runtime.Immutable
import cz.adamec.timotej.snag.feat.findings.business.FindingTypeKey
import cz.adamec.timotej.snag.feat.findings.business.RelativeCoordinate
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
@Immutable
data class WebFindingsListRoute(
    override val projectId: Uuid,
    override val structureId: Uuid,
) : FindingsListRoute {
    companion object {
        const val URL_NAME = "findings"
    }
}

@Serializable
@Immutable
data class WebFindingDetailRoute(
    override val projectId: Uuid,
    override val structureId: Uuid,
    override val findingId: Uuid,
) : FindingDetailRoute {
    companion object {
        const val URL_NAME = "finding-detail"
    }
}

class WebFindingsListRouteFactory : FindingsListRouteFactory {
    override fun create(
        projectId: Uuid,
        structureId: Uuid,
    ): FindingsListRoute =
        WebFindingsListRoute(
            projectId = projectId,
            structureId = structureId,
        )
}

class WebFindingDetailRouteFactory : FindingDetailRouteFactory {
    override fun create(
        projectId: Uuid,
        structureId: Uuid,
        findingId: Uuid,
    ): FindingDetailRoute =
        WebFindingDetailRoute(
            projectId = projectId,
            structureId = structureId,
            findingId = findingId,
        )
}

@Serializable
@Immutable
data class WebFindingEditRoute(
    override val projectId: Uuid,
    override val findingId: Uuid,
) : FindingEditRoute {
    companion object {
        const val URL_NAME = "edit-finding"
    }
}

class WebFindingEditRouteFactory : FindingEditRouteFactory {
    override fun create(
        projectId: Uuid,
        structureId: Uuid,
        findingId: Uuid,
    ): FindingEditRoute =
        WebFindingEditRoute(
            projectId = projectId,
            findingId = findingId,
        )
}

@Serializable
@Immutable
data class WebFindingCreationRoute(
    override val projectId: Uuid,
    override val structureId: Uuid,
    val coordinateX: Float,
    val coordinateY: Float,
    override val findingTypeKey: FindingTypeKey,
) : FindingCreationRoute {
    override val coordinate: RelativeCoordinate
        get() = RelativeCoordinate(coordinateX, coordinateY)

    companion object {
        const val URL_NAME = "create-finding"
    }
}

class WebFindingCreationRouteFactory : FindingCreationRouteFactory {
    override fun create(
        projectId: Uuid,
        structureId: Uuid,
        coordinate: RelativeCoordinate,
        findingTypeKey: FindingTypeKey,
    ): FindingCreationRoute =
        WebFindingCreationRoute(
            projectId = projectId,
            structureId = structureId,
            coordinateX = coordinate.x,
            coordinateY = coordinate.y,
            findingTypeKey = findingTypeKey,
        )
}
