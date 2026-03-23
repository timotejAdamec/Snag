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
import cz.adamec.timotej.snag.core.foundation.common.mapState
import cz.adamec.timotej.snag.core.network.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.findings.fe.app.api.AddFindingPhotoRequest
import cz.adamec.timotej.snag.findings.fe.app.api.AddFindingPhotoUseCase
import cz.adamec.timotej.snag.findings.fe.app.api.DeleteFindingPhotoUseCase
import cz.adamec.timotej.snag.findings.fe.app.api.DeleteFindingUseCase
import cz.adamec.timotej.snag.findings.fe.app.api.GetFindingPhotosUseCase
import cz.adamec.timotej.snag.findings.fe.app.api.GetFindingUseCase
import cz.adamec.timotej.snag.lib.design.fe.error.UiError
import cz.adamec.timotej.snag.lib.design.fe.error.UiError.Unknown
import cz.adamec.timotej.snag.projects.fe.app.api.CanEditProjectEntitiesUseCase
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
    @InjectedParam private val projectId: Uuid,
    private val getFindingUseCase: GetFindingUseCase,
    private val deleteFindingUseCase: DeleteFindingUseCase,
    private val canEditProjectEntitiesUseCase: CanEditProjectEntitiesUseCase,
    private val getFindingPhotosUseCase: GetFindingPhotosUseCase,
    private val addFindingPhotoUseCase: AddFindingPhotoUseCase,
    private val deleteFindingPhotoUseCase: DeleteFindingPhotoUseCase,
) : ViewModel() {
    private val vmState: MutableStateFlow<FindingDetailVmState> =
        MutableStateFlow(FindingDetailVmState())
    val state: StateFlow<FindingDetailUiState> =
        vmState.mapState { it.toUiState() }

    private val errorEventsChannel = Channel<UiError>()
    val errorsFlow = errorEventsChannel.receiveAsFlow()

    private val deletedSuccessfullyEventChannel = Channel<Unit>()
    val deletedSuccessfullyEventFlow = deletedSuccessfullyEventChannel.receiveAsFlow()

    init {
        collectFinding()
        collectCanEditFinding()
        collectPhotos()
    }

    private fun collectCanEditFinding() =
        viewModelScope.launch {
            canEditProjectEntitiesUseCase(projectId).collect { canEdit ->
                vmState.update { it.copy(canEditFinding = canEdit) }
            }
        }

    fun onDelete() =
        viewModelScope.launch {
            vmState.update {
                it.copy(isBeingDeleted = true)
            }
            when (deleteFindingUseCase(findingId)) {
                is OfflineFirstDataResult.ProgrammerError -> {
                    vmState.update {
                        it.copy(isBeingDeleted = false)
                    }
                    errorEventsChannel.send(Unknown)
                }

                is OfflineFirstDataResult.Success -> {
                    vmState.update {
                        it.copy(
                            status = FindingDetailUiStatus.DELETED,
                            isBeingDeleted = false,
                        )
                    }
                    deletedSuccessfullyEventChannel.send(Unit)
                }
            }
        }

    private fun collectPhotos() {
        viewModelScope.launch {
            getFindingPhotosUseCase(findingId).collect { result ->
                when (result) {
                    is OfflineFirstDataResult.Success -> {
                        vmState.update { it.copy(photos = result.data) }
                    }

                    is OfflineFirstDataResult.ProgrammerError -> {
                        // Don't fail the whole screen for photo errors
                    }
                }
            }
        }
    }

    fun onAddPhoto(
        bytes: ByteArray,
        fileName: String,
    ) = viewModelScope.launch {
        vmState.update { it.copy(isAddingPhoto = true) }
        val request =
            AddFindingPhotoRequest(
                bytes = bytes,
                fileName = fileName,
                findingId = findingId,
                projectId = projectId,
            )
        when (addFindingPhotoUseCase(request)) {
            is OfflineFirstDataResult.ProgrammerError -> {
                errorEventsChannel.send(Unknown)
            }

            is OfflineFirstDataResult.Success -> {
                // Photo will appear via flow
            }
        }
        vmState.update { it.copy(isAddingPhoto = false) }
    }

    fun onDeletePhoto(photoId: Uuid) =
        viewModelScope.launch {
            when (deleteFindingPhotoUseCase(photoId)) {
                is OfflineFirstDataResult.ProgrammerError -> {
                    errorEventsChannel.send(Unknown)
                }

                is OfflineFirstDataResult.Success -> {
                    // Photo will disappear via flow
                }
            }
        }

    private fun collectFinding() {
        viewModelScope.launch {
            getFindingUseCase(findingId).collect { result ->
                when (result) {
                    is OfflineFirstDataResult.ProgrammerError -> {
                        vmState.update {
                            it.copy(status = FindingDetailUiStatus.ERROR)
                        }
                    }

                    is OfflineFirstDataResult.Success -> {
                        val finding = result.data
                        if (finding == null) {
                            if (vmState.value.status != FindingDetailUiStatus.DELETED) {
                                vmState.update {
                                    it.copy(status = FindingDetailUiStatus.NOT_FOUND)
                                }
                            }
                        } else {
                            vmState.update {
                                it.copy(
                                    status = FindingDetailUiStatus.LOADED,
                                    finding = finding,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
