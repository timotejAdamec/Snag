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

package cz.adamec.timotej.snag

import androidx.compose.runtime.Composable
import org.koin.core.module.Module

@Composable
internal actual fun KoinAppContainer(
    @Suppress("UNUSED_PARAMETER") extraModules: List<Module>,
    content: @Composable () -> Unit,
) {
    content()
}
