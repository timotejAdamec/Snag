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

import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.touchlab.kermit.Logger
import co.touchlab.kermit.koin.KermitKoinLogger
import cz.adamec.timotej.snag.core.foundation.fe.CurrentUserIdStore
import cz.adamec.timotej.snag.core.foundation.fe.Initializer
import cz.adamec.timotej.snag.di.appModule
import cz.adamec.timotej.snag.lib.design.fe.initializer.ComposeInitializer
import cz.adamec.timotej.snag.lib.design.fe.theme.SnagTheme
import cz.adamec.timotej.snag.ui.LoginScreen
import cz.adamec.timotej.snag.ui.MainScreen
import org.koin.compose.KoinApplication
import org.koin.compose.getKoin
import org.koin.dsl.koinConfiguration

@Composable
fun App() {
    KoinApplication(
        configuration =
            koinConfiguration(
                declaration = {
                    modules(appModule)
                    logger(KermitKoinLogger(Logger.withTag("Koin")))
                },
            ),
    ) {
        InitializeInitializers(
            uninitializedContent = { ContainedLoadingIndicator() },
        ) {
            AuthenticationGate()
        }
    }
}

@Composable
private fun AuthenticationGate() {
    val currentUserIdStore = getKoin().get<CurrentUserIdStore>()
    val currentUserId by currentUserIdStore.currentUserId.collectAsStateWithLifecycle()

    if (currentUserId != null) {
        MainScreen()
    } else {
        SnagTheme {
            LoginScreen(
                onSignIn = {
                    // In production, this triggers the MSAL interactive sign-in flow.
                    // In mock mode, the user ID is set immediately so this screen is never shown.
                },
            )
        }
    }
}

@Composable
fun InitializeInitializers(
    uninitializedContent: @Composable () -> Unit,
    initializedContent: @Composable () -> Unit,
) {
    val composeInitializers = getKoin().getAll<ComposeInitializer>()
    composeInitializers.forEach { it.init() }

    val initializers = getKoin().getAll<Initializer>()
    var isInitialized by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        initializers.forEach { it.init() }
        isInitialized = true
    }

    if (isInitialized) {
        initializedContent()
    } else {
        uninitializedContent()
    }
}
