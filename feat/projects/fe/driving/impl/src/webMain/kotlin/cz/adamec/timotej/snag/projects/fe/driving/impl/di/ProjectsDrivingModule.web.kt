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

import cz.adamec.timotej.snag.feat.structures.fe.driving.api.WebStructureCreationRoute
import cz.adamec.timotej.snag.projects.fe.driving.api.WebProjectCreationRoute
import cz.adamec.timotej.snag.projects.fe.driving.api.WebProjectDetailRoute
import cz.adamec.timotej.snag.projects.fe.driving.api.WebProjectEditRoute
import cz.adamec.timotej.snag.projects.fe.driving.api.WebProjectsRoute
import org.koin.dsl.module

internal actual val platformModule =
    module {
        projectsScreenNavigation<WebProjectsRoute>(
            getProjectDetailRoute = { projectId ->
                WebProjectDetailRoute(projectId = projectId)
            },
        )
        projectDetailsEditScreenNavigation<WebProjectCreationRoute>(
            getProjectDetailRoute = { savedProjectId ->
                WebProjectDetailRoute(projectId = savedProjectId)
            },
        )
        projectDetailsEditScreenNavigation<WebProjectEditRoute>(
            getProjectDetailRoute = { savedProjectId ->
                WebProjectDetailRoute(projectId = savedProjectId)
            },
            getProjectId = { it.projectId },
        )
        projectDetailsScreenNavigation<WebProjectDetailRoute>(
            getStructureCreationRoute = { projectId ->
                WebStructureCreationRoute(projectId = projectId)
            },
        ) { it.projectId }
    }
