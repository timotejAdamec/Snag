/*
 * Copyright (c) 2026 Timotej Adamec
 * SPDX-License-Identifier: MIT
 *
 * This file is part of the thesis:
 * "Multiplatform snagging system with code sharing maximisation"
 *
 * Czech Technical University in Prague
 * Faculty of Information Technology
 * Department of Software Engineering
 */

package cz.adamec.timotej.snag.lib.design.fe.scaffold

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class AppScaffoldState(
    initialFabState: FabState = FabState.NotVisible,
    val snackbarHostState: SnackbarHostState = SnackbarHostState(),
) {
    var fabState by mutableStateOf(initialFabState)
}

interface FabState {
    data object NotVisible : FabState
    data class Visible(
        val text: String,
        val onClick: () -> Unit,
    ) : FabState
}

val LocalAppScaffoldState =
    compositionLocalOf<AppScaffoldState> {
        error("No AppScaffoldState provided")
    }
