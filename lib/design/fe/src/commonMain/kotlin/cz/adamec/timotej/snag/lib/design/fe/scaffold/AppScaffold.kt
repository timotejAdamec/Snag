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

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun AppScaffold(
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    content: @Composable (PaddingValues) -> Unit,
) {
    val appScaffoldState = remember { AppScaffoldState() }

    CompositionLocalProvider(LocalAppScaffoldState provides appScaffoldState) {
        Scaffold(
            modifier = modifier,
            containerColor = containerColor,
            snackbarHost = { SnackbarHost(appScaffoldState.snackbarHostState) },
        ) { paddingValues ->
            content(paddingValues)
        }
    }
}
