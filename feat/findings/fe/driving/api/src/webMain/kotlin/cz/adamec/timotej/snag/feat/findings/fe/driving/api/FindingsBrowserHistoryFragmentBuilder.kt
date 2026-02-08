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

package cz.adamec.timotej.snag.feat.findings.fe.driving.api

import com.github.terrakok.navigation3.browser.buildBrowserHistoryFragment
import cz.adamec.timotej.snag.lib.navigation.fe.BrowserHistoryFragmentBuilder
import cz.adamec.timotej.snag.lib.navigation.fe.SnagNavRoute

internal class FindingsBrowserHistoryFragmentBuilder : BrowserHistoryFragmentBuilder {
    override fun handles(route: SnagNavRoute): Boolean = route is WebFindingEditRoute

    override fun build(route: SnagNavRoute): String =
        if (route is WebFindingEditRoute) {
            buildBrowserHistoryFragment(
                WebFindingEditRoute.URL_NAME,
                mapOf("id" to route.findingId.toString()),
            )
        } else {
            error("FindingsBrowserHistoryFragmentBuilder cannot handle $route")
        }
}
