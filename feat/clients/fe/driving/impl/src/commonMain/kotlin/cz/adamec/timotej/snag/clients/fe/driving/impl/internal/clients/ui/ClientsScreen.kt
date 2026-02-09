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

package cz.adamec.timotej.snag.clients.fe.driving.impl.internal.clients.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cz.adamec.timotej.snag.clients.fe.driving.impl.internal.clients.vm.ClientsViewModel
import cz.adamec.timotej.snag.lib.design.fe.error.ShowSnackbarOnError
import org.koin.compose.viewmodel.koinViewModel
import kotlin.uuid.Uuid

@Composable
internal fun ClientsScreen(
    onNewClientClick: () -> Unit,
    onClientClick: (clientId: Uuid) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ClientsViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ShowSnackbarOnError(viewModel.errorsFlow)

    ClientsContent(
        modifier = modifier,
        onNewClientClick = onNewClientClick,
        onClientClick = onClientClick,
        state = state,
    )
}
