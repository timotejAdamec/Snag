package cz.adamec.timotej.snag.ui.navigation

import androidx.compose.runtime.Composable
import com.github.terrakok.navigation3.browser.HierarchicalBrowserNavigation
import com.github.terrakok.navigation3.browser.buildBrowserHistoryFragment
import cz.adamec.timotej.snag.lib.navigation.fe.NavRoute
import cz.adamec.timotej.snag.projects.fe.driving.api.WebProjectsRouteImpl

@Composable
internal actual fun SnagNavigationPreparation(backStack: MutableList<NavRoute>) {
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
