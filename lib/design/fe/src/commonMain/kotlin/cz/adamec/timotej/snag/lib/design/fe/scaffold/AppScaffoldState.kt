package cz.adamec.timotej.snag.lib.design.fe.scaffold

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateOf

class AppScaffoldState(
    val title: MutableState<String> = mutableStateOf("")
)

val LocalAppScaffoldState = compositionLocalOf<AppScaffoldState> {
    error("No AppScaffoldState provided")
}
