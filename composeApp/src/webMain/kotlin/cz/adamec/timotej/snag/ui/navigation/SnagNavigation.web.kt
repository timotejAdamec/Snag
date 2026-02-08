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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import com.github.terrakok.navigation3.browser.HierarchicalBrowserNavigation
import cz.adamec.timotej.snag.lib.navigation.fe.BrowserHistoryFragmentBuilder
import cz.adamec.timotej.snag.lib.navigation.fe.SnagBackStack
import org.koin.compose.getKoin

@Composable
internal actual fun SnagNavigationPreparation(backStack: SnagBackStack) {
    val builders = getKoin().getAll<BrowserHistoryFragmentBuilder>()

    HierarchicalBrowserNavigation(
        currentDestination = remember { derivedStateOf { backStack.value.lastOrNull() } },
        currentDestinationName = { route ->
            if (route == null) {
                ""
            } else {
                val builder =
                    builders.find { it.handles(route) }
                        ?: error("No BrowserHistoryFragmentBuilder found for route $route")
                builder.build(route)
            }
        },
    )
}
