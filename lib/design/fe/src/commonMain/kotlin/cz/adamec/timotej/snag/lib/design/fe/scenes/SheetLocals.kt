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

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.dp

val LocalIsInSheet = compositionLocalOf { false }

val LocalSheetPeekHeight = compositionLocalOf { 0.dp }
