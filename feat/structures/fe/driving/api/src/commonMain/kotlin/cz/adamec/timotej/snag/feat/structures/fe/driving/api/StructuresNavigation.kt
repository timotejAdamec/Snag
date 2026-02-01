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

import cz.adamec.timotej.snag.lib.navigation.fe.SnagNavRoute
import kotlin.jvm.JvmInline
import kotlin.uuid.Uuid

interface StructureCreationRoute : SnagNavRoute {
    val projectId: Uuid
}

interface StructureEditRoute : SnagNavRoute {
    val structureId: Uuid
}

interface StructureFloorPlanRoute : StructureDetailNavRoute {
    override val structureId: Uuid
}

interface StructureCreationRouteFactory {
    fun create(projectId: Uuid): StructureCreationRoute
}

interface StructureEditRouteFactory {
    fun create(structureId: Uuid): StructureEditRoute
}

interface StructureFloorPlanRouteFactory {
    fun create(structureId: Uuid): StructureFloorPlanRoute
}

/**
 * Nav route for the structure detail nested navigation graph.
 */
interface StructureDetailNavRoute : SnagNavRoute {
    val structureId: Uuid
}

interface StructureDetailRouteFactory {
    fun create(structureId: Uuid): SnagNavRoute
}

@JvmInline
value class StructureDetailBackStack(
    val value: MutableList<StructureDetailNavRoute>,
) {
    fun removeLastSafely() {
        if (value.size > 1) value.removeLastOrNull()
    }
}
