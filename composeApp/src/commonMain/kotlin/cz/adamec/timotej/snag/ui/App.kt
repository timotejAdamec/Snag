package cz.adamec.timotej.snag.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation3.ui.NavDisplay
import cz.adamec.timotej.snag.lib.design.SnagTheme
import cz.adamec.timotej.snag.projects.fe.driving.api.ProjectsRoute
import org.koin.compose.KoinApplication
import org.koin.compose.navigation3.koinEntryProvider
import org.koin.dsl.koinConfiguration

@Composable
@Preview
fun App() {
    KoinApplication(
        configuration = koinConfiguration(declaration = { modules(appModule) }),
    ) {
        SnagTheme {
            val backStack = remember { mutableStateListOf<Any>(ProjectsRoute) }
            val entryProvider = koinEntryProvider()

            NavDisplay(
                backStack = backStack,
                entryProvider = entryProvider,
            )
        }
    }
}
