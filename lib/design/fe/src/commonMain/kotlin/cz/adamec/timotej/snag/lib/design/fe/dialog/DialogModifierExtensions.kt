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

package cz.adamec.timotej.snag.lib.design.fe.dialog

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cz.adamec.timotej.snag.lib.design.fe.layout.systemBarsPaddingCoerceAtLeast

private val DialogPaddingMinimum = 32.dp

@Composable
fun Modifier.dialogPadding(): Modifier = systemBarsPaddingCoerceAtLeast(DialogPaddingMinimum)
