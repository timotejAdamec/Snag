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
import cz.adamec.timotej.snag.feat.inspections.fe.app.api.GetInspectionsUseCase
import cz.adamec.timotej.snag.feat.reports.fe.app.api.DownloadReportUseCase
import cz.adamec.timotej.snag.feat.reports.fe.model.FrontendReport
import cz.adamec.timotej.snag.lib.core.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.lib.core.fe.OnlineDataResult
import cz.adamec.timotej.snag.lib.design.fe.error.UiError
import cz.adamec.timotej.snag.projects.fe.app.api.DeleteProjectUseCase
import cz.adamec.timotej.snag.projects.fe.app.api.GetProjectUseCase
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
) : ViewModel() {
    private val _state: MutableStateFlow<ProjectDetailsUiState> =
        MutableStateFlow(ProjectDetailsUiState())
    val state: StateFlow<ProjectDetailsUiState> = _state

    private val errorEventsChannel = Channel<UiError>()
    val errorsFlow = errorEventsChannel.receiveAsFlow()

    private val deletedSuccessfullyEventChannel = Channel<Unit>()
    val deletedSuccessfullyEventFlow = deletedSuccessfullyEventChannel.receiveAsFlow()

    private val reportReadyChannel = Channel<Pair<FrontendReport, String>>()
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
                        _state.update {
                            it.copy(
                                projectStatus = ProjectDetailsUiStatus.ERROR,
                            )
                        }
                        errorEventsChannel.send(UiError.Unknown)
                    }
                    is OfflineFirstDataResult.Success -> {
                        result.data?.let { project ->
                            _state.update {
                                it.copy(
                                    projectStatus = ProjectDetailsUiStatus.LOADED,
                                    project = project,
                                )
                            }
                        } ?: if (state.value.projectStatus != ProjectDetailsUiStatus.DELETED) {
                            _state.update {
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
                        _state.update {
                            it.copy(
                                structureStatus = StructuresUiStatus.ERROR,
                            )
                        }
                        errorEventsChannel.send(UiError.Unknown)
                    }
                    is OfflineFirstDataResult.Success -> {
                        _state.update {
                            it.copy(
                                structureStatus = StructuresUiStatus.LOADED,
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
                        _state.update {
                            it.copy(
                                inspectionStatus = InspectionsUiStatus.ERROR,
                            )
                        }
                        errorEventsChannel.send(UiError.Unknown)
                    }
                    is OfflineFirstDataResult.Success -> {
                        _state.update {
                            it.copy(
                                inspectionStatus = InspectionsUiStatus.LOADED,
                                inspections = result.data.toImmutableList(),
                            )
                        }
                    }
                }
            }
        }

    fun onDelete() =
        viewModelScope.launch {
            _state.update {
                it.copy(isBeingDeleted = true)
            }
            when (deleteProjectUseCase(projectId)) {
                is OfflineFirstDataResult.ProgrammerError -> {
                    _state.update {
                        it.copy(
                            isBeingDeleted = false,
                        )
                    }
                    errorEventsChannel.send(UiError.Unknown)
                }

                is OfflineFirstDataResult.Success -> {
                    _state.update {
                        it.copy(
                            projectStatus = ProjectDetailsUiStatus.DELETED,
                            isBeingDeleted = false,
                        )
                    }
                    deletedSuccessfullyEventChannel.send(Unit)
                }
            }
        }

    fun onDownloadReport() =
        viewModelScope.launch {
            _state.update { it.copy(isDownloadingReport = true) }
            when (val result = downloadReportUseCase(projectId)) {
                is OnlineDataResult.Success -> {
                    val projectName =
                        _state.value.project
                            ?.project
                            ?.name
                            .orEmpty()
                    val sanitizedName =
                        projectName
                            .replace(Regex("[^a-zA-Z0-9._-]"), "_")
                            .take(50)
                            .ifEmpty { "report" }
                    reportReadyChannel.send(result.data to sanitizedName)
                }
                is OnlineDataResult.Failure.NetworkUnavailable -> {
                    errorEventsChannel.send(UiError.NetworkUnavailable)
                }
                is OnlineDataResult.Failure.UserMessageError -> {
                    errorEventsChannel.send(UiError.CustomUserMessage(result.message))
                }
                is OnlineDataResult.Failure.ProgrammerError -> {
                    errorEventsChannel.send(UiError.Unknown)
                }
            }
            _state.update { it.copy(isDownloadingReport = false) }
        }
}
