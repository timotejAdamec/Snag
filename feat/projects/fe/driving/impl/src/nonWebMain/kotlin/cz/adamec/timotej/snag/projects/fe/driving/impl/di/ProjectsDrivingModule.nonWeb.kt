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

import cz.adamec.timotej.snag.projects.fe.driving.api.NonWebProjectsRouteImpl
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation

internal actual val platformModule =
    module {
        navigation<NonWebProjectsRouteImpl> { _ ->
            ProjectsScreenInjection()
        }
//        navigation<NonWebEditProjectRouteImpl> { route ->
//            ProjectDetailScreenInjection(
//                projectId = route.projectId,
//            )
//        }
    }
