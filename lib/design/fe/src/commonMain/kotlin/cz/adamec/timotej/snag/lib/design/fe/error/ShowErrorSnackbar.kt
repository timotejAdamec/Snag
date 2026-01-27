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

package cz.adamec.timotej.snag.lib.design.fe.error

import androidx.compose.material3.SnackbarDuration
import cz.adamec.timotej.snag.lib.design.fe.scaffold.AppScaffoldState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Suppress("LabeledExpression")
fun showErrorSnackbar(
    uiError: UiError?,
    scope: CoroutineScope,
    scaffoldState: AppScaffoldState,
) {
    if (uiError == null) return
    scope.launch {
        scaffoldState.snackbarHostState.showSnackbar(
            message = uiError.toInformativeMessage(),
            duration = SnackbarDuration.Long,
        )
    }
}
