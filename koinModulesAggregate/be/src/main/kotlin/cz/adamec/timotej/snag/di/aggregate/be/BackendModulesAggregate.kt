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

package cz.adamec.timotej.snag.di.aggregate.be

import cz.adamec.timotej.snag.clients.be.app.impl.di.clientsAppModule
import cz.adamec.timotej.snag.clients.be.driven.impl.di.clientsDrivenModule
import cz.adamec.timotej.snag.clients.be.driving.impl.di.clientsDrivingModule
import cz.adamec.timotej.snag.configuration.be.impl.di.configurationModule
import cz.adamec.timotej.snag.feat.inspections.be.app.impl.di.inspectionsAppModule
import cz.adamec.timotej.snag.feat.inspections.be.driven.impl.di.inspectionsDrivenModule
import cz.adamec.timotej.snag.feat.inspections.be.driving.impl.di.inspectionsDrivingModule
import cz.adamec.timotej.snag.feat.shared.database.be.di.sharedDatabaseModule
import cz.adamec.timotej.snag.feat.shared.storage.be.di.sharedStorageBeModule
import cz.adamec.timotej.snag.findings.be.app.impl.di.findingsAppModule
import cz.adamec.timotej.snag.findings.be.driven.impl.di.findingsDrivenModule
import cz.adamec.timotej.snag.findings.be.driving.impl.di.findingsDrivingModule
import cz.adamec.timotej.snag.lib.core.be.backendCoreModule
import cz.adamec.timotej.snag.lib.storage.be.impl.di.storageRoutingModule
import cz.adamec.timotej.snag.projects.be.app.impl.di.projectsAppModule
import cz.adamec.timotej.snag.projects.be.driven.impl.di.projectsDrivenModule
import cz.adamec.timotej.snag.projects.be.driving.impl.di.projectsDrivingModule
import cz.adamec.timotej.snag.reports.be.app.impl.di.reportsAppModule
import cz.adamec.timotej.snag.reports.be.driven.impl.di.reportsDrivenModule
import cz.adamec.timotej.snag.reports.be.driving.impl.di.reportsDrivingModule
import cz.adamec.timotej.snag.shared.rules.business.impl.di.sharedRulesModule
import cz.adamec.timotej.snag.structures.be.app.impl.di.structuresAppModule
import cz.adamec.timotej.snag.structures.be.driven.impl.di.structuresDrivenModule
import cz.adamec.timotej.snag.structures.be.driving.impl.di.structuresDrivingModule
import org.koin.dsl.module

val backendModulesAggregate =
    module {
        includes(
            backendCoreModule,
            configurationModule,
            sharedDatabaseModule,
            sharedRulesModule,
            storageRoutingModule,
            sharedStorageBeModule,
            projectsDrivingModule,
            projectsDrivenModule,
            projectsAppModule,
            clientsDrivingModule,
            clientsDrivenModule,
            clientsAppModule,
            structuresDrivingModule,
            structuresDrivenModule,
            structuresAppModule,
            findingsDrivingModule,
            findingsDrivenModule,
            findingsAppModule,
            inspectionsDrivingModule,
            inspectionsDrivenModule,
            inspectionsAppModule,
            reportsDrivingModule,
            reportsDrivenModule,
            reportsAppModule,
        )
    }
