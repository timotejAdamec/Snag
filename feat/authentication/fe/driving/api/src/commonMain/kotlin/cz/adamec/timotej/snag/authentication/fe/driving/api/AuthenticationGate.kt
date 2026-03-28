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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cz.adamec.timotej.snag.authentication.fe.app.api.AuthenticatedUserProvider
import org.koin.compose.getKoin

@Composable
fun AuthenticationGate(authenticatedContent: @Composable () -> Unit) {
    val authenticatedUserProvider = getKoin().get<AuthenticatedUserProvider>()
    val currentUserId by authenticatedUserProvider.currentUserId.collectAsStateWithLifecycle()

    if (currentUserId != null) {
        authenticatedContent()
    } else {
        LoginScreen()
    }
}

@Composable
private fun LoginScreen() {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "Snag",
            style = MaterialTheme.typography.displayMedium,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Sign in to continue",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = { /* MSAL/OAuth sign-in flow */ }) {
            Text("Sign in with Microsoft")
        }
    }
}
