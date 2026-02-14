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

import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.window.core.layout.WindowSizeClass.Companion.HEIGHT_DP_MEDIUM_LOWER_BOUND
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_EXPANDED_LOWER_BOUND
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_MEDIUM_LOWER_BOUND

@Composable
fun isScreenWide(): Boolean {
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    return windowSizeClass.isWidthAtLeastBreakpoint(
        widthDpBreakpoint = WIDTH_DP_MEDIUM_LOWER_BOUND,
    )
}

@Composable
fun isScreenExtraWide(): Boolean {
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    return windowSizeClass.isWidthAtLeastBreakpoint(
        widthDpBreakpoint = WIDTH_DP_EXPANDED_LOWER_BOUND,
    )
}

@Composable
fun isScreenLarge(): Boolean {
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    return windowSizeClass.isAtLeastBreakpoint(
        widthDpBreakpoint = WIDTH_DP_MEDIUM_LOWER_BOUND,
        heightDpBreakpoint = HEIGHT_DP_MEDIUM_LOWER_BOUND,
    )
}
