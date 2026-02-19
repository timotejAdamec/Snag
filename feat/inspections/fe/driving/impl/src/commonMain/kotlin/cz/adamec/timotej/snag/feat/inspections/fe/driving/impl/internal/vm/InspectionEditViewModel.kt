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

package cz.adamec.timotej.snag.feat.inspections.fe.driving.impl.internal.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.adamec.timotej.snag.feat.inspections.fe.app.api.GetInspectionUseCase
import cz.adamec.timotej.snag.feat.inspections.fe.app.api.SaveInspectionUseCase
import cz.adamec.timotej.snag.feat.inspections.fe.app.api.model.SaveInspectionRequest
import cz.adamec.timotej.snag.lib.core.common.Timestamp
import cz.adamec.timotej.snag.lib.core.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.lib.design.fe.error.UiError
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.InjectedParam
import kotlin.uuid.Uuid

internal class InspectionEditViewModel(
    @InjectedParam private val inspectionId: Uuid?,
    @InjectedParam private val projectId: Uuid?,
    private val getInspectionUseCase: GetInspectionUseCase,
    private val saveInspectionUseCase: SaveInspectionUseCase,
) : ViewModel() {
    private val _state: MutableStateFlow<InspectionEditUiState> =
        MutableStateFlow(InspectionEditUiState(projectId = projectId))
    val state: StateFlow<InspectionEditUiState> = _state

    private val errorEventsChannel = Channel<UiError>()
    val errorsFlow = errorEventsChannel.receiveAsFlow()

    private val saveEventChannel = Channel<Uuid>()
    val saveEventFlow = saveEventChannel.receiveAsFlow()

    init {
        require(inspectionId != null || projectId != null) {
            "Either inspectionId or projectId must be provided"
        }
        inspectionId?.let { collectInspection(it) }
    }

    private fun collectInspection(inspectionId: Uuid) =
        viewModelScope.launch {
            getInspectionUseCase(inspectionId).collect { result ->
                when (result) {
                    is OfflineFirstDataResult.ProgrammerError -> {
                        errorEventsChannel.send(UiError.Unknown)
                    }
                    is OfflineFirstDataResult.Success -> {
                        result.data?.let { data ->
                            _state.update {
                                it.copy(
                                    projectId = data.inspection.projectId,
                                    startedAt = data.inspection.startedAt,
                                    endedAt = data.inspection.endedAt,
                                    participants =
                                        data.inspection.participants.orEmpty(),
                                    climate = data.inspection.climate.orEmpty(),
                                    note = data.inspection.note.orEmpty(),
                                )
                            }
                            cancel()
                        }
                    }
                }
            }
        }

    fun onStartedAtChange(value: Timestamp?) {
        _state.update { it.copy(startedAt = value) }
    }

    fun onEndedAtChange(value: Timestamp?) {
        _state.update { it.copy(endedAt = value) }
    }

    fun onParticipantsChange(value: String) {
        _state.update { it.copy(participants = value) }
    }

    fun onClimateChange(value: String) {
        _state.update { it.copy(climate = value) }
    }

    fun onNoteChange(value: String) {
        _state.update { it.copy(note = value) }
    }

    fun onSaveInspection() =
        viewModelScope.launch {
            saveInspection()
        }

    private suspend fun saveInspection() {
        val currentState = state.value
        val result =
            saveInspectionUseCase(
                request =
                    SaveInspectionRequest(
                        id = inspectionId,
                        projectId = currentState.projectId ?: projectId!!,
                        startedAt = currentState.startedAt,
                        endedAt = currentState.endedAt,
                        participants =
                            currentState.participants.ifBlank { null },
                        climate = currentState.climate.ifBlank { null },
                        note = currentState.note.ifBlank { null },
                    ),
            )
        when (result) {
            is OfflineFirstDataResult.ProgrammerError -> {
                errorEventsChannel.send(UiError.Unknown)
            }
            is OfflineFirstDataResult.Success -> {
                saveEventChannel.send(result.data)
            }
        }
    }
}
