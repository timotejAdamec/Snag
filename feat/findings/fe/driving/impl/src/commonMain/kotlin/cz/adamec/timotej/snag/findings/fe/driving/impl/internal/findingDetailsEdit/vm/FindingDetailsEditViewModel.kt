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

package cz.adamec.timotej.snag.findings.fe.driving.impl.internal.findingDetailsEdit.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.adamec.timotej.snag.feat.findings.business.Importance
import cz.adamec.timotej.snag.feat.findings.business.Term
import cz.adamec.timotej.snag.findings.fe.app.api.GetFindingUseCase
import cz.adamec.timotej.snag.findings.fe.app.api.SaveFindingDetailsUseCase
import cz.adamec.timotej.snag.findings.fe.app.api.SaveNewFindingUseCase
import cz.adamec.timotej.snag.findings.fe.app.api.model.SaveFindingDetailsRequest
import cz.adamec.timotej.snag.findings.fe.app.api.model.SaveNewFindingRequest
import cz.adamec.timotej.snag.lib.core.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.lib.core.fe.OfflineFirstUpdateDataResult
import cz.adamec.timotej.snag.lib.design.fe.error.UiError
import cz.adamec.timotej.snag.lib.design.fe.error.UiError.CustomUserMessage
import cz.adamec.timotej.snag.lib.design.fe.error.UiError.Unknown
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.InjectedParam
import kotlin.uuid.Uuid

internal class FindingDetailsEditViewModel(
    @InjectedParam private val findingId: Uuid?,
    @InjectedParam private val structureId: Uuid?,
    private val getFindingUseCase: GetFindingUseCase,
    private val saveNewFindingUseCase: SaveNewFindingUseCase,
    private val saveFindingDetailsUseCase: SaveFindingDetailsUseCase,
) : ViewModel() {
    private val _state: MutableStateFlow<FindingDetailsEditUiState> =
        MutableStateFlow(FindingDetailsEditUiState())
    val state: StateFlow<FindingDetailsEditUiState> = _state

    private val errorEventsChannel = Channel<UiError>()
    val errorsFlow = errorEventsChannel.receiveAsFlow()

    private val saveEventChannel = Channel<Uuid>()
    val saveEventFlow = saveEventChannel.receiveAsFlow()

    init {
        require(findingId != null || structureId != null) {
            "Either findingId or structureId must be provided"
        }
        findingId?.let { collectFinding(it) }
    }

    private fun collectFinding(findingId: Uuid) =
        viewModelScope.launch {
            getFindingUseCase(findingId).collect { result ->
                when (result) {
                    is OfflineFirstDataResult.ProgrammerError -> {
                        errorEventsChannel.send(Unknown)
                    }
                    is OfflineFirstDataResult.Success -> {
                        result.data?.let { data ->
                            _state.update {
                                it.copy(
                                    findingName = data.finding.name,
                                    findingDescription = data.finding.description.orEmpty(),
                                    findingImportance = data.finding.importance,
                                    findingTerm = data.finding.term,
                                )
                            }
                            cancel()
                        }
                    }
                }
            }
        }

    fun onFindingNameChange(updatedName: String) {
        _state.update { it.copy(findingName = updatedName) }
    }

    fun onFindingDescriptionChange(updatedDescription: String) {
        _state.update { it.copy(findingDescription = updatedDescription) }
    }

    fun onImportanceChange(importance: Importance) {
        _state.update { it.copy(findingImportance = importance) }
    }

    fun onTermChange(term: Term) {
        _state.update { it.copy(findingTerm = term) }
    }

    fun onSaveFinding() =
        viewModelScope.launch {
            if (state.value.findingName.isBlank()) {
                errorEventsChannel.send(CustomUserMessage("Finding name cannot be empty"))
            } else {
                val currentFindingId = findingId
                if (currentFindingId != null) {
                    editFinding(currentFindingId)
                } else {
                    createFinding()
                }
            }
        }

    private suspend fun createFinding() {
        val result =
            saveNewFindingUseCase(
                request =
                    SaveNewFindingRequest(
                        structureId = structureId!!,
                        name = state.value.findingName,
                        description = state.value.findingDescription.ifBlank { null },
                        importance = state.value.findingImportance,
                        term = state.value.findingTerm,
                    ),
            )
        when (result) {
            is OfflineFirstDataResult.ProgrammerError -> {
                errorEventsChannel.send(Unknown)
            }
            is OfflineFirstDataResult.Success -> {
                saveEventChannel.send(result.data)
            }
        }
    }

    private suspend fun editFinding(findingId: Uuid) {
        val result =
            saveFindingDetailsUseCase(
                request =
                    SaveFindingDetailsRequest(
                        findingId = findingId,
                        name = state.value.findingName,
                        description = state.value.findingDescription.ifBlank { null },
                        importance = state.value.findingImportance,
                        term = state.value.findingTerm,
                    ),
            )
        when (result) {
            is OfflineFirstUpdateDataResult.Success -> {
                saveEventChannel.send(findingId)
            }
            is OfflineFirstUpdateDataResult.NotFound,
            is OfflineFirstUpdateDataResult.ProgrammerError,
            -> {
                errorEventsChannel.send(Unknown)
            }
        }
    }
}
