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
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation3.scene.DialogSceneStrategy
import androidx.navigation3.ui.NavDisplay
import cz.adamec.timotej.snag.lib.navigation.fe.SnagBackStack
import cz.adamec.timotej.snag.lib.navigation.fe.SnagNavRoute
import cz.adamec.timotej.snag.projects.fe.driving.api.ProjectsRoute
import org.koin.compose.koinInject
import org.koin.compose.navigation3.koinEntryProvider

@Composable
internal fun SnagNavigation(
    modifier: Modifier = Modifier,
    backStack: SnagBackStack = koinInject(),
) {
    SnagNavigationPreparation(
        backStack = backStack,
    )
    val entryProvider = koinEntryProvider<SnagNavRoute>()
    val sceneStrategy = remember { DialogSceneStrategy<SnagNavRoute>() }
    NavDisplay(
        modifier = modifier,
        backStack = backStack.value,
        entryProvider = entryProvider,
        sceneStrategy = sceneStrategy,
    )
}

@Composable
internal expect fun SnagNavigationPreparation(backStack: SnagBackStack)
