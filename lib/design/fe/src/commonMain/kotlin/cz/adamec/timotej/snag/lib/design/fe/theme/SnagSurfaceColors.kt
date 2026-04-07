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

package cz.adamec.timotej.snag.lib.design.fe.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object SnagSurfaceColors {
    /**
     * Color for nav bar/rail and outer color on large screens.
     */
    val containerColor: Color
        @Composable get() = MaterialTheme.colorScheme.surfaceContainerLowest

    /**
     * Color for the top app bar of screens.
     */
    val screenAppBarColor: Color
        @Composable get() = MaterialTheme.colorScheme.surfaceContainerLow

    val sheetColor: Color
        @Composable get() = MaterialTheme.colorScheme.surface

    /**
     * Background of actual screens with content.
     */
    val screenContainerColor: Color
        @Composable get() = MaterialTheme.colorScheme.surface

    /**
     * Background of components like cards etc.
     */
    val screenContentContainersColor: Color
        @Composable get() = MaterialTheme.colorScheme.surfaceContainer

    /**
     * Components on top of components.
     */
    val screenContentContainersColorHigh: Color
        @Composable get() = MaterialTheme.colorScheme.surfaceContainerHigh
}
