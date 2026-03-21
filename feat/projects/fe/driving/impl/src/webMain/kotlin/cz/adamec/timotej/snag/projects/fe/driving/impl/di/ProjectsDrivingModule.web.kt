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

import cz.adamec.timotej.snag.projects.fe.driving.api.WebProjectCreationRoute
import cz.adamec.timotej.snag.projects.fe.driving.api.WebProjectDetailRoute
import cz.adamec.timotej.snag.projects.fe.driving.api.WebProjectEditRoute
import cz.adamec.timotej.snag.projects.fe.driving.api.WebProjectsRoute
import org.koin.dsl.module

internal actual val platformModule =
    module {
        projectsScreenNavigation<WebProjectsRoute>()
        projectCreationScreenNavigation<WebProjectCreationRoute>()
        projectEditScreenNavigation<WebProjectEditRoute>()
        projectDetailsScreenNavigation<WebProjectDetailRoute>()
    }
