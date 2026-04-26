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

import androidx.lifecycle.viewModelScope
import cz.adamec.timotej.snag.core.network.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.findings.fe.app.api.AddFindingPhotoRequest
import cz.adamec.timotej.snag.findings.fe.app.api.DeleteFindingPhotoUseCase
import cz.adamec.timotej.snag.findings.fe.app.api.DeleteFindingUseCase
import cz.adamec.timotej.snag.findings.fe.app.api.GetFindingPhotosUseCase
import cz.adamec.timotej.snag.findings.fe.app.api.GetFindingUseCase
import cz.adamec.timotej.snag.findings.fe.app.api.NonWebAddFindingPhotoUseCase
import cz.adamec.timotej.snag.lib.design.fe.api.error.UiError
import cz.adamec.timotej.snag.projects.fe.app.api.CanEditProjectEntitiesUseCase
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.uuid.Uuid

internal class NonWebFindingDetailViewModel(
    findingId: Uuid,
    projectId: Uuid,
    getFindingUseCase: GetFindingUseCase,
    deleteFindingUseCase: DeleteFindingUseCase,
    canEditProjectEntitiesUseCase: CanEditProjectEntitiesUseCase,
    getFindingPhotosUseCase: GetFindingPhotosUseCase,
    deleteFindingPhotoUseCase: DeleteFindingPhotoUseCase,
    private val nonWebAddFindingPhotoUseCase: NonWebAddFindingPhotoUseCase,
) : FindingDetailViewModel(
        findingId = findingId,
        projectId = projectId,
        getFindingUseCase = getFindingUseCase,
        deleteFindingUseCase = deleteFindingUseCase,
        canEditProjectEntitiesUseCase = canEditProjectEntitiesUseCase,
        getFindingPhotosUseCase = getFindingPhotosUseCase,
        deleteFindingPhotoUseCase = deleteFindingPhotoUseCase,
    ) {
    override fun onAddPhoto(
        bytes: ByteArray,
        fileName: String,
    ) {
        viewModelScope.launch {
            vmState.update { it.copy(isAddingPhoto = true) }
            val request =
                AddFindingPhotoRequest(
                    bytes = bytes,
                    fileName = fileName,
                    findingId = findingId,
                    projectId = projectId,
                )
            when (nonWebAddFindingPhotoUseCase(request)) {
                is OfflineFirstDataResult.ProgrammerError -> {
                    errorEventsChannel.send(UiError.Unknown)
                }

                is OfflineFirstDataResult.Success -> {
                    // Photo will appear via flow
                }
            }
            vmState.update { it.copy(isAddingPhoto = false) }
        }
    }
}
