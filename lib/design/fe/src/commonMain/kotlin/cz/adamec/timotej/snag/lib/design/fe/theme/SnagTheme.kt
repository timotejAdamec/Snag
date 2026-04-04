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

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.expressiveLightColorScheme
import androidx.compose.runtime.Composable

object SnagTheme {

    val surfaceColors: SnagSurfaceColors = SnagSurfaceColors

    @Composable
    operator fun invoke(content: @Composable () -> Unit) {
        val colorScheme =
            if (isSystemInDarkTheme()) {
                darkColorScheme()
            } else {
                expressiveLightColorScheme()
            }

        MaterialExpressiveTheme(colorScheme = colorScheme) {
            content()
        }
    }
}
