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

package cz.adamec.timotej.snag.di.aggregate.fe

import cz.adamec.timotej.snag.feat.findings.fe.driving.api.di.findingsDrivingApiModule
import cz.adamec.timotej.snag.feat.shared.database.fe.di.databaseModule
import cz.adamec.timotej.snag.feat.structures.fe.driving.api.di.structuresDrivingApiModule
import cz.adamec.timotej.snag.findings.fe.app.impl.di.findingsAppModule
import cz.adamec.timotej.snag.findings.fe.driven.di.findingsDrivenModule
import cz.adamec.timotej.snag.findings.fe.driving.impl.di.findingsDrivingImplModule
import cz.adamec.timotej.snag.lib.core.fe.di.frontendCoreModule
import cz.adamec.timotej.snag.lib.sync.fe.app.impl.di.syncAppModule
import cz.adamec.timotej.snag.lib.sync.fe.driven.impl.di.syncDrivenModule
import cz.adamec.timotej.snag.network.fe.di.networkModule
import cz.adamec.timotej.snag.projects.fe.app.impl.di.projectsAppModule
import cz.adamec.timotej.snag.projects.fe.driven.di.projectsDrivenModule
import cz.adamec.timotej.snag.projects.fe.driving.api.di.projectsDrivingApiModule
import cz.adamec.timotej.snag.projects.fe.driving.impl.di.projectsDrivingImplModule
import cz.adamec.timotej.snag.structures.fe.app.impl.di.structuresAppModule
import cz.adamec.timotej.snag.structures.fe.driven.di.structuresDrivenModule
import cz.adamec.timotej.snag.structures.fe.driving.impl.di.structuresDrivingImplModule
import org.koin.dsl.module

val frontendModulesAggregate =
    module {
        includes(
            frontendCoreModule,
            networkModule,
            databaseModule,
            syncDrivenModule,
            syncAppModule,
            projectsAppModule,
            projectsDrivingApiModule,
            projectsDrivingImplModule,
            projectsDrivenModule,
            structuresAppModule,
            structuresDrivingApiModule,
            structuresDrivingImplModule,
            structuresDrivenModule,
            findingsAppModule,
            findingsDrivingApiModule,
            findingsDrivingImplModule,
            findingsDrivenModule,
        )
    }
