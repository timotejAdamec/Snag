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
    override val structureId: Uuid,
) : StructureEditRoute {
    companion object {
        const val URL_NAME = "edit-structure"
    }
}

class WebStructureCreationRouteFactory : StructureCreationRouteFactory {
    override fun create(projectId: Uuid): StructureCreationRoute = WebStructureCreationRoute(projectId)
}

class WebStructureEditRouteFactory : StructureEditRouteFactory {
    override fun create(structureId: Uuid): StructureEditRoute = WebStructureEditRoute(structureId)
}

@Serializable
@Immutable
data class WebStructureDetailRoute(
    override val structureId: Uuid,
) : StructureDetailRoute {
    companion object {
        const val URL_NAME = "structure-detail"
    }
}

class WebStructureDetailRouteFactory : StructureDetailRouteFactory {
    override fun create(structureId: Uuid): StructureDetailRoute = WebStructureDetailRoute(structureId)
}
