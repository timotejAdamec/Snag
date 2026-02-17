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
import cz.adamec.timotej.snag.feat.findings.business.RelativeCoordinate
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
@Immutable
data class WebFindingsListRoute(
    override val structureId: Uuid,
) : FindingsListRoute {
    companion object {
        const val URL_NAME = "findings"
    }
}

@Serializable
@Immutable
data class WebFindingDetailRoute(
    override val structureId: Uuid,
    override val findingId: Uuid,
) : FindingDetailRoute {
    companion object {
        const val URL_NAME = "finding-detail"
    }
}

class WebFindingsListRouteFactory : FindingsListRouteFactory {
    override fun create(structureId: Uuid): FindingsListRoute = WebFindingsListRoute(structureId)
}

class WebFindingDetailRouteFactory : FindingDetailRouteFactory {
    override fun create(
        structureId: Uuid,
        findingId: Uuid,
    ): FindingDetailRoute =
        WebFindingDetailRoute(
            structureId = structureId,
            findingId = findingId,
        )
}

@Serializable
@Immutable
data class WebFindingEditRoute(
    override val findingId: Uuid,
) : FindingEditRoute {
    companion object {
        const val URL_NAME = "edit-finding"
    }
}

class WebFindingEditRouteFactory : FindingEditRouteFactory {
    override fun create(
        structureId: Uuid,
        findingId: Uuid,
    ): FindingEditRoute = WebFindingEditRoute(findingId = findingId)
}

@Serializable
@Immutable
data class WebFindingCreationRoute(
    override val structureId: Uuid,
    val coordinateX: Float,
    val coordinateY: Float,
    override val findingTypeKey: String,
) : FindingCreationRoute {
    override val coordinate: RelativeCoordinate
        get() = RelativeCoordinate(coordinateX, coordinateY)

    companion object {
        const val URL_NAME = "create-finding"
    }
}

class WebFindingCreationRouteFactory : FindingCreationRouteFactory {
    override fun create(
        structureId: Uuid,
        coordinate: RelativeCoordinate,
        findingTypeKey: String,
    ): FindingCreationRoute =
        WebFindingCreationRoute(
            structureId = structureId,
            coordinateX = coordinate.x,
            coordinateY = coordinate.y,
            findingTypeKey = findingTypeKey,
        )
}
