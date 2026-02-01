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
    override fun create(findingId: Uuid): FindingDetailRoute = WebFindingDetailRoute(findingId)
}
