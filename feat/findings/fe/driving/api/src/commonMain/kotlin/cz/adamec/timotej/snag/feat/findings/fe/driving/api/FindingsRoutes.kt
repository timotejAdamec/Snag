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

import cz.adamec.timotej.snag.feat.findings.business.RelativeCoordinate
import cz.adamec.timotej.snag.feat.structures.fe.driving.api.StructureDetailNavRoute
import cz.adamec.timotej.snag.lib.navigation.fe.SnagNavRoute
import kotlin.uuid.Uuid

interface FindingsListRoute : StructureDetailNavRoute {
    override val structureId: Uuid
}

interface FindingDetailRoute : StructureDetailNavRoute {
    val findingId: Uuid
}

interface FindingEditRoute : SnagNavRoute {
    val findingId: Uuid
}

interface FindingsListRouteFactory {
    fun create(structureId: Uuid): FindingsListRoute
}

interface FindingDetailRouteFactory {
    fun create(
        structureId: Uuid,
        findingId: Uuid,
    ): FindingDetailRoute
}

interface FindingEditRouteFactory {
    fun create(
        structureId: Uuid,
        findingId: Uuid,
    ): FindingEditRoute
}

interface FindingCreationRoute : SnagNavRoute {
    val structureId: Uuid
    val coordinate: RelativeCoordinate
    val findingTypeKey: String
}

interface FindingCreationRouteFactory {
    fun create(
        structureId: Uuid,
        coordinate: RelativeCoordinate,
        findingTypeKey: String,
    ): FindingCreationRoute
}
