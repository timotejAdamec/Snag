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
import cz.adamec.timotej.snag.lib.design.fe.error.UiError
import cz.adamec.timotej.snag.lib.design.fe.error.toUiError
import cz.adamec.timotej.snag.projects.fe.app.api.DeleteProjectUseCase
import cz.adamec.timotej.snag.projects.fe.app.api.GetProjectUseCase
import cz.adamec.timotej.snag.projects.fe.app.api.SetProjectClosedUseCase
import cz.adamec.timotej.snag.projects.fe.app.api.model.SetProjectClosedRequest
import cz.adamec.timotej.snag.reports.business.Report
import cz.adamec.timotej.snag.structures.fe.app.api.GetStructuresUseCase
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.InjectedParam
import kotlin.uuid.Uuid

internal class ProjectDetailsViewModel(
    @InjectedParam private val projectId: Uuid,
    private val getProjectUseCase: GetProjectUseCase,
    private val deleteProjectUseCase: DeleteProjectUseCase,
    private val getStructuresUseCase: GetStructuresUseCase,
    private val getInspectionsUseCase: GetInspectionsUseCase,
    private val downloadReportUseCase: DownloadReportUseCase,
    private val saveInspectionUseCase: SaveInspectionUseCase,
    private val setProjectClosedUseCase: SetProjectClosedUseCase,
    private val timestampProvider: TimestampProvider,
) : ViewModel() {
    private val vmState: MutableStateFlow<ProjectDetailsVmState> =
        MutableStateFlow(ProjectDetailsVmState())
    val state: StateFlow<ProjectDetailsUiState> =
        vmState.mapState { it.toUiState() }

    private val errorEventsChannel = Channel<UiError>()
    val errorsFlow = errorEventsChannel.receiveAsFlow()

    private val deletedSuccessfullyEventChannel = Channel<Unit>()
    val deletedSuccessfullyEventFlow = deletedSuccessfullyEventChannel.receiveAsFlow()

    private val reportReadyChannel = Channel<Report>()
    val reportReadyFlow = reportReadyChannel.receiveAsFlow()

    init {
        collectProject(projectId)
        collectStructures(projectId)
        collectInspections(projectId)
    }

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

    fun onDownloadReport() =
        viewModelScope.launch {
            vmState.update { it.copy(isDownloadingReport = true) }
            when (val result = downloadReportUseCase(projectId)) {
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
