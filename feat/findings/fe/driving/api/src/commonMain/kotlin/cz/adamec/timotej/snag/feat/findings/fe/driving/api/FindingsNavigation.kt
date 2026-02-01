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

import cz.adamec.timotej.snag.lib.navigation.fe.SnagNavRoute
import kotlin.uuid.Uuid

interface FindingsListRoute : SnagNavRoute {
    val structureId: Uuid
}

interface FindingDetailRoute : SnagNavRoute {
    val findingId: Uuid
}

interface FindingsListRouteFactory {
    fun create(structureId: Uuid): FindingsListRoute
}

interface FindingDetailRouteFactory {
    fun create(findingId: Uuid): FindingDetailRoute
}

object FindingsSceneMetadata {
    const val FINDINGS_LIST_KEY = "FloorPlanScene-FindingsList"
    const val FINDING_DETAIL_KEY = "FloorPlanScene-FindingDetail"

    fun findingsListPane(): Map<String, Any> = mapOf(FINDINGS_LIST_KEY to true)

    fun findingDetailPane(): Map<String, Any> = mapOf(FINDING_DETAIL_KEY to true)
}
