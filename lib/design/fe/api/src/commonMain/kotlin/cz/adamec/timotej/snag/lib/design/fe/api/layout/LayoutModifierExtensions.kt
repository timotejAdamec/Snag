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

package cz.adamec.timotej.snag.lib.design.fe.api.layout

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp

@Composable
fun Modifier.systemBarsPaddingCoerceAtLeast(
    start: Dp = 0.dp,
    top: Dp = 0.dp,
    end: Dp = 0.dp,
    bottom: Dp = 0.dp,
): Modifier {
    val systemBarsPadding = WindowInsets.systemBars.asPaddingValues()
    val padding =
        PaddingValues(
            start =
                systemBarsPadding
                    .calculateStartPadding(LayoutDirection.Ltr)
                    .coerceAtLeast(start),
            top =
                systemBarsPadding
                    .calculateTopPadding()
                    .coerceAtLeast(top),
            end =
                systemBarsPadding
                    .calculateEndPadding(LayoutDirection.Ltr)
                    .coerceAtLeast(end),
            bottom =
                systemBarsPadding
                    .calculateBottomPadding()
                    .coerceAtLeast(bottom),
        )
    return this
        .padding(padding)
        .consumeWindowInsets(WindowInsets.systemBars)
}

@Composable
fun Modifier.systemBarsPaddingCoerceAtLeast(all: Dp): Modifier = systemBarsPaddingCoerceAtLeast(all, all, all, all)
