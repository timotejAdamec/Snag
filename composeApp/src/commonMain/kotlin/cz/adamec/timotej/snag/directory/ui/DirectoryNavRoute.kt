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

package cz.adamec.timotej.snag.directory.ui

import androidx.compose.runtime.Immutable
import cz.adamec.timotej.snag.lib.navigation.fe.SnagNavRoute
import kotlinx.serialization.Serializable

@Serializable
@Immutable
data class DirectoryNavRoute(
    val onExit: () -> Unit,
) : SnagNavRoute
