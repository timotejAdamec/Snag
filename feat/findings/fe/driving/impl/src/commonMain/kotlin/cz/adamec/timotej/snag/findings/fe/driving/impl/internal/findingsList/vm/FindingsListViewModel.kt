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
import cz.adamec.timotej.snag.findings.fe.app.api.GetFindingsUseCase
import cz.adamec.timotej.snag.lib.core.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.lib.design.fe.error.UiError
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
    private val _state: MutableStateFlow<FindingsListUiState> =
        MutableStateFlow(FindingsListUiState())
    val state: StateFlow<FindingsListUiState> = _state

    private val errorEventsChannel = Channel<UiError>()
    val errorsFlow = errorEventsChannel.receiveAsFlow()

    init {
        collectFindings()
    }

    private fun collectFindings() {
        viewModelScope.launch {
            getFindingsUseCase(structureId).collect { result ->
                when (result) {
                    is OfflineFirstDataResult.ProgrammerError -> {
                        _state.update {
                            it.copy(status = FindingsListUiStatus.ERROR)
                        }
                    }

                    is OfflineFirstDataResult.Success -> {
                        _state.update {
                            it.copy(
                                status = FindingsListUiStatus.LOADED,
                                findings = result.data,
                            )
                        }
                    }
                }
            }
        }
    }
}
