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

import cz.adamec.timotej.snag.findings.be.app.impl.di.findingsAppModule
import cz.adamec.timotej.snag.findings.be.driven.impl.di.findingsDrivenModule
import cz.adamec.timotej.snag.findings.be.driving.impl.di.findingsDrivingModule
import cz.adamec.timotej.snag.configuration.be.impl.di.configurationModule
import cz.adamec.timotej.snag.lib.core.be.backendCoreModule
import cz.adamec.timotej.snag.lib.database.be.di.backendDatabaseModule
import cz.adamec.timotej.snag.projects.be.app.impl.di.projectsAppModule
import cz.adamec.timotej.snag.projects.be.driven.impl.di.projectsDrivenModule
import cz.adamec.timotej.snag.projects.be.driving.impl.di.projectsDrivingModule
import cz.adamec.timotej.snag.structures.be.app.impl.di.structuresAppModule
import cz.adamec.timotej.snag.structures.be.driven.impl.di.structuresDrivenModule
import cz.adamec.timotej.snag.structures.be.driving.impl.di.structuresDrivingModule
import org.koin.dsl.module

val backendModulesAggregate =
    module {
        includes(
            backendCoreModule,
            configurationModule,
            backendDatabaseModule,
            projectsDrivingModule,
            projectsDrivenModule,
            projectsAppModule,
            structuresDrivingModule,
            structuresDrivenModule,
            structuresAppModule,
            findingsDrivingModule,
            findingsDrivenModule,
            findingsAppModule,
        )
    }
