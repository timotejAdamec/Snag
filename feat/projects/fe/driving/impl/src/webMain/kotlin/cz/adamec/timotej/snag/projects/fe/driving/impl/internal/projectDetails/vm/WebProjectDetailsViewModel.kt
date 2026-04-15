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

package cz.adamec.timotej.snag.projects.fe.driving.impl.internal.projectDetails.vm

import androidx.lifecycle.viewModelScope
import cz.adamec.timotej.snag.core.foundation.common.TimestampProvider
import cz.adamec.timotej.snag.core.network.fe.PhotoUploadResult
import cz.adamec.timotej.snag.feat.inspections.fe.app.api.GetInspectionsUseCase
import cz.adamec.timotej.snag.feat.inspections.fe.app.api.SaveInspectionUseCase
import cz.adamec.timotej.snag.feat.reports.fe.app.api.DownloadReportUseCase
import cz.adamec.timotej.snag.feat.reports.fe.app.api.GetAvailableReportTypesFlowUseCase
import cz.adamec.timotej.snag.lib.design.fe.error.UiError
import cz.adamec.timotej.snag.projects.fe.app.api.AddProjectPhotoRequest
import cz.adamec.timotej.snag.projects.fe.app.api.AddProjectPhotoUseCase
import cz.adamec.timotej.snag.projects.fe.app.api.AssignUserToProjectUseCase
import cz.adamec.timotej.snag.projects.fe.app.api.CanAssignUserToProjectUseCase
import cz.adamec.timotej.snag.projects.fe.app.api.CanCloseProjectUseCase
import cz.adamec.timotej.snag.projects.fe.app.api.CanEditProjectEntitiesUseCase
import cz.adamec.timotej.snag.projects.fe.app.api.CanModifyProjectFilesUseCase
import cz.adamec.timotej.snag.projects.fe.app.api.DeleteProjectPhotoUseCase
import cz.adamec.timotej.snag.projects.fe.app.api.DeleteProjectUseCase
import cz.adamec.timotej.snag.projects.fe.app.api.GetProjectAssignmentsUseCase
import cz.adamec.timotej.snag.projects.fe.app.api.GetProjectPhotosUseCase
import cz.adamec.timotej.snag.projects.fe.app.api.GetProjectUseCase
import cz.adamec.timotej.snag.projects.fe.app.api.RemoveUserFromProjectUseCase
import cz.adamec.timotej.snag.projects.fe.app.api.SetProjectClosedUseCase
import cz.adamec.timotej.snag.projects.fe.app.api.UpdateProjectPhotoDescriptionUseCase
import cz.adamec.timotej.snag.structures.fe.app.api.GetStructuresUseCase
import cz.adamec.timotej.snag.users.fe.app.api.GetUsersUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.uuid.Uuid

internal class WebProjectDetailsViewModel(
    projectId: Uuid,
    getProjectUseCase: GetProjectUseCase,
    deleteProjectUseCase: DeleteProjectUseCase,
    getStructuresUseCase: GetStructuresUseCase,
    getInspectionsUseCase: GetInspectionsUseCase,
    downloadReportUseCase: DownloadReportUseCase,
    getAvailableReportTypesUseCase: GetAvailableReportTypesFlowUseCase,
    saveInspectionUseCase: SaveInspectionUseCase,
    setProjectClosedUseCase: SetProjectClosedUseCase,
    canEditProjectEntitiesUseCase: CanEditProjectEntitiesUseCase,
    canCloseProjectUseCase: CanCloseProjectUseCase,
    canAssignUserToProjectUseCase: CanAssignUserToProjectUseCase,
    getProjectAssignmentsUseCase: GetProjectAssignmentsUseCase,
    getUsersUseCase: GetUsersUseCase,
    assignUserToProjectUseCase: AssignUserToProjectUseCase,
    removeUserFromProjectUseCase: RemoveUserFromProjectUseCase,
    timestampProvider: TimestampProvider,
    getProjectPhotosUseCase: GetProjectPhotosUseCase,
    deleteProjectPhotoUseCase: DeleteProjectPhotoUseCase,
    updateProjectPhotoDescriptionUseCase: UpdateProjectPhotoDescriptionUseCase,
    private val addProjectPhotoUseCase: AddProjectPhotoUseCase,
    private val canModifyProjectFilesUseCase: CanModifyProjectFilesUseCase,
) : ProjectDetailsViewModel(
        projectId = projectId,
        getProjectUseCase = getProjectUseCase,
        deleteProjectUseCase = deleteProjectUseCase,
        getStructuresUseCase = getStructuresUseCase,
        getInspectionsUseCase = getInspectionsUseCase,
        downloadReportUseCase = downloadReportUseCase,
        getAvailableReportTypesUseCase = getAvailableReportTypesUseCase,
        saveInspectionUseCase = saveInspectionUseCase,
        setProjectClosedUseCase = setProjectClosedUseCase,
        canEditProjectEntitiesUseCase = canEditProjectEntitiesUseCase,
        canCloseProjectUseCase = canCloseProjectUseCase,
        canAssignUserToProjectUseCase = canAssignUserToProjectUseCase,
        getProjectAssignmentsUseCase = getProjectAssignmentsUseCase,
        getUsersUseCase = getUsersUseCase,
        assignUserToProjectUseCase = assignUserToProjectUseCase,
        removeUserFromProjectUseCase = removeUserFromProjectUseCase,
        timestampProvider = timestampProvider,
        getProjectPhotosUseCase = getProjectPhotosUseCase,
        deleteProjectPhotoUseCase = deleteProjectPhotoUseCase,
        updateProjectPhotoDescriptionUseCase = updateProjectPhotoDescriptionUseCase,
    ) {
    private val _uploadProgressFlow = MutableStateFlow<Float?>(null)
    val uploadProgressFlow: StateFlow<Float?> = _uploadProgressFlow.asStateFlow()

    override fun collectJobs(): List<Job> = super.collectJobs() + listOf(collectCanModifyPhotos())

    private fun collectCanModifyPhotos(): Job =
        viewModelScope.launch {
            canModifyProjectFilesUseCase(projectId).collect { canModify ->
                vmState.update { it.copy(canModifyPhotos = canModify) }
            }
        }

    override fun onAddPhoto(
        bytes: ByteArray,
        fileName: String,
        description: String,
    ) {
        viewModelScope.launch {
            vmState.update { it.copy(isAddingPhoto = true) }
            val request =
                AddProjectPhotoRequest(
                    bytes = bytes,
                    fileName = fileName,
                    projectId = projectId,
                    description = description,
                )
            val result =
                addProjectPhotoUseCase(
                    request = request,
                    onProgress = { progress -> _uploadProgressFlow.value = progress },
                )
            when (result) {
                is PhotoUploadResult.Success -> {
                    // Photo will appear via flow
                }

                is PhotoUploadResult.ProgrammerError -> {
                    errorEventsChannel.send(UiError.Unknown)
                }

                is PhotoUploadResult.NetworkUnavailable -> {
                    errorEventsChannel.send(UiError.Unknown)
                }

                is PhotoUploadResult.UserMessageError -> {
                    errorEventsChannel.send(UiError.Unknown)
                }
            }
            _uploadProgressFlow.value = null
            vmState.update { it.copy(isAddingPhoto = false) }
        }
    }
}
