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

package cz.adamec.timotej.snag.structures.fe.driving.impl.internal.structureDetailsEdit.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.adamec.timotej.snag.lib.core.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.lib.design.fe.error.UiError
import cz.adamec.timotej.snag.lib.design.fe.error.UiError.Unknown
import cz.adamec.timotej.snag.structures.fe.app.api.GetStructureUseCase
import cz.adamec.timotej.snag.structures.fe.app.api.SaveStructureUseCase
import cz.adamec.timotej.snag.structures.fe.app.api.model.SaveStructureRequest
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

internal class StructureDetailsEditViewModel(
    @InjectedParam private val structureId: Uuid?,
    @InjectedParam private val projectId: Uuid?,
    private val getStructureUseCase: GetStructureUseCase,
    private val saveStructureUseCase: SaveStructureUseCase,
) : ViewModel() {
    private val _state: MutableStateFlow<StructureDetailsEditUiState> =
        MutableStateFlow(StructureDetailsEditUiState(projectId = projectId))
    val state: StateFlow<StructureDetailsEditUiState> = _state

    private val errorEventsChannel = Channel<UiError>()
    val errorsFlow = errorEventsChannel.receiveAsFlow()

    private val saveEventChannel = Channel<Uuid>()
    val saveEventFlow = saveEventChannel.receiveAsFlow()

    init {
        require(structureId != null || projectId != null) {
            "Either structureId or projectId must be provided"
        }
        structureId?.let { collectStructure(it) }
    }

    private fun collectStructure(structureId: Uuid) =
        viewModelScope.launch {
            getStructureUseCase(structureId).collect { result ->
                when (result) {
                    is OfflineFirstDataResult.ProgrammerError -> {
                        errorEventsChannel.send(Unknown)
                    }
                    is OfflineFirstDataResult.Success -> {
                        result.data?.let { data ->
                            _state.update {
                                it.copy(
                                    structureName = data.structure.name,
                                    projectId = data.structure.projectId,
                                )
                            }
                            cancel()
                        }
                    }
                }
            }
        }

    fun onStructureNameChange(updatedName: String) {
        _state.update { it.copy(structureName = updatedName, structureNameError = null) }
    }

    fun onSaveStructure() =
        viewModelScope.launch {
            if (state.value.structureName.isBlank()) {
                _state.update { it.copy(structureNameError = Res.string.error_field_required) }
            } else {
                saveStructure()
            }
        }

    private suspend fun saveStructure() {
        val result =
            saveStructureUseCase(
                request =
                    SaveStructureRequest(
                        id = structureId,
                        projectId = state.value.projectId ?: projectId!!,
                        name = state.value.structureName,
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
