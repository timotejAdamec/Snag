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
import androidx.compose.runtime.State
import com.github.terrakok.navigation3.browser.HierarchicalBrowserNavigation
import org.koin.compose.getKoin

@Composable
fun SnagHierarchicalBrowserNavigation(lastRoute: State<SnagNavRoute>) {
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
