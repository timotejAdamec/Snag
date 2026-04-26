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
import cz.adamec.timotej.snag.lib.design.fe.api.navigation.SnagNavDisplay
import cz.adamec.timotej.snag.lib.design.fe.api.scenes.ContentPaneSceneStrategy
import org.koin.compose.koinInject

@Composable
fun ProjectsNavigation(modifier: Modifier = Modifier) {
    val backStack: ProjectsBackStack = koinInject()
    SnagNavDisplay(
        backStack = backStack,
        modifier = modifier,
        additionalSceneStrategies =
            listOf(
                ContentPaneSceneStrategy(),
            ),
    )
}
