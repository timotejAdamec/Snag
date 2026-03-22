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

package cz.adamec.timotej.snag.users.fe.driving.impl.internal.userManagement.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cz.adamec.timotej.snag.lib.design.fe.error.ShowSnackbarOnError
import cz.adamec.timotej.snag.users.fe.driving.impl.internal.userManagement.vm.UserManagementViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun UsersScreen(
    modifier: Modifier = Modifier,
    userManagementViewModel: UserManagementViewModel = koinViewModel(),
) {
    val state by userManagementViewModel.state.collectAsStateWithLifecycle()

    ShowSnackbarOnError(userManagementViewModel.errorsFlow)

    UserManagementContent(
        modifier = modifier,
        state = state,
        onRoleChange = userManagementViewModel::onRoleChanged,
    )
}
