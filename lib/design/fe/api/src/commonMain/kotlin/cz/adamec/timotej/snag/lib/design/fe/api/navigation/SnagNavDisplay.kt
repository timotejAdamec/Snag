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

package cz.adamec.timotej.snag.lib.design.fe.api.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.scene.SceneStrategy
import androidx.navigation3.ui.NavDisplay
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationEventHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import cz.adamec.timotej.snag.lib.design.fe.api.scenes.InlineDialogSceneStrategy
import cz.adamec.timotej.snag.lib.navigation.fe.RegisterActiveBackStack
import cz.adamec.timotej.snag.lib.navigation.fe.SnagBackStack
import cz.adamec.timotej.snag.lib.navigation.fe.SnagNavRoute
import org.koin.compose.navigation3.koinEntryProvider

@Composable
fun SnagNavDisplay(
    backStack: SnagBackStack,
    modifier: Modifier = Modifier,
    additionalSceneStrategies: List<SceneStrategy<SnagNavRoute>> = emptyList(),
) {
    RegisterActiveBackStack(backStack = backStack)
    NavDisplay(
        modifier = modifier,
        backStack = backStack.value,
        onBack = { backStack.removeLastSafely() },
        entryProvider = koinEntryProvider<SnagNavRoute>(),
        sceneStrategies =
            listOf<SceneStrategy<SnagNavRoute>>(InlineDialogSceneStrategy()) + additionalSceneStrategies,
        entryDecorators =
            listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator(),
            ),
    )
    NavigationEventHandler(
        state = rememberNavigationEventState(NavigationEventInfo.None),
        isBackEnabled = backStack.value.size > 1,
        onBackCompleted = { backStack.removeLastSafely() },
    )
}
