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

package cz.adamec.timotej.snag.projects.fe.driving.impl.di

import cz.adamec.timotej.snag.projects.fe.driving.api.NonWebProjectCreationRoute
import cz.adamec.timotej.snag.projects.fe.driving.api.NonWebProjectDetailRoute
import cz.adamec.timotej.snag.projects.fe.driving.api.NonWebProjectEditRoute
import cz.adamec.timotej.snag.projects.fe.driving.api.NonWebProjectsRoute
import org.koin.dsl.module

internal actual val platformModule =
    module {
        projectsScreenNavigation<NonWebProjectsRoute>(
            getProjectDetailRoute = { projectId ->
                NonWebProjectDetailRoute(projectId = projectId)
            }
        )
        projectDetailsEditScreenNavigation<NonWebProjectCreationRoute>(
            getProjectDetailRoute = { savedProjectId ->
                NonWebProjectDetailRoute(projectId = savedProjectId)
            }
        )
        projectDetailsEditScreenNavigation<NonWebProjectEditRoute>(
            getProjectDetailRoute = { savedProjectId ->
                NonWebProjectDetailRoute(projectId = savedProjectId)
            },
            getProjectId = { it.projectId }
        )
        projectDetailsScreenNavigation<NonWebProjectDetailRoute> { it.projectId }
    }
