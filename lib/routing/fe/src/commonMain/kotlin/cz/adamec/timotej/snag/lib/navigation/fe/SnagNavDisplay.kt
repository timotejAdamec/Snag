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

package cz.adamec.timotej.snag.lib.navigation.fe

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.scene.DialogSceneStrategy
import androidx.navigation3.scene.SceneStrategy
import androidx.navigation3.ui.NavDisplay
import org.koin.compose.navigation3.koinEntryProvider

@Composable
fun SnagNavDisplay(
    backStack: SnagBackStack,
    modifier: Modifier = Modifier,
    additionalSceneStrategies: List<SceneStrategy<SnagNavRoute>> = emptyList(),
) {
    BackHandler(enabled = backStack.value.size > 1) {
        backStack.removeLastSafely()
    }
    NavDisplay(
        modifier = modifier,
        backStack = backStack.value,
        onBack = {},
        entryProvider = koinEntryProvider<SnagNavRoute>(),
        sceneStrategies =
            listOf<SceneStrategy<SnagNavRoute>>(DialogSceneStrategy()) + additionalSceneStrategies,
        entryDecorators =
            listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator(),
            ),
    )
}
