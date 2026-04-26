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

package cz.adamec.timotej.snag.initializer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import com.github.terrakok.navigation3.browser.HierarchicalBrowserNavigation
import cz.adamec.timotej.snag.lib.design.fe.api.initializer.ComposeInitializer
import cz.adamec.timotej.snag.lib.navigation.fe.BrowserHistoryFragmentBuilder
import cz.adamec.timotej.snag.lib.navigation.fe.SnagBackStack
import kotlinx.coroutines.flow.filterNotNull
import org.koin.mp.KoinPlatform.getKoin

internal class SnagHierarchicalBrowserNavigationInitializer : ComposeInitializer {
    @Composable
    override fun init() {
        val allBackStacks = getKoin().getAll<SnagBackStack>()
        val lastRoute =
            remember {
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

        val builders = getKoin().getAll<BrowserHistoryFragmentBuilder>()
        HierarchicalBrowserNavigation(
            currentDestination = lastRoute,
            currentDestinationName = { route ->
                val builder =
                    builders.find { it.handles(route) }
                        ?: error("No BrowserHistoryFragmentBuilder found for route $route")
                builder.build(route)
            },
        )
    }
}
