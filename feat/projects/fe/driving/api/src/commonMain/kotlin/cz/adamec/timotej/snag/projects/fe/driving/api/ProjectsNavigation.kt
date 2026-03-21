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

package cz.adamec.timotej.snag.projects.fe.driving.api

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import cz.adamec.timotej.snag.lib.design.fe.scenes.ContentPaneSceneStrategy
import cz.adamec.timotej.snag.lib.design.fe.scenes.InlineDialogSceneStrategy
import cz.adamec.timotej.snag.lib.navigation.fe.SnagNavRoute
import org.koin.compose.koinInject
import org.koin.compose.navigation3.koinEntryProvider

@Composable
fun ProjectsNavigation(
    modifier: Modifier = Modifier,
    backStack: ProjectsBackStack = koinInject(),
) {
    ProjectsNavigationPreparation(
        backStack = backStack,
    )
    val entryProvider = koinEntryProvider<SnagNavRoute>()
    NavDisplay(
        modifier = modifier,
        backStack = backStack.value,
        entryProvider = entryProvider,
        sceneStrategies =
            listOf(
                InlineDialogSceneStrategy(),
                ContentPaneSceneStrategy(),
            ),
        entryDecorators =
            listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator(),
            ),
    )
}

@Composable
internal expect fun ProjectsNavigationPreparation(backStack: ProjectsBackStack)
