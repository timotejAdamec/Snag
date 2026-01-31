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

package cz.adamec.timotej.snag.structures.fe.driving.impl.internal.structureDetails.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.adamec.timotej.snag.lib.core.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.lib.design.fe.error.UiError
import cz.adamec.timotej.snag.structures.fe.app.api.DeleteStructureUseCase
import cz.adamec.timotej.snag.structures.fe.app.api.GetStructureUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.InjectedParam
import kotlin.uuid.Uuid

internal class StructureDetailsViewModel(
    @InjectedParam private val structureId: Uuid,
    private val getStructureUseCase: GetStructureUseCase,
    private val deleteStructureUseCase: DeleteStructureUseCase,
) : ViewModel() {
    private val _state: MutableStateFlow<StructureDetailsUiState> =
        MutableStateFlow(StructureDetailsUiState())
    val state: StateFlow<StructureDetailsUiState> = _state

    private val errorEventsChannel = Channel<UiError>()
    val errorsFlow = errorEventsChannel.receiveAsFlow()

    private val deletedSuccessfullyEventChannel = Channel<Unit>()
    val deletedSuccessfullyEventFlow = deletedSuccessfullyEventChannel.receiveAsFlow()

    init {
        collectStructure()
    }

    private fun collectStructure() =
        viewModelScope.launch {
            getStructureUseCase(structureId).collect { result ->
                when (result) {
                    is OfflineFirstDataResult.ProgrammerError -> {
                        _state.update {
                            it.copy(status = StructureDetailsUiStatus.ERROR)
                        }
                        errorEventsChannel.send(UiError.Unknown)
                    }
                    is OfflineFirstDataResult.Success -> {
                        result.data?.let { structure ->
                            _state.update {
                                it.copy(
                                    status = StructureDetailsUiStatus.LOADED,
                                    structure = structure,
                                )
                            }
                        } ?: if (state.value.status != StructureDetailsUiStatus.DELETED) {
                            _state.update {
                                it.copy(status = StructureDetailsUiStatus.NOT_FOUND)
                            }
                        } else {
                            // keep as deleted
                        }
                    }
                }
            }
        }

    fun onDelete() =
        viewModelScope.launch {
            _state.update {
                it.copy(isBeingDeleted = true)
            }
            when (deleteStructureUseCase(structureId)) {
                is OfflineFirstDataResult.ProgrammerError -> {
                    _state.update {
                        it.copy(isBeingDeleted = false)
                    }
                    errorEventsChannel.send(UiError.Unknown)
                }

                is OfflineFirstDataResult.Success -> {
                    _state.update {
                        it.copy(
                            status = StructureDetailsUiStatus.DELETED,
                            isBeingDeleted = false,
                        )
                    }
                    deletedSuccessfullyEventChannel.send(Unit)
                }
            }
        }
}
