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
data class NonWebStructureCreationRoute(
    override val projectId: Uuid,
) : StructureCreationRoute

@Serializable
@Immutable
data class NonWebStructureEditRoute(
    override val structureId: Uuid,
) : StructureEditRoute

class NonWebStructureCreationRouteFactory : StructureCreationRouteFactory {
    override fun create(projectId: Uuid): StructureCreationRoute = NonWebStructureCreationRoute(projectId)
}

class NonWebStructureEditRouteFactory : StructureEditRouteFactory {
    override fun create(structureId: Uuid): StructureEditRoute = NonWebStructureEditRoute(structureId)
}
