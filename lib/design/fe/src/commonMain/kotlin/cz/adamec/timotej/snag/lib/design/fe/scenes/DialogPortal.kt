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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf

/**
 * Callback to render dialog content at the top of the composition tree,
 * above the navigation rail and all other UI. Set to `null` to dismiss.
 */
val LocalDialogPortal = compositionLocalOf<(((@Composable () -> Unit)?) -> Unit)?> { null }
