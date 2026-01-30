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

package cz.adamec.timotej.snag.di

import cz.adamec.timotej.snag.feat.shared.database.fe.di.databaseModule
import cz.adamec.timotej.snag.lib.core.fe.di.frontendCoreModule
import cz.adamec.timotej.snag.lib.sync.fe.app.di.syncAppModule
import cz.adamec.timotej.snag.lib.sync.fe.driven.impl.di.syncDrivenModule
import cz.adamec.timotej.snag.logging.loggerModule
import cz.adamec.timotej.snag.network.fe.di.networkModule
import cz.adamec.timotej.snag.projects.fe.app.di.projectsAppModule
import cz.adamec.timotej.snag.projects.fe.driven.di.projectsDrivenModule
import cz.adamec.timotej.snag.projects.fe.driving.api.di.projectsDrivingApiModule
import cz.adamec.timotej.snag.projects.fe.driving.impl.di.projectsDrivingImplModule
import cz.adamec.timotej.snag.structures.fe.app.di.structuresAppModule
import cz.adamec.timotej.snag.structures.fe.driven.di.structuresDrivenModule
import cz.adamec.timotej.snag.ui.navigation.navigationModule
import org.koin.dsl.module

val appModule =
    module {
        includes(
            frontendCoreModule,
            loggerModule,
            navigationModule,
            databaseModule,
            networkModule,
            syncDrivenModule,
            syncAppModule,
            mainModule,
            projectsDrivingApiModule,
            projectsDrivingImplModule,
            projectsDrivenModule,
            projectsAppModule,
            structuresDrivenModule,
            structuresAppModule,
        )
    }
