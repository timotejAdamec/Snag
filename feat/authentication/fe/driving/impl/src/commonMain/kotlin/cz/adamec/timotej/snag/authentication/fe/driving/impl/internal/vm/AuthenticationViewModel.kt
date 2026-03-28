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
import cz.adamec.timotej.snag.authentication.fe.app.api.GetAuthenticatedUserIdUseCase
import cz.adamec.timotej.snag.core.foundation.common.mapState
import cz.adamec.timotej.snag.lib.design.fe.state.launchWhileSubscribed
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

internal class AuthenticationViewModel(
    private val getAuthenticatedUserIdUseCase: GetAuthenticatedUserIdUseCase,
) : ViewModel() {
    private val vmState: MutableStateFlow<AuthenticationVmState> =
        MutableStateFlow(AuthenticationVmState())
            .launchWhileSubscribed(scope = viewModelScope) {
                listOf(collectAuthState())
            }

    val state: StateFlow<AuthenticationUiState> =
        vmState.mapState { it.toUiState() }

    private fun collectAuthState(): Job =
        getAuthenticatedUserIdUseCase.currentUserId
            .map { userId ->
                vmState.update { it.copy(currentUserId = userId) }
            }.launchIn(viewModelScope)
}
