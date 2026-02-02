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

package cz.adamec.timotej.snag.findings.fe.driving.impl.internal.findingDetail.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cz.adamec.timotej.snag.findings.fe.driving.impl.internal.findingDetail.vm.FindingDetailViewModel
import cz.adamec.timotej.snag.lib.design.fe.error.ShowSnackbarOnError
import cz.adamec.timotej.snag.lib.design.fe.events.ObserveAsEvents
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import kotlin.uuid.Uuid

@Composable
internal fun FindingDetailScreen(
    findingId: Uuid,
    onBack: () -> Unit,
    onEditClick: () -> Unit,
    viewModel: FindingDetailViewModel = koinViewModel { parametersOf(findingId) },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ShowSnackbarOnError(uiErrorsFlow = viewModel.errorsFlow)
    ObserveAsEvents(
        eventsFlow = viewModel.deletedSuccessfullyEventFlow,
        onEvent = {
            onBack()
        },
    )

    FindingDetailContent(
        state = state,
        onBack = onBack,
        onEditClick = onEditClick,
        onDelete = viewModel::onDelete,
    )
}
