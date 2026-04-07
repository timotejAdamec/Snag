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

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.adamec.timotej.snag.core.foundation.common.TimestampProvider
import cz.adamec.timotej.snag.core.foundation.common.mapState
import cz.adamec.timotej.snag.core.network.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.core.network.fe.OnlineDataResult
import cz.adamec.timotej.snag.feat.inspections.fe.app.api.GetInspectionsUseCase
import cz.adamec.timotej.snag.feat.inspections.fe.app.api.SaveInspectionUseCase
import cz.adamec.timotej.snag.feat.inspections.fe.app.api.model.SaveInspectionRequest
import cz.adamec.timotej.snag.feat.reports.fe.app.api.DownloadReportUseCase
import cz.adamec.timotej.snag.feat.reports.fe.app.api.GetAvailableReportTypesFlowUseCase
import cz.adamec.timotej.snag.lib.design.fe.error.UiError
import cz.adamec.timotej.snag.lib.design.fe.error.UiError.Unknown
import cz.adamec.timotej.snag.lib.design.fe.error.toUiError
import cz.adamec.timotej.snag.lib.design.fe.state.launchWhileSubscribed
import cz.adamec.timotej.snag.projects.fe.app.api.AssignUserToProjectUseCase
import cz.adamec.timotej.snag.projects.fe.app.api.CanAssignUserToProjectUseCase
import cz.adamec.timotej.snag.projects.fe.app.api.CanCloseProjectUseCase
import cz.adamec.timotej.snag.projects.fe.app.api.CanEditProjectEntitiesUseCase
import cz.adamec.timotej.snag.projects.fe.app.api.DeleteProjectPhotoUseCase
import cz.adamec.timotej.snag.projects.fe.app.api.DeleteProjectUseCase
import cz.adamec.timotej.snag.projects.fe.app.api.GetProjectAssignmentsUseCase
import cz.adamec.timotej.snag.projects.fe.app.api.GetProjectPhotosUseCase
import cz.adamec.timotej.snag.projects.fe.app.api.GetProjectUseCase
import cz.adamec.timotej.snag.projects.fe.app.api.RemoveUserFromProjectUseCase
import cz.adamec.timotej.snag.projects.fe.app.api.SetProjectClosedUseCase
import cz.adamec.timotej.snag.projects.fe.app.api.UpdateProjectPhotoDescriptionUseCase
import cz.adamec.timotej.snag.projects.fe.app.api.model.SetProjectClosedRequest
import cz.adamec.timotej.snag.reports.business.Report
import cz.adamec.timotej.snag.reports.business.ReportType
import cz.adamec.timotej.snag.structures.fe.app.api.GetStructuresUseCase
import cz.adamec.timotej.snag.users.fe.app.api.GetUsersUseCase
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.uuid.Uuid

