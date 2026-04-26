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

package cz.adamec.timotej.snag.findings.fe.driving.impl.internal.findingDetailsEdit.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.adamec.timotej.snag.core.foundation.common.mapState
import cz.adamec.timotej.snag.core.network.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.core.network.fe.OfflineFirstUpdateDataResult
import cz.adamec.timotej.snag.feat.findings.business.FindingType
import cz.adamec.timotej.snag.feat.findings.business.FindingTypeKey
import cz.adamec.timotej.snag.feat.findings.business.Importance
import cz.adamec.timotej.snag.feat.findings.business.RelativeCoordinate
import cz.adamec.timotej.snag.feat.findings.business.Term
import cz.adamec.timotej.snag.feat.findings.business.toDefaultFindingType
import cz.adamec.timotej.snag.findings.fe.app.api.GetFindingUseCase
import cz.adamec.timotej.snag.findings.fe.app.api.SaveFindingDetailsUseCase
import cz.adamec.timotej.snag.findings.fe.app.api.SaveNewFindingUseCase
import cz.adamec.timotej.snag.findings.fe.app.api.model.SaveFindingDetailsRequest
import cz.adamec.timotej.snag.findings.fe.app.api.model.SaveNewFindingRequest
import cz.adamec.timotej.snag.lib.design.fe.api.error.UiError
import cz.adamec.timotej.snag.lib.design.fe.api.error.UiError.Unknown
import cz.adamec.timotej.snag.lib.design.fe.api.state.launchWhileSubscribed
import cz.adamec.timotej.snag.projects.fe.app.api.CanEditProjectEntitiesUseCase
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.InjectedParam
import snag.lib.design.fe.api.generated.resources.Res
import snag.lib.design.fe.api.generated.resources.error_field_required
import kotlin.uuid.Uuid

internal class FindingDetailsEditViewModel(
    @InjectedParam private val projectId: Uuid,
    @InjectedParam private val findingId: Uuid?,
    @InjectedParam private val structureId: Uuid?,
    @InjectedParam private val findingTypeKey: FindingTypeKey?,
    @InjectedParam private val coordinate: RelativeCoordinate?,
    private val getFindingUseCase: GetFindingUseCase,
    private val saveNewFindingUseCase: SaveNewFindingUseCase,
    private val saveFindingDetailsUseCase: SaveFindingDetailsUseCase,
    private val canEditProjectEntitiesUseCase: CanEditProjectEntitiesUseCase,
) : ViewModel() {
    private val vmState: MutableStateFlow<FindingDetailsEditVmState> =
        MutableStateFlow(
            FindingDetailsEditVmState(
                findingType = findingTypeKey?.toDefaultFindingType() ?: FindingType.Classic(),
            ),
        ).launchWhileSubscribed(scope = viewModelScope) {
            listOf(collectCanEditFinding())
        }
    val state: StateFlow<FindingDetailsEditUiState> =
        vmState.mapState { it.toUiState() }

    private val errorEventsChannel = Channel<UiError>()
    val errorsFlow = errorEventsChannel.receiveAsFlow()

    private val saveEventChannel = Channel<Uuid>()
    val saveEventFlow = saveEventChannel.receiveAsFlow()

    init {
        require(findingId != null || structureId != null) {
            "Either findingId or structureId must be provided"
        }
        findingId?.let { collectFinding(it) }
    }

    private fun collectCanEditFinding() =
        viewModelScope.launch {
            canEditProjectEntitiesUseCase(projectId).collect { canEdit ->
                vmState.update { it.copy(canEditFinding = canEdit) }
            }
        }

    private fun collectFinding(findingId: Uuid) =
        viewModelScope.launch {
            getFindingUseCase(findingId).collect { result ->
                when (result) {
                    is OfflineFirstDataResult.ProgrammerError -> {
                        errorEventsChannel.send(Unknown)
                    }
                    is OfflineFirstDataResult.Success -> {
                        result.data?.let { data ->
                            vmState.update {
                                it.copy(
                                    findingName = data.name,
                                    findingDescription = data.description.orEmpty(),
                                    findingType = data.type,
                                )
                            }
                            cancel()
                        }
                    }
                }
            }
        }

    fun onFindingNameChange(updatedName: String) {
        vmState.update { it.copy(findingName = updatedName, findingNameError = null) }
    }

    fun onFindingDescriptionChange(updatedDescription: String) {
        vmState.update { it.copy(findingDescription = updatedDescription) }
    }

    fun onImportanceChange(importance: Importance) {
        vmState.update { state ->
            val currentType = state.findingType
            if (currentType is FindingType.Classic) {
                state.copy(findingType = currentType.copy(importance = importance))
            } else {
                state
            }
        }
    }

    fun onTermChange(term: Term) {
        vmState.update { state ->
            val currentType = state.findingType
            if (currentType is FindingType.Classic) {
                state.copy(findingType = currentType.copy(term = term))
            } else {
                state
            }
        }
    }

    fun onSaveFinding() =
        viewModelScope.launch {
            if (vmState.value.findingName.isBlank()) {
                vmState.update { it.copy(findingNameError = Res.string.error_field_required) }
            } else {
                val currentFindingId = findingId
                if (currentFindingId != null) {
                    editFinding(currentFindingId)
                } else {
                    createFinding()
                }
            }
        }

    private suspend fun createFinding() {
        val result =
            saveNewFindingUseCase(
                request =
                    SaveNewFindingRequest(
                        structureId = structureId!!,
                        name = vmState.value.findingName,
                        description = vmState.value.findingDescription.ifBlank { null },
                        findingType = vmState.value.findingType,
                        coordinates = setOfNotNull(coordinate),
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

    private suspend fun editFinding(findingId: Uuid) {
        val result =
            saveFindingDetailsUseCase(
                request =
                    SaveFindingDetailsRequest(
                        findingId = findingId,
                        name = vmState.value.findingName,
                        description = vmState.value.findingDescription.ifBlank { null },
                        findingType = vmState.value.findingType,
                    ),
            )
        when (result) {
            is OfflineFirstUpdateDataResult.Success -> {
                saveEventChannel.send(findingId)
            }
            is OfflineFirstUpdateDataResult.NotFound,
            is OfflineFirstUpdateDataResult.ProgrammerError,
            -> {
                errorEventsChannel.send(Unknown)
            }
        }
    }
}
