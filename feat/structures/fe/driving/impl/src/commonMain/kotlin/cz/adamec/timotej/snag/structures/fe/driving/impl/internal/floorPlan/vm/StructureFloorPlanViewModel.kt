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

package cz.adamec.timotej.snag.structures.fe.driving.impl.internal.floorPlan.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.adamec.timotej.snag.findings.fe.app.api.GetFindingsUseCase
import cz.adamec.timotej.snag.lib.core.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.lib.design.fe.error.UiError
import cz.adamec.timotej.snag.lib.design.fe.error.UiError.Unknown
import cz.adamec.timotej.snag.lib.design.fe.state.DEFAULT_NO_STATE_SUBSCRIBER_TIMEOUT
import cz.adamec.timotej.snag.structures.fe.app.api.DeleteStructureUseCase
import cz.adamec.timotej.snag.structures.fe.app.api.GetStructureUseCase
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.InjectedParam
import kotlin.uuid.Uuid

internal class StructureFloorPlanViewModel(
    @InjectedParam private val structureId: Uuid,
    private val getStructureUseCase: GetStructureUseCase,
    private val deleteStructureUseCase: DeleteStructureUseCase,
    private val getFindingsUseCase: GetFindingsUseCase,
) : ViewModel() {
    private val _state: MutableStateFlow<StructureDetailsUiState> =
        MutableStateFlow(StructureDetailsUiState())
    val state: StateFlow<StructureDetailsUiState> =
        _state
            .scan(
                initial = StructureDetailsUiState(),
            ) { prev, new ->
                @Suppress("LabeledExpression")
                val newStructureId = new.feStructure?.structure?.id ?: return@scan new
                if (prev.feStructure?.structure?.id != newStructureId) {
                    collectFindings(newStructureId)
                }
                new
            }.stateIn(
                scope = viewModelScope,
                started =
                    SharingStarted.WhileSubscribed(
                        stopTimeoutMillis = DEFAULT_NO_STATE_SUBSCRIBER_TIMEOUT,
                    ),
                initialValue = StructureDetailsUiState(),
            )

    private val errorEventsChannel = Channel<UiError>()
    val errorsFlow = errorEventsChannel.receiveAsFlow()

    private val deletedSuccessfullyEventChannel = Channel<Unit>()
    val deletedSuccessfullyEventFlow = deletedSuccessfullyEventChannel.receiveAsFlow()

    private var collectFindingsJob: Job? = null

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
                        errorEventsChannel.send(Unknown)
                    }

                    is OfflineFirstDataResult.Success -> {
                        result.data?.let { structure ->
                            _state.update {
                                it.copy(
                                    status = StructureDetailsUiStatus.LOADED,
                                    feStructure = structure,
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

    private fun collectFindings(structureId: Uuid) {
        if (collectFindingsJob?.isActive == true) collectFindingsJob?.cancel()
        collectFindingsJob =
            viewModelScope.launch {
                getFindingsUseCase(structureId)
                    .collect { result ->
                        when (result) {
                            is OfflineFirstDataResult.Success -> {
                                _state.update {
                                    it.copy(
                                        findings = result.data.toPersistentList(),
                                    )
                                }
                            }
                            is OfflineFirstDataResult.ProgrammerError -> {
                                errorEventsChannel.send(Unknown)
                            }
                        }
                    }
            }
    }

    fun onFindingSelected(findingId: Uuid?) {
        _state.update { it.copy(selectedFindingId = findingId) }
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
                    errorEventsChannel.send(Unknown)
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
