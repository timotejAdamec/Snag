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
import cz.adamec.timotej.snag.core.foundation.common.UuidProvider
import cz.adamec.timotej.snag.core.foundation.common.mapState
import cz.adamec.timotej.snag.core.network.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.core.network.fe.OnlineDataResult
import cz.adamec.timotej.snag.lib.design.fe.error.UiError
import cz.adamec.timotej.snag.lib.design.fe.error.UiError.Unknown
import cz.adamec.timotej.snag.projects.fe.app.api.CanEditProjectEntitiesUseCase
import cz.adamec.timotej.snag.structures.fe.app.api.CanModifyFloorPlanImageUseCase
import cz.adamec.timotej.snag.structures.fe.app.api.DeleteFloorPlanImageUseCase
import cz.adamec.timotej.snag.structures.fe.app.api.GetStructureUseCase
import cz.adamec.timotej.snag.structures.fe.app.api.SaveStructureUseCase
import cz.adamec.timotej.snag.structures.fe.app.api.UploadFloorPlanImageUseCase
import cz.adamec.timotej.snag.structures.fe.app.api.model.SaveStructureRequest
import cz.adamec.timotej.snag.structures.fe.app.api.model.UploadFloorPlanImageRequest
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
    @InjectedParam private val projectId: Uuid,
    @InjectedParam private val structureId: Uuid?,
    private val getStructureUseCase: GetStructureUseCase,
    private val saveStructureUseCase: SaveStructureUseCase,
    private val uploadFloorPlanImageUseCase: UploadFloorPlanImageUseCase,
    private val deleteFloorPlanImageUseCase: DeleteFloorPlanImageUseCase,
    private val canModifyFloorPlanImageUseCase: CanModifyFloorPlanImageUseCase,
    private val canEditProjectEntitiesUseCase: CanEditProjectEntitiesUseCase,
) : ViewModel() {
    private val vmState: MutableStateFlow<StructureDetailsEditVmState> =
        MutableStateFlow(
            StructureDetailsEditVmState(
                isCreatingNew = structureId == null,
                projectId = projectId,
            ),
        )
    val state: StateFlow<StructureDetailsEditUiState> =
        vmState.mapState { it.toUiState() }

    private val errorEventsChannel = Channel<UiError>()
    val errorsFlow = errorEventsChannel.receiveAsFlow()

    private val saveEventChannel = Channel<Uuid>()
    val saveEventFlow = saveEventChannel.receiveAsFlow()

    private val resolvedStructureId: Uuid = structureId ?: UuidProvider.getUuid()

    init {
        structureId?.let { collectStructure(it) }
        collectCanModifyFloorPlanImage()
        collectCanEditStructure()
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
                            vmState.update {
                                it.copy(
                                    structureName = data.name,
                                    floorPlanUrl = data.floorPlanUrl,
                                    projectId = data.projectId,
                                )
                            }
                            cancel()
                        }
                    }
                }
            }
        }

    private fun collectCanModifyFloorPlanImage() =
        viewModelScope.launch {
            canModifyFloorPlanImageUseCase().collect { canModify ->
                vmState.update {
                    it.copy(
                        canModifyFloorPlanImage = canModify,
                    )
                }
            }
        }

    private fun collectCanEditStructure() =
        viewModelScope.launch {
            canEditProjectEntitiesUseCase(projectId).collect { canEdit ->
                vmState.update { it.copy(canEditStructure = canEdit) }
            }
        }

    fun onStructureNameChange(updatedName: String) {
        vmState.update { it.copy(structureName = updatedName, structureNameError = null) }
    }

    fun onImagePicked(
        bytes: ByteArray,
        fileName: String,
    ) = viewModelScope.launch {
        vmState.update { it.copy(isUploadingImage = true) }
        when (
            val result =
                uploadFloorPlanImageUseCase(
                    UploadFloorPlanImageRequest(
                        projectId = projectId,
                        structureId = resolvedStructureId,
                        bytes = bytes,
                        fileName = fileName,
                    ),
                )
        ) {
            is OnlineDataResult.Success -> {
                val previousPendingUrl = vmState.value.pendingUploadUrl
                vmState.update {
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
                vmState.update { it.copy(isUploadingImage = false) }
                errorEventsChannel.send(Unknown)
            }
        }
    }

    fun onRemoveImage() {
        val pendingUrl = vmState.value.pendingUploadUrl
        vmState.update {
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
            if (vmState.value.structureName.isBlank()) {
                vmState.update { it.copy(structureNameError = Res.string.error_field_required) }
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
                        projectId = projectId,
                        name = vmState.value.structureName,
                        floorPlanUrl = vmState.value.floorPlanUrl,
                    ),
            )
        when (result) {
            is OfflineFirstDataResult.ProgrammerError -> {
                errorEventsChannel.send(Unknown)
            }
            is OfflineFirstDataResult.Success -> {
                vmState.update { it.copy(pendingUploadUrl = null) }
                saveEventChannel.send(result.data)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        val pendingUrl = vmState.value.pendingUploadUrl
        if (pendingUrl != null) {
            viewModelScope.launch {
                withContext(NonCancellable) {
                    deleteFloorPlanImageUseCase(pendingUrl)
                }
            }
        }
    }
}
