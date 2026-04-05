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
import cz.adamec.timotej.snag.reports.business.ReportType
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
    var reportTypePickerTypes by remember { mutableStateOf<List<ReportType>?>(null) }

    ShowSnackbarOnError(
        uiErrorsFlow = viewModel.errorsFlow,
    )
    ObserveAsEvents(
        eventsFlow = viewModel.deletedSuccessfullyEventFlow,
        onEvent = {
            onBack()
        },
    )
    ObserveAsEvents(
        eventsFlow = viewModel.showReportTypePickerFlow,
        onEvent = { types ->
            reportTypePickerTypes = types
        },
    )
    SaveReportEffect(reportFlow = viewModel.reportReadyFlow)

    reportTypePickerTypes?.let { types ->
        ReportTypePickerDialog(
            types = types,
            onSelectType = { type ->
                reportTypePickerTypes = null
                viewModel.onReportTypeSelected(type)
            },
            onDismiss = { reportTypePickerTypes = null },
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
        onDownloadReportClick = viewModel::onDownloadReport,
        onToggleClose = viewModel::onToggleClose,
        onManageAssignmentsClick = onManageAssignmentsClick,
        onAssignUser = viewModel::onAssignUser,
        onRemoveUser = viewModel::onRemoveUser,
    )
}
