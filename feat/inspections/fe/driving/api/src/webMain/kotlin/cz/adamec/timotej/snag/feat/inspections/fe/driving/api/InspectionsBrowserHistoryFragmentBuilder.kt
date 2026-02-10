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

package cz.adamec.timotej.snag.feat.inspections.fe.driving.api

import com.github.terrakok.navigation3.browser.buildBrowserHistoryFragment
import cz.adamec.timotej.snag.lib.navigation.fe.BrowserHistoryFragmentBuilder
import cz.adamec.timotej.snag.lib.navigation.fe.SnagNavRoute

internal class InspectionsBrowserHistoryFragmentBuilder : BrowserHistoryFragmentBuilder {
    override fun handles(route: SnagNavRoute): Boolean =
        route is WebInspectionCreationRoute ||
            route is WebInspectionEditRoute

    override fun build(route: SnagNavRoute): String =
        when (route) {
            is WebInspectionCreationRoute ->
                buildBrowserHistoryFragment(
                    WebInspectionCreationRoute.URL_NAME,
                    mapOf("projectId" to route.projectId.toString()),
                )
            is WebInspectionEditRoute ->
                buildBrowserHistoryFragment(
                    WebInspectionEditRoute.URL_NAME,
                    mapOf("id" to route.inspectionId.toString()),
                )
            else -> error("InspectionsBrowserHistoryFragmentBuilder cannot handle $route")
        }
}
