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

package cz.adamec.timotej.snag.ui

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.scene.DialogSceneStrategy
import androidx.navigation3.ui.NavDisplay
import cz.adamec.timotej.snag.lib.design.fe.scenes.ContentPaneSceneStrategy
import cz.adamec.timotej.snag.lib.navigation.fe.SnagNavRoute
import org.koin.compose.koinInject
import org.koin.compose.navigation3.koinEntryProvider

@Composable
internal fun MainNavigation() {
    val backStack: MainBackStack = koinInject()
    val entryProvider = koinEntryProvider<SnagNavRoute>()
    NavDisplay(
        backStack = backStack.value,
        entryProvider = entryProvider,
        sceneStrategies =
            listOf(
                DialogSceneStrategy(),
                ContentPaneSceneStrategy(),
            ),
        entryDecorators =
            listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator(),
            ),
    )
}
