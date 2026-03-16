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

package cz.adamec.timotej.snag.lib.design.fe.adaptive

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

val ContentPaneSpacing = 12.dp

val LocalIsInContentPane = compositionLocalOf { false }

object ContentPaneDefaults {
    val containerColor: Color
        @Composable get() = MaterialTheme.colorScheme.surfaceContainerLow

    // Change this will not override color for everything, will need changes
    // so that this color is used where it needs to be used.
    val paneColor: Color
        @Composable get() = MaterialTheme.colorScheme.surface
}

@Composable
fun ContentPane(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.extraLarge,
        color = ContentPaneDefaults.paneColor,
    ) {
        CompositionLocalProvider(LocalIsInContentPane provides true) {
            content()
        }
    }
}
