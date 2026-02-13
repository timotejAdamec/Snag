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
import cz.adamec.timotej.snag.lib.core.common.UuidProvider
import cz.adamec.timotej.snag.lib.core.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.lib.core.fe.OnlineDataResult
import cz.adamec.timotej.snag.lib.design.fe.error.UiError
import cz.adamec.timotej.snag.lib.design.fe.error.UiError.Unknown
import cz.adamec.timotej.snag.structures.fe.app.api.DeleteFloorPlanImageUseCase
import cz.adamec.timotej.snag.structures.fe.app.api.GetStructureUseCase
import cz.adamec.timotej.snag.structures.fe.app.api.SaveStructureUseCase
import cz.adamec.timotej.snag.structures.fe.app.api.UploadFloorPlanImageUseCase
import cz.adamec.timotej.snag.structures.fe.app.api.model.SaveStructureRequest
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.annotation.InjectedParam
import snag.lib.design.fe.generated.resources.Res
import snag.lib.design.fe.generated.resources.error_field_required
import kotlin.uuid.Uuid

internal class StructureDetailsEditViewModel(
    @InjectedParam private val structureId: Uuid?,
    @InjectedParam private val projectId: Uuid?,
    private val getStructureUseCase: GetStructureUseCase,
    private val saveStructureUseCase: SaveStructureUseCase,
    private val uploadFloorPlanImageUseCase: UploadFloorPlanImageUseCase,
    private val deleteFloorPlanImageUseCase: DeleteFloorPlanImageUseCase,
) : ViewModel() {
    private val _state: MutableStateFlow<StructureDetailsEditUiState> =
        MutableStateFlow(StructureDetailsEditUiState(projectId = projectId))
    val state: StateFlow<StructureDetailsEditUiState> = _state

    private val errorEventsChannel = Channel<UiError>()
    val errorsFlow = errorEventsChannel.receiveAsFlow()

    private val saveEventChannel = Channel<Uuid>()
    val saveEventFlow = saveEventChannel.receiveAsFlow()

    private var originalFloorPlanUrl: String? = null
    private val resolvedStructureId: Uuid = structureId ?: UuidProvider.getUuid()

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
                            originalFloorPlanUrl = data.structure.floorPlanUrl
                            _state.update {
                                it.copy(
                                    structureName = data.structure.name,
                                    projectId = data.structure.projectId,
                                    floorPlanUrl = data.structure.floorPlanUrl,
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

    fun onImagePicked(
        bytes: ByteArray,
        fileName: String,
    ) = viewModelScope.launch {
        _state.update { it.copy(isUploadingImage = true) }
        val resolvedProjectId = _state.value.projectId ?: projectId!!
        when (val result = uploadFloorPlanImageUseCase(resolvedProjectId, resolvedStructureId, bytes, fileName)) {
            is OnlineDataResult.Success -> {
                val previousPendingUrl = _state.value.pendingUploadUrl
                _state.update {
                    it.copy(
                        floorPlanUrl = result.data,
                        pendingUploadUrl = result.data,
                        isUploadingImage = false,
                    )
                }
                previousPendingUrl?.let { url ->
                    launch { deleteFloorPlanImageUseCase(url) }
                }
            }
            is OnlineDataResult.Failure -> {
                _state.update { it.copy(isUploadingImage = false) }
                errorEventsChannel.send(Unknown)
            }
        }
    }

    fun onRemoveImage() {
        val pendingUrl = _state.value.pendingUploadUrl
        _state.update {
            it.copy(
                floorPlanUrl = null,
                pendingUploadUrl = null,
            )
        }
        pendingUrl?.let { url ->
            viewModelScope.launch { deleteFloorPlanImageUseCase(url) }
        }
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
                        id = resolvedStructureId,
                        projectId = state.value.projectId ?: projectId!!,
                        name = state.value.structureName,
                        floorPlanUrl = state.value.floorPlanUrl,
                    ),
            )
        when (result) {
            is OfflineFirstDataResult.ProgrammerError -> {
                errorEventsChannel.send(Unknown)
            }
            is OfflineFirstDataResult.Success -> {
                _state.update { it.copy(pendingUploadUrl = null) }
                saveEventChannel.send(result.data)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        val pendingUrl = _state.value.pendingUploadUrl
        if (pendingUrl != null) {
            viewModelScope.launch {
                withContext(NonCancellable) {
                    deleteFloorPlanImageUseCase(pendingUrl)
                }
            }
        }
    }
}
