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

package cz.adamec.timotej.snag.wear

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.Text
import cz.adamec.timotej.snag.authentication.fe.ports.AuthState
import cz.adamec.timotej.snag.authentication.fe.ports.AuthTokenProvider
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun WearAuthenticationGate(content: @Composable () -> Unit) {
    val authTokenProvider: AuthTokenProvider = koinInject()
    val authState by authTokenProvider.authState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    LaunchedEffect(authTokenProvider) {
        authTokenProvider.restoreSession()
    }

    when (authState) {
        AuthState.Loading ->
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }

        AuthState.Unauthenticated ->
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Chip(
                    onClick = { scope.launch { authTokenProvider.login() } },
                    label = { Text(text = "Sign in") },
                    colors = ChipDefaults.primaryChipColors(),
                )
            }

        is AuthState.Authenticated -> content()
    }
}
