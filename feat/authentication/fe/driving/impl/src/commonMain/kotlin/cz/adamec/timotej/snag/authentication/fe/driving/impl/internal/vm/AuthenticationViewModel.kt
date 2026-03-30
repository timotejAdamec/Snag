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

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.adamec.timotej.snag.authentication.fe.app.api.GetAuthProviderIdUseCase
import cz.adamec.timotej.snag.authentication.fe.app.api.LoginResult
import cz.adamec.timotej.snag.authentication.fe.app.api.LoginUseCase
import cz.adamec.timotej.snag.core.foundation.common.mapState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class AuthenticationViewModel(
    getAuthProviderIdUseCase: GetAuthProviderIdUseCase,
    private val loginUseCase: LoginUseCase,
) : ViewModel() {
    private val vmState: MutableStateFlow<AuthenticationVmState> =
        MutableStateFlow(AuthenticationVmState())

    val state: StateFlow<AuthenticationUiState> =
        vmState.mapState { it.toUiState() }

    init {
        getAuthProviderIdUseCase()
            .onEach { authProviderId ->
                vmState.update { it.copy(authProviderId = authProviderId) }
                if (authProviderId == null) login()
            }.launchIn(viewModelScope)
    }

    fun login() {
        viewModelScope.launch {
            vmState.update { it.copy(isLoggingIn = true, loginError = null) }
            when (val result = loginUseCase()) {
                is LoginResult.Success ->
                    vmState.update { it.copy(isLoggingIn = false) }
                is LoginResult.Error ->
                    vmState.update { it.copy(isLoggingIn = false, loginError = result.message) }
            }
        }
    }
}
