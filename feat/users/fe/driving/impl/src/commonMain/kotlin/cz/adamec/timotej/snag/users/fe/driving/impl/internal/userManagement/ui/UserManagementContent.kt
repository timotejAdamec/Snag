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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LoadingIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cz.adamec.timotej.snag.lib.design.fe.scaffold.CollapsableTopAppBarScaffold
import cz.adamec.timotej.snag.users.business.UserRole
import cz.adamec.timotej.snag.users.fe.driving.impl.internal.userManagement.ui.components.UserListItem
import cz.adamec.timotej.snag.users.fe.driving.impl.internal.userManagement.vm.UserManagementUiState
import org.jetbrains.compose.resources.stringResource
import snag.feat.users.fe.driving.impl.generated.resources.Res
import snag.feat.users.fe.driving.impl.generated.resources.users_title
import kotlin.uuid.Uuid

@Composable
internal fun UserManagementContent(
    state: UserManagementUiState,
    onRoleChange: (Uuid, UserRole?) -> Unit,
    modifier: Modifier = Modifier,
) {
    CollapsableTopAppBarScaffold(
        title = stringResource(Res.string.users_title),
    ) { paddingValues ->
        LazyColumn(
            modifier =
                modifier
                    .padding(paddingValues)
                    .consumeWindowInsets(paddingValues),
            contentPadding =
                PaddingValues(
                    start = 8.dp,
                    end = 8.dp,
                    top = 8.dp,
                    bottom = 48.dp,
                ),
        ) {
            items(
                items = state.users,
                key = { it.id },
            ) { userItem ->
                UserListItem(
                    userItem = userItem,
                    onRoleSelect = { newRole ->
                        onRoleChange(userItem.id, newRole)
                    },
                )
                HorizontalDivider()
            }
            item {
                AnimatedVisibility(
                    visible = state.isLoading,
                    exit = fadeOut(),
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        LoadingIndicator()
                    }
                }
            }
        }
    }
}
