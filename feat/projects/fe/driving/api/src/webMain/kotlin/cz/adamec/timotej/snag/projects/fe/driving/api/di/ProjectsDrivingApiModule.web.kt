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

import cz.adamec.timotej.snag.projects.fe.driving.api.ProjectCreationRoute
import cz.adamec.timotej.snag.projects.fe.driving.api.ProjectDetailRouteFactory
import cz.adamec.timotej.snag.projects.fe.driving.api.ProjectEditRouteFactory
import cz.adamec.timotej.snag.projects.fe.driving.api.ProjectsRoute
import cz.adamec.timotej.snag.projects.fe.driving.api.WebProjectCreationRoute
import cz.adamec.timotej.snag.projects.fe.driving.api.WebProjectDetailRouteFactory
import cz.adamec.timotej.snag.projects.fe.driving.api.WebProjectEditRouteFactory
import cz.adamec.timotej.snag.projects.fe.driving.api.WebProjectsRoute
import org.koin.dsl.bind
import org.koin.dsl.module

internal actual val platformModule =
    module {
        factory { WebProjectsRoute } bind ProjectsRoute::class
        factory { WebProjectCreationRoute } bind ProjectCreationRoute::class
        factory { WebProjectEditRouteFactory() } bind ProjectEditRouteFactory::class
        factory { WebProjectDetailRouteFactory() } bind ProjectDetailRouteFactory::class
    }
