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

package cz.adamec.timotej.snag.findings.fe.driving.impl.internal.findingsList.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.adamec.timotej.snag.core.foundation.common.mapState
import cz.adamec.timotej.snag.core.network.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.findings.fe.app.api.GetFindingsUseCase
import cz.adamec.timotej.snag.lib.design.fe.error.UiError
import cz.adamec.timotej.snag.lib.design.fe.state.launchWhileSubscribed
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.InjectedParam
import kotlin.uuid.Uuid

internal class FindingsListViewModel(
    @InjectedParam private val structureId: Uuid,
    private val getFindingsUseCase: GetFindingsUseCase,
) : ViewModel() {
    private val vmState: MutableStateFlow<FindingsListVmState> =
        MutableStateFlow(FindingsListVmState())
            .launchWhileSubscribed(scope = viewModelScope) {
                listOf(collectFindings())
            }
    val state: StateFlow<FindingsListUiState> =
        vmState.mapState { it.toUiState() }

    private val errorEventsChannel = Channel<UiError>()
    val errorsFlow = errorEventsChannel.receiveAsFlow()

    private fun collectFindings(): Job =
        viewModelScope.launch {
            getFindingsUseCase(structureId).collect { result ->
                when (result) {
                    is OfflineFirstDataResult.ProgrammerError -> {
                        vmState.update {
                            it.copy(status = FindingsListUiStatus.ERROR)
                        }
                    }

                    is OfflineFirstDataResult.Success -> {
                        vmState.update {
                            it.copy(
                                findings = result.data,
                            )
                        }
                    }
                }
            }
        }
}
