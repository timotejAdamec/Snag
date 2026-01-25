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

package cz.adamec.timotej.snag.projects.fe.driving.api.di

import cz.adamec.timotej.snag.projects.fe.driving.api.NonWebProjectCreationRoute
import cz.adamec.timotej.snag.projects.fe.driving.api.NonWebProjectEditRouteFactory
import cz.adamec.timotej.snag.projects.fe.driving.api.NonWebProjectsRoute
import cz.adamec.timotej.snag.projects.fe.driving.api.ProjectCreationRoute
import cz.adamec.timotej.snag.projects.fe.driving.api.ProjectEditRouteFactory
import cz.adamec.timotej.snag.projects.fe.driving.api.ProjectsRoute
import org.koin.dsl.bind
import org.koin.dsl.module

internal actual val platformModule =
    module {
        factory { NonWebProjectsRoute } bind ProjectsRoute::class
        factory { NonWebProjectCreationRoute } bind ProjectCreationRoute::class
        factory { NonWebProjectEditRouteFactory() } bind ProjectEditRouteFactory::class
    }
