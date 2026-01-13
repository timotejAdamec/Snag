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

package cz.adamec.timotej.snag.impl.di

import cz.adamec.timotej.snag.projects.be.app.di.projectsAppModule
import cz.adamec.timotej.snag.projects.be.driven.di.projectsDrivenModule
import cz.adamec.timotej.snag.projects.be.driving.impl.di.projectsDrivingModule
import cz.adamec.timotej.snag.structures.be.driving.impl.di.structuresDrivingModule
import org.koin.dsl.module

internal val appModule =
    module {
        includes(
            projectsDrivingModule,
            projectsDrivenModule,
            projectsAppModule,
            structuresDrivingModule,
        )
    }
