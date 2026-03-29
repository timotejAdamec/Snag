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

package cz.adamec.timotej.snag.authentication.fe.driving.impl.internal.vm

import androidx.compose.runtime.Immutable

@Immutable
internal data class AuthenticationUiState(
    val isAuthenticated: Boolean = false,
    val isLoggingIn: Boolean = false,
    val loginError: String? = null,
)
