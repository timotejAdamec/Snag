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

package cz.adamec.timotej.snag.clients.fe.driving.impl.internal.clients.vm

import androidx.compose.runtime.Immutable
import cz.adamec.timotej.snag.clients.fe.model.FrontendClient
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Immutable
data class ClientsUiState(
    val clients: ImmutableList<FrontendClient> = persistentListOf(),
    val isLoading: Boolean = false,
)
