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
import cz.adamec.timotej.snag.lib.design.fe.scenes.ContentPaneSceneStrategy
import cz.adamec.timotej.snag.lib.navigation.fe.SnagNavDisplay
import org.koin.compose.koinInject

@Composable
internal fun MainNavigation() {
    val backStack: MainBackStack = koinInject()
    SnagNavDisplay(
        backStack = backStack.value,
        additionalSceneStrategies =
            listOf(
                ContentPaneSceneStrategy(),
            ),
    )
}
