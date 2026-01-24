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

import androidx.navigation3.scene.DialogSceneStrategy
import cz.adamec.timotej.snag.projects.fe.driving.api.WebProjectCreationRoute
import cz.adamec.timotej.snag.projects.fe.driving.api.WebProjectEditRoute
import cz.adamec.timotej.snag.projects.fe.driving.api.WebProjectsRoute
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation

internal actual val platformModule =
    module {
        navigation<WebProjectsRoute> { _ ->
            ProjectsScreenInjection()
        }
        navigation<WebProjectCreationRoute>(
            metadata = DialogSceneStrategy.dialog(),
        ) { _ ->
            ProjectDetailsEditScreenInjection()
        }
        navigation<WebProjectEditRoute>(
            metadata = DialogSceneStrategy.dialog(),
        ) { route ->
            ProjectDetailsEditScreenInjection(
                projectId = route.projectId,
            )
        }
    }
