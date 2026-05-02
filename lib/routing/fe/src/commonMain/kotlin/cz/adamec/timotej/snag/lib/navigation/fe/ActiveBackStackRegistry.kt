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

package cz.adamec.timotej.snag.lib.navigation.fe

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateListOf

object ActiveBackStackRegistry {
    internal val stacks = mutableStateListOf<SnagBackStack>()

    val current: SnagBackStack?
        get() = stacks.lastOrNull()
}

@Composable
fun RegisterActiveBackStack(backStack: SnagBackStack) {
    DisposableEffect(backStack) {
        ActiveBackStackRegistry.stacks += backStack
        onDispose { ActiveBackStackRegistry.stacks -= backStack }
    }
}
