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

package cz.adamec.timotej.snag.users.fe.driving.api

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.scene.DialogSceneStrategy
import androidx.navigation3.ui.NavDisplay
import cz.adamec.timotej.snag.lib.design.fe.scenes.ContentPaneSceneStrategy
import org.koin.compose.koinInject
import org.koin.compose.navigation3.koinEntryProvider

@Composable
fun UsersNavigation(
    modifier: Modifier = Modifier,
    backStack: UsersBackStack = koinInject(),
) {
    val entryProvider = koinEntryProvider<UsersNavRoute>()
    val sceneStrategy =
        remember {
            DialogSceneStrategy<UsersNavRoute>() then
                ContentPaneSceneStrategy()
        }
    NavDisplay(
        modifier = modifier,
        backStack = backStack.value,
        entryProvider = entryProvider,
        sceneStrategy = sceneStrategy,
        entryDecorators =
            listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator(),
            ),
    )
}
