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

package cz.adamec.timotej.snag.feat.inspections.fe.driving.api.di

import cz.adamec.timotej.snag.feat.inspections.fe.driving.api.InspectionCreationRouteFactory
import cz.adamec.timotej.snag.feat.inspections.fe.driving.api.InspectionEditRouteFactory
import cz.adamec.timotej.snag.feat.inspections.fe.driving.api.InspectionsBrowserHistoryFragmentBuilder
import cz.adamec.timotej.snag.feat.inspections.fe.driving.api.WebInspectionCreationRouteFactory
import cz.adamec.timotej.snag.feat.inspections.fe.driving.api.WebInspectionEditRouteFactory
import cz.adamec.timotej.snag.lib.navigation.fe.BrowserHistoryFragmentBuilder
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module

internal actual val platformModule =
    module {
        factory { WebInspectionCreationRouteFactory() } bind InspectionCreationRouteFactory::class
        factory { WebInspectionEditRouteFactory() } bind InspectionEditRouteFactory::class
        factoryOf(::InspectionsBrowserHistoryFragmentBuilder) bind BrowserHistoryFragmentBuilder::class
    }
