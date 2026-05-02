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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import com.github.terrakok.navigation3.browser.HierarchicalBrowserNavigation
import org.koin.mp.KoinPlatform.getKoin

@Composable
fun BrowserNavigation() {
    val builders = remember { getKoin().getAll<BrowserHistoryFragmentBuilder>() }
    val currentRoute =
        remember {
            derivedStateOf { ActiveBackStackRegistry.current?.value?.lastOrNull() }
        }
    HierarchicalBrowserNavigation(
        currentDestination = currentRoute,
        currentDestinationName = { route ->
            route?.let { resolved -> builders.find { it.handles(resolved) }?.build(resolved) }
        },
    )
}
