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

package cz.adamec.timotej.snag.findings.fe.driving.impl.internal.findingDetail.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.adamec.timotej.snag.findings.fe.app.api.DeleteFindingUseCase
import cz.adamec.timotej.snag.findings.fe.app.api.GetFindingUseCase
import cz.adamec.timotej.snag.lib.core.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.lib.design.fe.error.UiError
import cz.adamec.timotej.snag.lib.design.fe.error.UiError.Unknown
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.InjectedParam
import kotlin.uuid.Uuid

internal class FindingDetailViewModel(
    @InjectedParam private val findingId: Uuid,
    private val getFindingUseCase: GetFindingUseCase,
    private val deleteFindingUseCase: DeleteFindingUseCase,
) : ViewModel() {
    private val _state: MutableStateFlow<FindingDetailUiState> =
        MutableStateFlow(FindingDetailUiState())
    val state: StateFlow<FindingDetailUiState> = _state

    private val errorEventsChannel = Channel<UiError>()
    val errorsFlow = errorEventsChannel.receiveAsFlow()

    private val deletedSuccessfullyEventChannel = Channel<Unit>()
    val deletedSuccessfullyEventFlow = deletedSuccessfullyEventChannel.receiveAsFlow()

    init {
        collectFinding()
    }

    fun onDelete() =
        viewModelScope.launch {
            _state.update {
                it.copy(isBeingDeleted = true)
            }
            when (deleteFindingUseCase(findingId)) {
                is OfflineFirstDataResult.ProgrammerError -> {
                    _state.update {
                        it.copy(isBeingDeleted = false)
                    }
                    errorEventsChannel.send(Unknown)
                }

                is OfflineFirstDataResult.Success -> {
                    _state.update {
                        it.copy(
                            status = FindingDetailUiStatus.DELETED,
                            isBeingDeleted = false,
                        )
                    }
                    deletedSuccessfullyEventChannel.send(Unit)
                }
            }
        }

    private fun collectFinding() {
        viewModelScope.launch {
            getFindingUseCase(findingId).collect { result ->
                when (result) {
                    is OfflineFirstDataResult.ProgrammerError -> {
                        _state.update {
                            it.copy(status = FindingDetailUiStatus.ERROR)
                        }
                    }

                    is OfflineFirstDataResult.Success -> {
                        if (result.data == null) {
                            if (_state.value.status != FindingDetailUiStatus.DELETED) {
                                _state.update {
                                    it.copy(status = FindingDetailUiStatus.NOT_FOUND)
                                }
                            }
                        } else {
                            _state.update {
                                it.copy(
                                    status = FindingDetailUiStatus.LOADED,
                                    finding = result.data,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
