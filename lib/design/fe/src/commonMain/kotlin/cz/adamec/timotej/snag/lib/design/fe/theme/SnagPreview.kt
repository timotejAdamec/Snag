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

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable

@Composable
fun SnagPreview(content: @Composable () -> Unit) {
    SnagTheme {
        Surface {
            content()
        }
    }
}
