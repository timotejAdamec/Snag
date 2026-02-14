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

package cz.adamec.timotej.snag.feat.findings.fe.driving.api.di

import cz.adamec.timotej.snag.feat.findings.fe.driving.api.FindingCreationRouteFactory
import cz.adamec.timotej.snag.feat.findings.fe.driving.api.FindingDetailRouteFactory
import cz.adamec.timotej.snag.feat.findings.fe.driving.api.FindingEditRouteFactory
import cz.adamec.timotej.snag.feat.findings.fe.driving.api.FindingsListRouteFactory
import cz.adamec.timotej.snag.feat.findings.fe.driving.api.NonWebFindingCreationRouteFactory
import cz.adamec.timotej.snag.feat.findings.fe.driving.api.NonWebFindingDetailRouteFactory
import cz.adamec.timotej.snag.feat.findings.fe.driving.api.NonWebFindingEditRouteFactory
import cz.adamec.timotej.snag.feat.findings.fe.driving.api.NonWebFindingsListRouteFactory
import org.koin.dsl.bind
import org.koin.dsl.module

internal actual val platformModule =
    module {
        factory { NonWebFindingsListRouteFactory() } bind FindingsListRouteFactory::class
        factory { NonWebFindingDetailRouteFactory() } bind FindingDetailRouteFactory::class
        factory { NonWebFindingEditRouteFactory() } bind FindingEditRouteFactory::class
        factory { NonWebFindingCreationRouteFactory() } bind FindingCreationRouteFactory::class
    }
