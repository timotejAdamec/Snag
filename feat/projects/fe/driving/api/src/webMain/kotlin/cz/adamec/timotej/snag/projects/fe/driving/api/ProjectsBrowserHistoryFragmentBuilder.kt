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

import com.github.terrakok.navigation3.browser.buildBrowserHistoryFragment
import cz.adamec.timotej.snag.lib.navigation.fe.BrowserHistoryFragmentBuilder
import cz.adamec.timotej.snag.lib.navigation.fe.SnagNavRoute

internal class ProjectsBrowserHistoryFragmentBuilder : BrowserHistoryFragmentBuilder {
    override fun handles(route: SnagNavRoute): Boolean =
        route is WebProjectsRoute ||
            route is WebProjectCreationRoute ||
            route is WebProjectEditRoute ||
            route is WebProjectDetailRoute

    override fun build(route: SnagNavRoute): String =
        when (route) {
            is WebProjectsRoute -> buildBrowserHistoryFragment(WebProjectsRoute.URL_NAME)
            is WebProjectCreationRoute -> buildBrowserHistoryFragment(WebProjectCreationRoute.URL_NAME)
            is WebProjectEditRoute ->
                buildBrowserHistoryFragment(
                    WebProjectEditRoute.URL_NAME,
                    mapOf("id" to route.projectId.toString()),
                )
            is WebProjectDetailRoute ->
                buildBrowserHistoryFragment(
                    WebProjectDetailRoute.URL_NAME,
                    mapOf("id" to route.projectId.toString()),
                )
            else -> error("ProjectsBrowserHistoryFragmentBuilder cannot handle $route")
        }
}
