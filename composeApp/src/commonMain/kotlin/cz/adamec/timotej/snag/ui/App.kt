package cz.adamec.timotej.snag.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import cz.adamec.timotej.snag.lib.design.scaffold.AppScaffold
import cz.adamec.timotej.snag.lib.design.theme.SnagTheme
import cz.adamec.timotej.snag.ui.di.appModule
import cz.adamec.timotej.snag.ui.navigation.SnagNavigation
import org.koin.compose.KoinApplication
import org.koin.dsl.koinConfiguration

@Composable
@Preview
fun App() {
    KoinApplication(
        configuration = koinConfiguration(declaration = { modules(appModule) }),
    ) {
        SnagTheme {
            AppScaffold{ paddingValues ->
                SnagNavigation(
                    modifier = Modifier.padding(paddingValues),
                )
            }
        }
    }
}
