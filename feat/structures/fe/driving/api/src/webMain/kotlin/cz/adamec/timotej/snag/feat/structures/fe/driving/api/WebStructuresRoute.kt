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

class WebStructureCreationRouteFactory : StructureCreationRouteFactory {
    override fun create(projectId: Uuid): StructureCreationRoute = WebStructureCreationRoute(projectId)
}
