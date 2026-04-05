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

package cz.adamec.timotej.snag.projects.fe.driving.impl.internal.projectDetails.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cz.adamec.timotej.snag.lib.design.fe.error.ShowSnackbarOnError
import cz.adamec.timotej.snag.lib.design.fe.events.ObserveAsEvents
import cz.adamec.timotej.snag.projects.fe.driving.impl.internal.projectDetails.vm.ProjectDetailsViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import kotlin.uuid.Uuid

@Composable
internal fun ProjectDetailsScreen(
    projectId: Uuid,
    onNewStructureClick: () -> Unit,
    onStructureClick: (projectId: Uuid, structureId: Uuid) -> Unit,
    onNewInspectionClick: () -> Unit,
    onInspectionClick: (inspectionId: Uuid) -> Unit,
    onBack: () -> Unit,
    onEditClick: () -> Unit,
    onManageAssignmentsClick: () -> Unit,
    viewModel: ProjectDetailsViewModel = koinViewModel { parametersOf(projectId) },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var isShowingReportTypePicker by remember { mutableStateOf(false) }

    ShowSnackbarOnError(
        uiErrorsFlow = viewModel.errorsFlow,
    )
    ObserveAsEvents(
        eventsFlow = viewModel.deletedSuccessfullyEventFlow,
        onEvent = {
            onBack()
        },
    )
    SaveReportEffect(reportFlow = viewModel.reportReadyFlow)

    if (isShowingReportTypePicker) {
        ReportTypePickerDialog(
            types = state.availableReportTypes,
            onSelectType = { type ->
                isShowingReportTypePicker = false
                viewModel.onDownloadReport(type)
            },
            onDismiss = { isShowingReportTypePicker = false },
        )
    }

    ProjectDetailsContent(
        state = state,
        onNewStructureClick = onNewStructureClick,
        onStructureClick = onStructureClick,
        onNewInspectionClick = onNewInspectionClick,
        onInspectionClick = onInspectionClick,
        onStartInspection = viewModel::onStartInspection,
        onEndInspection = viewModel::onEndInspection,
        onBack = onBack,
        onEditClick = onEditClick,
        onDelete = viewModel::onDelete,
        onDownloadReportClick = {
            val types = state.availableReportTypes
            if (types.size == 1) {
                viewModel.onDownloadReport(types.first())
            } else {
                isShowingReportTypePicker = true
            }
        },
        onToggleClose = viewModel::onToggleClose,
        onManageAssignmentsClick = onManageAssignmentsClick,
        onAssignUser = viewModel::onAssignUser,
        onRemoveUser = viewModel::onRemoveUser,
    )
}
