package cz.adamec.timotej.snag

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import cz.adamec.timotej.snag.ui.App

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Snag",
    ) {
        App()
    }
}