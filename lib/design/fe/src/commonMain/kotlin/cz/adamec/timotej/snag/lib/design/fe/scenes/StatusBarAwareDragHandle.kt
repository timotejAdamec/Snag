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

package cz.adamec.timotej.snag.lib.design.fe.scenes

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.SheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

/**
 * A drag handle for bottom sheets that gradually adds status bar padding as the sheet
 * approaches the top of the screen.
 *
 * When a [BottomSheetScaffold][androidx.compose.material3.BottomSheetScaffold] sheet is
 * expanded, it can slide behind the status bar. This composable tracks the sheet's current
 * offset and smoothly introduces top padding so the handle (and the content below it) stays
 * clear of the status bar. The padding is zero when the sheet is far from the top and
 * reaches the full status bar height when the sheet is at the top.
 *
 * @param sheetState the [SheetState] driving the bottom sheet whose offset is tracked.
 * @param modifier optional [Modifier] applied to the outer container.
 */
@Composable
fun StatusBarAwareDragHandle(
    sheetState: SheetState,
    modifier: Modifier = Modifier,
) {
    val statusBarTop =
        WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val density = LocalDensity.current
    val statusBarTopPx = with(density) { statusBarTop.toPx() }
    val topPadding by remember {
        derivedStateOf {
            val offset =
                try {
                    sheetState.requireOffset()
                } catch (_: IllegalStateException) {
                    Float.MAX_VALUE
                }
            val threshold = statusBarTopPx * 2
            if (offset < threshold) {
                val fraction = 1f - (offset / threshold).coerceIn(0f, 1f)
                with(density) { (statusBarTopPx * fraction).toDp() }
            } else {
                0.dp
            }
        }
    }
    Box(modifier = modifier.padding(top = topPadding)) {
        BottomSheetDefaults.DragHandle()
    }
}
