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

@file:Suppress("ktlint:standard:filename")

package cz.adamec.timotej.snag

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import cz.adamec.timotej.snag.lib.navigation.fe.SnagBackStack
import cz.adamec.timotej.snag.lib.navigation.fe.SnagHierarchicalBrowserNavigation
import cz.adamec.timotej.snag.lib.navigation.fe.SnagNavRoute
import kotlinx.coroutines.flow.filterNotNull
import org.koin.mp.KoinPlatform.getKoin

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    ComposeViewport {
        val allBackStacks = getKoin().getAll<SnagBackStack>()
        val lastRoute = remember {
            mutableStateOf(allBackStacks.first().value.last())
        }
        allBackStacks.forEach { backStack ->
            LaunchedEffect(backStack) {
                snapshotFlow { backStack.value.lastOrNull() }
                    .filterNotNull()
                    .collect { route ->
                        lastRoute.value = route
                    }
            }
        }
        SnagHierarchicalBrowserNavigation(lastRoute = lastRoute)

        App()
    }
}
