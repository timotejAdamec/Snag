package cz.adamec.timotej.snag.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cz.adamec.timotej.snag.lib.design.fe.scaffold.AppScaffold
import cz.adamec.timotej.snag.lib.design.fe.theme.SnagTheme
import cz.adamec.timotej.snag.ui.navigation.SnagNavigation
import cz.adamec.timotej.snag.vm.MainViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun MainScreen(mainViewModel: MainViewModel = koinViewModel()) {
    SnagTheme {
        AppScaffold { paddingValues ->
            SnagNavigation(
                modifier = Modifier.padding(paddingValues),
            )
        }
    }
}