@Suppress("TooManyFunctions")
internal abstract class ProjectDetailsViewModel(
    protected val projectId: Uuid,
    private val getProjectUseCase: GetProjectUseCase,
    private val deleteProjectUseCase: DeleteProjectUseCase,
    private val getStructuresUseCase: GetStructuresUseCase,
    private val getInspectionsUseCase: GetInspectionsUseCase,
    private val downloadReportUseCase: DownloadReportUseCase,
    private val getAvailableReportTypesUseCase: GetAvailableReportTypesFlowUseCase,
    private val saveInspectionUseCase: SaveInspectionUseCase,
    private val setProjectClosedUseCase: SetProjectClosedUseCase,
    private val canEditProjectEntitiesUseCase: CanEditProjectEntitiesUseCase,
    private val canCloseProjectUseCase: CanCloseProjectUseCase,
    private val canAssignUserToProjectUseCase: CanAssignUserToProjectUseCase,
    private val getProjectAssignmentsUseCase: GetProjectAssignmentsUseCase,
    private val getUsersUseCase: GetUsersUseCase,
    private val assignUserToProjectUseCase: AssignUserToProjectUseCase,
    private val removeUserFromProjectUseCase: RemoveUserFromProjectUseCase,
    private val timestampProvider: TimestampProvider,
    private val getProjectPhotosUseCase: GetProjectPhotosUseCase,
    private val deleteProjectPhotoUseCase: DeleteProjectPhotoUseCase,
    private val updateProjectPhotoDescriptionUseCase: UpdateProjectPhotoDescriptionUseCase,
) : ViewModel() {
    protected val vmState: MutableStateFlow<ProjectDetailsVmState> =
        MutableStateFlow(ProjectDetailsVmState())
            .launchWhileSubscribed(scope = viewModelScope) {
                collectJobs()
            }
    val state: StateFlow<ProjectDetailsUiState> =
        vmState.mapState { it.toUiState() }

    protected val errorEventsChannel = Channel<UiError>()
    val errorsFlow = errorEventsChannel.receiveAsFlow()

    private val deletedSuccessfullyEventChannel = Channel<Unit>()
    val deletedSuccessfullyEventFlow = deletedSuccessfullyEventChannel.receiveAsFlow()

    private val reportReadyChannel = Channel<Report>()
    val reportReadyFlow = reportReadyChannel.receiveAsFlow()

    protected open fun collectJobs(): List<Job> =
        listOf(
            collectProject(projectId),
            collectStructures(projectId),
            collectInspections(projectId),
            collectCanEditEntities(projectId),
            collectCanCloseProject(projectId),
            collectCanAssignUsers(projectId),
            collectAssignments(projectId),
            collectUsers(),
            collectAvailableReportTypes(),
            collectPhotos(),
        )

    private fun collectProject(projectId: Uuid) =
        viewModelScope.launch {
            getProjectUseCase(projectId).collect { result ->
                when (result) {
                    is OfflineFirstDataResult.ProgrammerError -> {
                        vmState.update {
                            it.copy(
                                projectStatus = ProjectDetailsUiStatus.ERROR,
                            )
                        }
                        errorEventsChannel.send(UiError.Unknown)
                    }
                    is OfflineFirstDataResult.Success -> {
                        result.data?.let { project ->
                            vmState.update {
                                it.copy(
                                    projectStatus = ProjectDetailsUiStatus.LOADED,
                                    project = project,
                                )
                            }
                        } ?: if (vmState.value.projectStatus != ProjectDetailsUiStatus.DELETED) {
                            vmState.update {
                                it.copy(
                                    projectStatus = ProjectDetailsUiStatus.NOT_FOUND,
                                )
                            }
                        } else {
                            // keep as deleted
                        }
                    }
                }
            }
        }

    private fun collectStructures(projectId: Uuid) =
        viewModelScope.launch {
            getStructuresUseCase(projectId).collect { result ->
                when (result) {
                    is OfflineFirstDataResult.ProgrammerError -> {
                        vmState.update {
                            it.copy(
                                structureStatus = StructuresUiStatus.ERROR,
                            )
                        }
                        errorEventsChannel.send(UiError.Unknown)
                    }
                    is OfflineFirstDataResult.Success -> {
                        vmState.update {
                            it.copy(
                                structures = result.data.toImmutableList(),
                            )
                        }
                    }
                }
            }
        }

    private fun collectInspections(projectId: Uuid) =
        viewModelScope.launch {
            getInspectionsUseCase(projectId).collect { result ->
                when (result) {
                    is OfflineFirstDataResult.ProgrammerError -> {
                        vmState.update {
                            it.copy(
                                inspectionStatus = InspectionsUiStatus.ERROR,
                            )
                        }
                        errorEventsChannel.send(UiError.Unknown)
                    }
                    is OfflineFirstDataResult.Success -> {
                        vmState.update {
                            it.copy(
                                inspections = result.data.toImmutableList(),
                            )
                        }
                    }
                }
            }
        }

    private fun collectCanEditEntities(projectId: Uuid) =
        viewModelScope.launch {
            canEditProjectEntitiesUseCase(projectId).collect { canEdit ->
                vmState.update { it.copy(canEditEntities = canEdit) }
            }
        }

    private fun collectCanCloseProject(projectId: Uuid) =
        viewModelScope.launch {
            canCloseProjectUseCase(projectId).collect { canClose ->
                vmState.update { it.copy(canCloseProject = canClose) }
            }
        }

    private fun collectCanAssignUsers(projectId: Uuid) =
        viewModelScope.launch {
            canAssignUserToProjectUseCase(projectId).collect { canAssign ->
                vmState.update { it.copy(canAssignUsers = canAssign) }
            }
        }

    private fun collectAssignments(projectId: Uuid) =
        viewModelScope.launch {
            getProjectAssignmentsUseCase(projectId).collect { result ->
                when (result) {
                    is OfflineFirstDataResult.ProgrammerError -> {
                        errorEventsChannel.send(UiError.Unknown)
                    }
                    is OfflineFirstDataResult.Success -> {
                        vmState.update {
                            it.copy(
                                assignedUserIds = result.data,
                                assignmentsLoaded = true,
                            )
                        }
                    }
                }
            }
        }

    private fun collectUsers() =
        viewModelScope.launch {
            getUsersUseCase().collect { result ->
                when (result) {
                    is OfflineFirstDataResult.ProgrammerError -> {
                        errorEventsChannel.send(UiError.Unknown)
                    }
                    is OfflineFirstDataResult.Success -> {
                        vmState.update {
                            it.copy(
                                allUsers = result.data.toImmutableList(),
                                usersLoaded = true,
                            )
                        }
                    }
                }
            }
        }

    private fun collectAvailableReportTypes() =
        viewModelScope.launch {
            getAvailableReportTypesUseCase().collect { types ->
                vmState.update { it.copy(availableReportTypes = types) }
            }
        }

    private fun collectPhotos(): Job =
        viewModelScope.launch {
            getProjectPhotosUseCase(projectId).collect { result ->
                when (result) {
                    is OfflineFirstDataResult.Success -> {
                        vmState.update { it.copy(photos = result.data.toImmutableList()) }
                    }

                    is OfflineFirstDataResult.ProgrammerError -> {
                        errorEventsChannel.send(Unknown)
                    }
                }
            }
        }

    abstract fun onAddPhoto(
        bytes: ByteArray,
        fileName: String,
        description: String,
    )

    fun onDeletePhoto(photoId: Uuid) =
        viewModelScope.launch {
            when (deleteProjectPhotoUseCase(photoId)) {
                is OfflineFirstDataResult.ProgrammerError -> {
                    errorEventsChannel.send(Unknown)
                }

                is OfflineFirstDataResult.Success -> {
                    // Photo will disappear via flow
                }
            }
        }

    fun onUpdatePhotoDescription(
        photoId: Uuid,
        newDescription: String,
    ) = viewModelScope.launch {
        when (updateProjectPhotoDescriptionUseCase(photoId = photoId, newDescription = newDescription)) {
            is OfflineFirstDataResult.ProgrammerError -> {
                errorEventsChannel.send(Unknown)
            }

            is OfflineFirstDataResult.Success -> {
                // Description update will appear via flow
            }
        }
    }

    fun onAssignUser(userId: Uuid) =
        viewModelScope.launch {
            assignUserToProjectUseCase(
                projectId = projectId,
                userId = userId,
            )
        }

    fun onRemoveUser(userId: Uuid) =
        viewModelScope.launch {
            removeUserFromProjectUseCase(
                projectId = projectId,
                userId = userId,
            )
        }

    fun onDelete() =
        viewModelScope.launch {
            vmState.update {
                it.copy(isBeingDeleted = true)
            }
            when (deleteProjectUseCase(projectId)) {
                is OfflineFirstDataResult.ProgrammerError -> {
                    vmState.update {
                        it.copy(
                            isBeingDeleted = false,
                        )
                    }
                    errorEventsChannel.send(UiError.Unknown)
                }

                is OfflineFirstDataResult.Success -> {
                    vmState.update {
                        it.copy(
                            projectStatus = ProjectDetailsUiStatus.DELETED,
                            isBeingDeleted = false,
                        )
                    }
                    deletedSuccessfullyEventChannel.send(Unit)
                }
            }
        }

    fun onStartInspection(inspectionId: Uuid) =
        viewModelScope.launch {
            vmState.value.inspections
                .find { it.id == inspectionId }
                ?.let { insp ->
                    saveInspectionUseCase(
                        SaveInspectionRequest(
                            id = insp.id,
                            projectId = insp.projectId,
                            startedAt = timestampProvider.getNowTimestamp(),
                            endedAt = insp.endedAt,
                            participants = insp.participants,
                            climate = insp.climate,
                            note = insp.note,
                        ),
                    )
                }
        }

    fun onEndInspection(inspectionId: Uuid) =
        viewModelScope.launch {
            vmState.value.inspections
                .find { it.id == inspectionId }
                ?.let { insp ->
                    saveInspectionUseCase(
                        SaveInspectionRequest(
                            id = insp.id,
                            projectId = insp.projectId,
                            startedAt = insp.startedAt,
                            endedAt = timestampProvider.getNowTimestamp(),
                            participants = insp.participants,
                            climate = insp.climate,
                            note = insp.note,
                        ),
                    )
                }
        }

    fun onToggleClose() =
        viewModelScope.launch {
            vmState.update { it.copy(isClosingOrReopening = true) }
            val result =
                setProjectClosedUseCase(
                    SetProjectClosedRequest(
                        projectId = projectId,
                        isClosed = !(vmState.value.project?.isClosed == true),
                    ),
                )
            if (result is OnlineDataResult.Failure) {
                errorEventsChannel.send(result.toUiError())
            }
            vmState.update { it.copy(isClosingOrReopening = false) }
        }

    fun onDownloadReport(type: ReportType) =
        viewModelScope.launch {
            vmState.update { it.copy(isDownloadingReport = true) }
            when (val result = downloadReportUseCase(projectId, type)) {
                is OnlineDataResult.Success -> {
                    reportReadyChannel.send(result.data)
                }
                is OnlineDataResult.Failure -> {
                    errorEventsChannel.send(result.toUiError())
                }
            }
            vmState.update { it.copy(isDownloadingReport = false) }
        }
}
