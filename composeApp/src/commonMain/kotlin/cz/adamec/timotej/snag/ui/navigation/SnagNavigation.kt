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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation3.ui.NavDisplay
import cz.adamec.timotej.snag.lib.navigation.fe.NavRoute
import cz.adamec.timotej.snag.projects.fe.driving.api.ProjectsRoute
import org.koin.compose.koinInject
import org.koin.compose.navigation3.koinEntryProvider

@Composable
internal fun SnagNavigation(modifier: Modifier = Modifier) {
    val initRoute = koinInject<ProjectsRoute>()
    val backStack = remember { mutableStateListOf<NavRoute>(initRoute) }
    SnagNavigationPreparation(
        backStack = backStack,
    )
    val entryProvider = koinEntryProvider()
    NavDisplay(
        modifier = modifier,
        backStack = backStack,
        entryProvider = entryProvider,
    )
}

@Composable
internal expect fun SnagNavigationPreparation(backStack: MutableList<NavRoute>)
