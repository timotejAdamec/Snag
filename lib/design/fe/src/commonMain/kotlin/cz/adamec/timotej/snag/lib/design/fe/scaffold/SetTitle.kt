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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString

@Composable
fun SetTitle(stringResource: StringResource) {
    val scaffoldState = LocalAppScaffoldState.current
    LaunchedEffect(Unit) {
        scaffoldState.title.value = getString(stringResource)
    }
}
