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
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
@Immutable
data class NonWebFindingsListRoute(
    override val structureId: Uuid,
) : FindingsListRoute

@Serializable
@Immutable
data class NonWebFindingDetailRoute(
    override val structureId: Uuid,
    override val findingId: Uuid,
) : FindingDetailRoute

class NonWebFindingsListRouteFactory : FindingsListRouteFactory {
    override fun create(structureId: Uuid): FindingsListRoute = NonWebFindingsListRoute(structureId)
}

class NonWebFindingDetailRouteFactory : FindingDetailRouteFactory {
    override fun create(
        structureId: Uuid,
        findingId: Uuid,
    ): FindingDetailRoute = NonWebFindingDetailRoute(
        structureId = structureId,
        findingId = findingId,
    )
}
