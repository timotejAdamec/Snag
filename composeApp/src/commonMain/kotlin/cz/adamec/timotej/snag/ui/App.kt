package cz.adamec.timotej.snag.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation3.ui.NavDisplay
import cz.adamec.timotej.snag.lib.design.theme.SnagTheme
import cz.adamec.timotej.snag.projects.fe.driving.api.ProjectsRoute
import cz.adamec.timotej.snag.lib.design.scaffold.AppScaffold
import cz.adamec.timotej.snag.ui.di.appModule
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
            AppScaffold{ paddingValues ->
                val backStack = remember { mutableStateListOf<Any>(ProjectsRoute) }
                val entryProvider = koinEntryProvider()
                NavDisplay(
                    modifier = Modifier.padding(paddingValues),
                    backStack = backStack,
                    entryProvider = entryProvider,
                )
            }
        }
    }
}
