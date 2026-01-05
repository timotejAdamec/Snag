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
import com.github.terrakok.navigation3.browser.HierarchicalBrowserNavigation
import com.github.terrakok.navigation3.browser.buildBrowserHistoryFragment
import cz.adamec.timotej.snag.lib.navigation.fe.NavRoute
import cz.adamec.timotej.snag.projects.fe.driving.api.WebProjectsRouteImpl

@Suppress("UseIfInsteadOfWhen")
@Composable
internal actual fun SnagNavigationPreparation(backStack: List<NavRoute>) {
    HierarchicalBrowserNavigation(
        currentDestinationName = {
            when (val key = backStack.lastOrNull()) {
                is WebProjectsRouteImpl -> buildBrowserHistoryFragment(key.URL_NAME)

                //                is Profile -> buildBrowserHistoryFragment("profile", mapOf("id" to key.id.toString()))
                else -> null
            }
        },
    )
}
