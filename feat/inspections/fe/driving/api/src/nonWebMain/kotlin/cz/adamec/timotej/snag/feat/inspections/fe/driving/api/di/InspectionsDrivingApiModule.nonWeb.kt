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
import cz.adamec.timotej.snag.feat.inspections.fe.driving.api.NonWebInspectionCreationRouteFactory
import cz.adamec.timotej.snag.feat.inspections.fe.driving.api.NonWebInspectionEditRouteFactory
import org.koin.dsl.bind
import org.koin.dsl.module

internal actual val platformModule =
    module {
        factory { NonWebInspectionCreationRouteFactory() } bind InspectionCreationRouteFactory::class
        factory { NonWebInspectionEditRouteFactory() } bind InspectionEditRouteFactory::class
    }
