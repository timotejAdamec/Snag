package cz.adamec.timotej.snag.lib.design.fe.theme

import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.runtime.Composable

@Composable
fun SnagTheme(content: @Composable () -> Unit) {
    MaterialExpressiveTheme {
        content()
    }
}
