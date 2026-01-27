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

package cz.adamec.timotej.snag.ui.navigation

import androidx.compose.runtime.mutableStateListOf
import cz.adamec.timotej.snag.lib.navigation.fe.SnagBackStack
import cz.adamec.timotej.snag.projects.fe.driving.api.ProjectsRoute
import org.koin.dsl.module

internal val navigationModule =
    module {
        single {
            SnagBackStack(
                value = mutableStateListOf(get<ProjectsRoute>()),
            )
        }
    }
