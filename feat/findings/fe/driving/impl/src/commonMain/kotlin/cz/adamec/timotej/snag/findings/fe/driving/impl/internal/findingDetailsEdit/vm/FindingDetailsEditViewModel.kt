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
import cz.adamec.timotej.snag.findings.fe.app.api.GetFindingUseCase
import cz.adamec.timotej.snag.findings.fe.app.api.SaveFindingUseCase
import cz.adamec.timotej.snag.findings.fe.app.api.model.SaveFindingRequest
import cz.adamec.timotej.snag.lib.core.fe.OfflineFirstDataResult
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
    private val saveFindingUseCase: SaveFindingUseCase,
) : ViewModel() {
    private val _state: MutableStateFlow<FindingDetailsEditUiState> =
        MutableStateFlow(FindingDetailsEditUiState())
    val state: StateFlow<FindingDetailsEditUiState> = _state

    private val errorEventsChannel = Channel<UiError>()
    val errorsFlow = errorEventsChannel.receiveAsFlow()

    private val saveEventChannel = Channel<Uuid>()
    val saveEventFlow = saveEventChannel.receiveAsFlow()

    private var resolvedStructureId: Uuid? = structureId

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
                    is OfflineFirstDataResult.Success ->
                        result.data?.let { data ->
                            resolvedStructureId = data.structureId
                            _state.update {
                                it.copy(
                                    findingName = data.name,
                                    findingDescription = data.description.orEmpty(),
                                )
                            }
                            cancel()
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

    fun onSaveFinding() =
        viewModelScope.launch {
            if (state.value.findingName.isBlank()) {
                errorEventsChannel.send(CustomUserMessage("Finding name cannot be empty"))
                return@launch
            }

            val result =
                saveFindingUseCase(
                    request =
                        SaveFindingRequest(
                            id = findingId,
                            structureId = resolvedStructureId ?: structureId!!,
                            name = state.value.findingName,
                            description = state.value.findingDescription.ifBlank { null },
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
}
