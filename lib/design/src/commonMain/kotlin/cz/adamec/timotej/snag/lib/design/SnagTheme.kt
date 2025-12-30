package cz.adamec.timotej.snag.lib.design

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
fun SnagTheme(content: @Composable () -> Unit) {
    MaterialTheme {
        content()
    }
}
