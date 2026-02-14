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

package cz.adamec.timotej.snag.structures.fe.driving.impl.internal.floorPlan.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cz.adamec.timotej.snag.feat.findings.business.RelativeCoordinate
import cz.adamec.timotej.snag.lib.design.fe.error.ShowSnackbarOnError
import cz.adamec.timotej.snag.lib.design.fe.events.ObserveAsEvents
import cz.adamec.timotej.snag.structures.fe.driving.impl.internal.floorPlan.vm.StructureFloorPlanViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import kotlin.uuid.Uuid

@Composable
internal fun StructureFloorPlanScreen(
    structureId: Uuid,
    selectedFindingId: Uuid?,
    onBack: () -> Unit,
    onEditClick: () -> Unit,
    onFindingClick: (Uuid) -> Unit,
    onCreateFinding: (coordinate: RelativeCoordinate, findingTypeKey: String) -> Unit,
    viewModel: StructureFloorPlanViewModel =
        koinViewModel {
            parametersOf(structureId)
        },
) {
    LaunchedEffect(selectedFindingId) {
        viewModel.onFindingSelected(selectedFindingId)
    }

    val state by viewModel.state.collectAsStateWithLifecycle()

    ShowSnackbarOnError(
        uiErrorsFlow = viewModel.errorsFlow,
    )
    ObserveAsEvents(
        eventsFlow = viewModel.deletedSuccessfullyEventFlow,
        onEvent = {
            onBack()
        },
    )

    StructureFloorPlanContent(
        state = state,
        onBack = onBack,
        onEditClick = onEditClick,
        onDelete = viewModel::onDelete,
        onFindingClick = onFindingClick,
        onCreateFinding = onCreateFinding,
    )
}
