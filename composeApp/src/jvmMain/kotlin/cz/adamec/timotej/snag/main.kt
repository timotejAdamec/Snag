package cz.adamec.timotej.snag

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Snag",
    ) {
        App()
    }
}
