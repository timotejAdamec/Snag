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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
        Box(
            modifier =
                modifier
                    .padding(paddingValues)
                    .consumeWindowInsets(paddingValues),
        ) {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 240.dp),
                contentPadding =
                    PaddingValues(
                        start = 8.dp,
                        end = 8.dp,
                        top = 8.dp,
                        bottom = 48.dp,
                    ),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
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
                }
            }
            AnimatedVisibility(
                visible = state.isLoading,
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.Center),
            ) {
                LoadingIndicator()
            }
        }
    }
}
