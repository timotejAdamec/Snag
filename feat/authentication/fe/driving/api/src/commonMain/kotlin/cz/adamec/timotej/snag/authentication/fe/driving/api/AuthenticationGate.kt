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

package cz.adamec.timotej.snag.authentication.fe.driving.api

import androidx.compose.runtime.Composable
import org.koin.compose.getKoin

@Composable
fun AuthenticationGate(authenticatedContent: @Composable () -> Unit) {
    val gate: AuthenticationGateContent = getKoin().get()
    gate(authenticatedContent = authenticatedContent)
}

interface AuthenticationGateContent {
    @Composable
    operator fun invoke(authenticatedContent: @Composable () -> Unit)
}
