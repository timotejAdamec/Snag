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

import cz.adamec.timotej.snag.clients.fe.app.impl.di.clientsAppModule
import cz.adamec.timotej.snag.clients.fe.driven.di.clientsDrivenModule
import cz.adamec.timotej.snag.clients.fe.driving.api.di.clientsDrivingApiModule
import cz.adamec.timotej.snag.clients.fe.driving.impl.di.clientsDrivingImplModule
import cz.adamec.timotej.snag.configuration.fe.impl.di.configurationModule
import cz.adamec.timotej.snag.feat.findings.fe.driving.api.di.findingsDrivingApiModule
import cz.adamec.timotej.snag.feat.inspections.fe.app.impl.di.inspectionsAppModule
import cz.adamec.timotej.snag.feat.inspections.fe.driven.di.inspectionsDrivenModule
import cz.adamec.timotej.snag.feat.inspections.fe.driving.api.di.inspectionsDrivingApiModule
import cz.adamec.timotej.snag.feat.inspections.fe.driving.impl.di.inspectionsDrivingImplModule
import cz.adamec.timotej.snag.feat.shared.database.fe.impl.di.databaseModule
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
import cz.adamec.timotej.snag.shared.rules.business.impl.di.sharedRulesModule
import cz.adamec.timotej.snag.structures.fe.app.impl.di.structuresAppModule
import cz.adamec.timotej.snag.structures.fe.driven.di.structuresDrivenModule
import cz.adamec.timotej.snag.structures.fe.driving.impl.di.structuresDrivingImplModule
import org.koin.dsl.module

val frontendModulesAggregate =
    module {
        includes(
            frontendCoreModule,
            configurationModule,
            networkModule,
            databaseModule,
            sharedRulesModule,
            syncDrivenModule,
            syncAppModule,
            projectsAppModule,
            projectsDrivingApiModule,
            projectsDrivingImplModule,
            projectsDrivenModule,
            clientsAppModule,
            clientsDrivingApiModule,
            clientsDrivingImplModule,
            clientsDrivenModule,
            structuresAppModule,
            structuresDrivingApiModule,
            structuresDrivingImplModule,
            structuresDrivenModule,
            findingsAppModule,
            findingsDrivingApiModule,
            findingsDrivingImplModule,
            findingsDrivenModule,
            inspectionsAppModule,
            inspectionsDrivingApiModule,
            inspectionsDrivingImplModule,
            inspectionsDrivenModule,
        )
    }
