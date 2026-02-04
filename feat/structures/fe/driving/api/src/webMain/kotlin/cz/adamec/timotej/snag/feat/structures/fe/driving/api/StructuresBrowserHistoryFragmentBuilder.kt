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

package cz.adamec.timotej.snag.feat.structures.fe.driving.api

import com.github.terrakok.navigation3.browser.buildBrowserHistoryFragment
import cz.adamec.timotej.snag.lib.navigation.fe.BrowserHistoryFragmentBuilder
import cz.adamec.timotej.snag.lib.navigation.fe.SnagNavRoute

internal class StructuresBrowserHistoryFragmentBuilder : BrowserHistoryFragmentBuilder {
    override fun handles(route: SnagNavRoute): Boolean =
        route is WebStructureCreationRoute ||
            route is WebStructureDetailNavRoute ||
            route is WebStructureEditRoute

    override fun build(route: SnagNavRoute): String =
        when (route) {
            is WebStructureCreationRoute ->
                buildBrowserHistoryFragment(
                    WebStructureCreationRoute.URL_NAME,
                    mapOf("projectId" to route.projectId.toString()),
                )
            is WebStructureDetailNavRoute ->
                buildBrowserHistoryFragment(
                    WebStructureDetailNavRoute.URL_NAME,
                    mapOf("id" to route.structureId.toString()),
                )
            is WebStructureEditRoute ->
                buildBrowserHistoryFragment(
                    WebStructureEditRoute.URL_NAME,
                    mapOf("id" to route.structureId.toString()),
                )
            else -> error("StructuresBrowserHistoryFragmentBuilder cannot handle $route")
        }
}
