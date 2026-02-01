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
import cz.adamec.timotej.snag.lib.design.fe.error.ShowSnackbarOnError
import cz.adamec.timotej.snag.lib.design.fe.events.ObserveAsEvents
import cz.adamec.timotej.snag.structures.fe.driving.impl.internal.floorPlan.vm.StructureFloorPlanViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import kotlin.uuid.Uuid

@Composable
internal fun StructureFloorPlanScreen(
    structureId: Uuid,
    getSelectedFindingId: () -> Uuid?,
    onBack: () -> Unit,
    viewModel: StructureFloorPlanViewModel = koinViewModel {
        parametersOf(structureId)
    },
) {
    val selectedFindingId = getSelectedFindingId()
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
        onDelete = viewModel::onDelete,
    )
}
