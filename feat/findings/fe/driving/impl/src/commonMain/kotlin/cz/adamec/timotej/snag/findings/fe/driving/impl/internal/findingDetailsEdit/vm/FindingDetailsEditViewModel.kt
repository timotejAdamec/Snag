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
import cz.adamec.timotej.snag.feat.findings.business.FindingType
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
import cz.adamec.timotej.snag.lib.design.fe.error.UiError.Unknown
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.InjectedParam
import snag.lib.design.fe.generated.resources.Res
import snag.lib.design.fe.generated.resources.error_field_required
import kotlin.uuid.Uuid

internal class FindingDetailsEditViewModel(
    @InjectedParam private val findingId: Uuid?,
    @InjectedParam private val structureId: Uuid?,
    @InjectedParam private val findingTypeKey: String?,
    private val getFindingUseCase: GetFindingUseCase,
    private val saveNewFindingUseCase: SaveNewFindingUseCase,
    private val saveFindingDetailsUseCase: SaveFindingDetailsUseCase,
) : ViewModel() {
    private val _state: MutableStateFlow<FindingDetailsEditUiState> =
        MutableStateFlow(
            FindingDetailsEditUiState(
                findingType = findingTypeKey.toFindingType(),
            ),
        )
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
                                    findingType = data.finding.type,
                                )
                            }
                            cancel()
                        }
                    }
                }
            }
        }

    fun onFindingNameChange(updatedName: String) {
        _state.update { it.copy(findingName = updatedName, findingNameError = null) }
    }

    fun onFindingDescriptionChange(updatedDescription: String) {
        _state.update { it.copy(findingDescription = updatedDescription) }
    }

    fun onImportanceChange(importance: Importance) {
        _state.update { state ->
            val currentType = state.findingType
            if (currentType is FindingType.Classic) {
                state.copy(findingType = currentType.copy(importance = importance))
            } else {
                state
            }
        }
    }

    fun onTermChange(term: Term) {
        _state.update { state ->
            val currentType = state.findingType
            if (currentType is FindingType.Classic) {
                state.copy(findingType = currentType.copy(term = term))
            } else {
                state
            }
        }
    }

    fun onSaveFinding() =
        viewModelScope.launch {
            if (state.value.findingName.isBlank()) {
                _state.update { it.copy(findingNameError = Res.string.error_field_required) }
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
                        findingType = state.value.findingType,
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
                        findingType = state.value.findingType,
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

    private fun String?.toFindingType(): FindingType =
        when (this) {
            FindingType.KEY_UNVISITED -> FindingType.Unvisited
            FindingType.KEY_NOTE -> FindingType.Note
            else -> FindingType.Classic()
        }
}
