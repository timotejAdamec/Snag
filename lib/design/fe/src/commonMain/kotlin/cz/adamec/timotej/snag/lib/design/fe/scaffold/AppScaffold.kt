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

import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier

@Composable
fun AppScaffold(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val appScaffoldState = remember { AppScaffoldState() }

    CompositionLocalProvider(LocalAppScaffoldState provides appScaffoldState) {
        Scaffold(
            modifier = modifier,
            snackbarHost = { SnackbarHost(appScaffoldState.snackbarHostState) },
        ) {
            content()
        }
    }
}
