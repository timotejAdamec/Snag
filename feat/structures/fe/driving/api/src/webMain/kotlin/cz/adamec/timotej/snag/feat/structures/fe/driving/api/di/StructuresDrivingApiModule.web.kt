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

import cz.adamec.timotej.snag.feat.structures.fe.driving.api.StructureCreationRouteFactory
import cz.adamec.timotej.snag.feat.structures.fe.driving.api.StructureDetailRouteFactory
import cz.adamec.timotej.snag.feat.structures.fe.driving.api.StructureEditRouteFactory
import cz.adamec.timotej.snag.feat.structures.fe.driving.api.StructureFloorPlanRouteFactory
import cz.adamec.timotej.snag.feat.structures.fe.driving.api.StructuresBrowserHistoryFragmentBuilder
import cz.adamec.timotej.snag.feat.structures.fe.driving.api.WebStructureCreationRouteFactory
import cz.adamec.timotej.snag.feat.structures.fe.driving.api.WebStructureDetailRouteFactory
import cz.adamec.timotej.snag.feat.structures.fe.driving.api.WebStructureEditRouteFactory
import cz.adamec.timotej.snag.feat.structures.fe.driving.api.WebStructureFloorPlanRouteFactory
import cz.adamec.timotej.snag.lib.navigation.fe.BrowserHistoryFragmentBuilder
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module

internal actual val platformModule =
    module {
        factory { WebStructureCreationRouteFactory() } bind StructureCreationRouteFactory::class
        factory { WebStructureEditRouteFactory() } bind StructureEditRouteFactory::class
        factory { WebStructureFloorPlanRouteFactory() } bind StructureFloorPlanRouteFactory::class
        factory { WebStructureDetailRouteFactory() } bind StructureDetailRouteFactory::class
        factoryOf(::StructuresBrowserHistoryFragmentBuilder) bind BrowserHistoryFragmentBuilder::class
    }
