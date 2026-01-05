package cz.adamec.timotej.snag

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import co.touchlab.kermit.Logger
import co.touchlab.kermit.koin.KermitKoinLogger
import cz.adamec.timotej.snag.di.appModule
import cz.adamec.timotej.snag.ui.MainScreen
import org.koin.compose.KoinApplication
import org.koin.dsl.koinConfiguration

@Composable
@Preview
fun App() {
    KoinApplication(
        configuration =
        koinConfiguration(
            declaration = {
                logger(KermitKoinLogger(Logger.withTag("Koin")))
                modules(appModule)
            },
        ),
    ) {
        MainScreen()
    }
}
