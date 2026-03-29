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

package cz.adamec.timotej.snag.authentication.fe.driving.impl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cz.adamec.timotej.snag.authentication.fe.driving.api.AuthenticationGateProvider
import cz.adamec.timotej.snag.authentication.fe.driving.impl.internal.LoginScreen
import cz.adamec.timotej.snag.authentication.fe.driving.impl.internal.vm.AuthenticationViewModel
import org.koin.compose.viewmodel.koinViewModel

internal class AuthenticationGateProviderImpl : AuthenticationGateProvider {
    @Composable
    override fun Content(authenticatedContent: @Composable () -> Unit) {
        val viewModel: AuthenticationViewModel = koinViewModel()
        val state by viewModel.state.collectAsStateWithLifecycle()

        if (state.isAuthenticated) {
            authenticatedContent()
        } else {
            LoginScreen(
                onRetry = viewModel::login,
                isLoading = state.isLoggingIn,
                errorMessage = state.loginError,
            )
        }
    }
}
