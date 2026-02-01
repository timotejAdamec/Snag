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

package cz.adamec.timotej.snag.feat.structures.fe.driving.api.di

import cz.adamec.timotej.snag.feat.structures.fe.driving.api.NonWebStructureCreationRouteFactory
import cz.adamec.timotej.snag.feat.structures.fe.driving.api.NonWebStructureDetailRouteFactory
import cz.adamec.timotej.snag.feat.structures.fe.driving.api.NonWebStructureEditRouteFactory
import cz.adamec.timotej.snag.feat.structures.fe.driving.api.NonWebStructureFloorPlanRouteFactory
import cz.adamec.timotej.snag.feat.structures.fe.driving.api.StructureCreationRouteFactory
import cz.adamec.timotej.snag.feat.structures.fe.driving.api.StructureDetailRouteFactory
import cz.adamec.timotej.snag.feat.structures.fe.driving.api.StructureEditRouteFactory
import cz.adamec.timotej.snag.feat.structures.fe.driving.api.StructureFloorPlanRouteFactory
import org.koin.dsl.bind
import org.koin.dsl.module

internal actual val platformModule =
    module {
        factory { NonWebStructureCreationRouteFactory() } bind StructureCreationRouteFactory::class
        factory { NonWebStructureEditRouteFactory() } bind StructureEditRouteFactory::class
        factory { NonWebStructureFloorPlanRouteFactory() } bind StructureFloorPlanRouteFactory::class
        factory { NonWebStructureDetailRouteFactory() } bind StructureDetailRouteFactory::class
    }
