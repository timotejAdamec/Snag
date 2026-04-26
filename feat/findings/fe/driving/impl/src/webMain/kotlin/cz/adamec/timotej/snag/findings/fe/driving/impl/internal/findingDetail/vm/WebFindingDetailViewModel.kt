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
import cz.adamec.timotej.snag.core.network.fe.OnlineDataResult
import cz.adamec.timotej.snag.findings.fe.app.api.AddFindingPhotoRequest
import cz.adamec.timotej.snag.findings.fe.app.api.CanModifyFindingPhotosUseCase
import cz.adamec.timotej.snag.findings.fe.app.api.DeleteFindingPhotoUseCase
import cz.adamec.timotej.snag.findings.fe.app.api.DeleteFindingUseCase
import cz.adamec.timotej.snag.findings.fe.app.api.GetFindingPhotosUseCase
import cz.adamec.timotej.snag.findings.fe.app.api.GetFindingUseCase
import cz.adamec.timotej.snag.findings.fe.app.api.WebAddFindingPhotoUseCase
import cz.adamec.timotej.snag.lib.design.fe.api.error.UiError
import cz.adamec.timotej.snag.projects.fe.app.api.CanEditProjectEntitiesUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.uuid.Uuid

internal class WebFindingDetailViewModel(
    findingId: Uuid,
    projectId: Uuid,
    getFindingUseCase: GetFindingUseCase,
    deleteFindingUseCase: DeleteFindingUseCase,
    canEditProjectEntitiesUseCase: CanEditProjectEntitiesUseCase,
    getFindingPhotosUseCase: GetFindingPhotosUseCase,
    deleteFindingPhotoUseCase: DeleteFindingPhotoUseCase,
    private val webAddFindingPhotoUseCase: WebAddFindingPhotoUseCase,
    private val canModifyFindingPhotosUseCase: CanModifyFindingPhotosUseCase,
) : FindingDetailViewModel(
        findingId = findingId,
        projectId = projectId,
        getFindingUseCase = getFindingUseCase,
        deleteFindingUseCase = deleteFindingUseCase,
        canEditProjectEntitiesUseCase = canEditProjectEntitiesUseCase,
        getFindingPhotosUseCase = getFindingPhotosUseCase,
        deleteFindingPhotoUseCase = deleteFindingPhotoUseCase,
    ) {
    override fun collectJobs(): List<Job> = super.collectJobs() + listOf(collectCanModifyPhotos())

    private fun collectCanModifyPhotos(): Job =
        viewModelScope.launch {
            canModifyFindingPhotosUseCase(projectId).collect { canModify ->
                vmState.update { it.copy(canModifyPhotos = canModify) }
            }
        }

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
            when (webAddFindingPhotoUseCase(request)) {
                is OnlineDataResult.Success -> {
                    // Photo will appear via flow
                }

                is OnlineDataResult.Failure -> {
                    errorEventsChannel.send(UiError.Unknown)
                }
            }
            vmState.update { it.copy(isAddingPhoto = false) }
        }
    }
}
