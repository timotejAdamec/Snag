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
import com.github.terrakok.navigation3.browser.buildBrowserHistoryFragment
import cz.adamec.timotej.snag.lib.navigation.fe.SnagBackStack
import cz.adamec.timotej.snag.projects.fe.driving.api.ProjectCreationRoute
import cz.adamec.timotej.snag.projects.fe.driving.api.ProjectEditRoute
import cz.adamec.timotej.snag.projects.fe.driving.api.WebProjectCreationRoute
import cz.adamec.timotej.snag.projects.fe.driving.api.WebProjectEditRoute
import cz.adamec.timotej.snag.projects.fe.driving.api.WebProjectsRoute

@Suppress("UseIfInsteadOfWhen")
@Composable
internal actual fun SnagNavigationPreparation(backStack: SnagBackStack) {
    HierarchicalBrowserNavigation(
        currentDestination = remember { derivedStateOf { backStack.value.lastOrNull() } },
        currentDestinationName = { key ->
            when (key) {
                is WebProjectsRoute -> buildBrowserHistoryFragment(key.URL_NAME)
                is WebProjectCreationRoute -> buildBrowserHistoryFragment(key.URL_NAME)
                is WebProjectEditRoute -> buildBrowserHistoryFragment(WebProjectEditRoute.URL_NAME, mapOf("id" to key.projectId.toString()))
                else -> null
            }
        },
    )
}
