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

package cz.adamec.timotej.snag.clients.fe.driving.api

import com.github.terrakok.navigation3.browser.buildBrowserHistoryFragment
import cz.adamec.timotej.snag.lib.navigation.fe.BrowserHistoryFragmentBuilder
import cz.adamec.timotej.snag.lib.navigation.fe.SnagNavRoute

internal class ClientsBrowserHistoryFragmentBuilder : BrowserHistoryFragmentBuilder {
    override fun handles(route: SnagNavRoute): Boolean =
        route is WebClientsRoute ||
            route is WebClientCreationRoute ||
            route is WebClientEditRoute

    override fun build(route: SnagNavRoute): String =
        when (route) {
            is WebClientsRoute -> buildBrowserHistoryFragment(WebClientsRoute.URL_NAME)
            is WebClientCreationRoute -> buildBrowserHistoryFragment(WebClientCreationRoute.URL_NAME)
            is WebClientEditRoute ->
                buildBrowserHistoryFragment(
                    WebClientEditRoute.URL_NAME,
                    mapOf("id" to route.clientId.toString()),
                )
            else -> error("ClientsBrowserHistoryFragmentBuilder cannot handle $route")
        }
}
