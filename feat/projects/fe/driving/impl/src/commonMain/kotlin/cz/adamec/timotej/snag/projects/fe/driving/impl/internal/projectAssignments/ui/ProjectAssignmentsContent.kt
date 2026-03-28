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

package cz.adamec.timotej.snag.projects.fe.driving.impl.internal.projectAssignments.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cz.adamec.timotej.snag.lib.design.fe.scaffold.BackNavigationIcon
import cz.adamec.timotej.snag.lib.design.fe.scaffold.CollapsableTopAppBarScaffold
import cz.adamec.timotej.snag.projects.fe.driving.impl.internal.projectAssignments.ui.components.AssignedUserListItem
import cz.adamec.timotej.snag.projects.fe.driving.impl.internal.projectAssignments.vm.AssignedUserItem
import cz.adamec.timotej.snag.projects.fe.driving.impl.internal.projectAssignments.vm.ProjectAssignmentsUiState
import cz.adamec.timotej.snag.users.fe.driving.api.toDisplayName
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import snag.feat.projects.fe.driving.impl.generated.resources.Res
import snag.feat.projects.fe.driving.impl.generated.resources.add_user_to_project
import snag.feat.projects.fe.driving.impl.generated.resources.project_assignments_title
import snag.lib.design.fe.generated.resources.ic_add
import snag.lib.design.fe.generated.resources.ic_person
import kotlin.uuid.Uuid
import snag.lib.design.fe.generated.resources.Res as DesignRes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ProjectAssignmentsContent(
    state: ProjectAssignmentsUiState,
    onAssignUser: (Uuid) -> Unit,
    onRemoveUser: (Uuid) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showAddUserSheet by remember { mutableStateOf(false) }
    val bottomSheetState = rememberModalBottomSheetState()

    CollapsableTopAppBarScaffold(
        modifier = modifier,
        title = stringResource(Res.string.project_assignments_title),
        topAppBarNavigationIcon = {
            BackNavigationIcon(onClick = onBack)
        },
        floatingActionButton = {
            if (state.canManageAssignments) {
                ExtendedFloatingActionButton(
                    onClick = { showAddUserSheet = true },
                    icon = {
                        Icon(
                            painter = painterResource(DesignRes.drawable.ic_add),
                            contentDescription = null,
                        )
                    },
                    text = {
                        Text(text = stringResource(Res.string.add_user_to_project))
                    },
                )
            }
        },
    ) { paddingValues ->
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .consumeWindowInsets(paddingValues),
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 88.dp),
            ) {
                items(
                    items = state.assignedUsers,
                    key = { it.id },
                ) { userItem ->
                    AssignedUserListItem(
                        userItem = userItem,
                        canRemove = state.canManageAssignments,
                        onRemoveClick = onRemoveUser,
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

        if (showAddUserSheet) {
            ModalBottomSheet(
                onDismissRequest = { showAddUserSheet = false },
                sheetState = bottomSheetState,
            ) {
                AddUserBottomSheetContent(
                    availableUsers = state.availableUsers,
                    onUserClick = { userId ->
                        onAssignUser(userId)
                        showAddUserSheet = false
                    },
                )
            }
        }
    }
}

@Composable
private fun AddUserBottomSheetContent(
    availableUsers: List<AssignedUserItem>,
    onUserClick: (Uuid) -> Unit,
) {
    LazyColumn(
        contentPadding = PaddingValues(bottom = 32.dp),
    ) {
        items(
            items = availableUsers,
            key = { it.id },
        ) { userItem ->
            ListItem(
                modifier = Modifier.clickable { onUserClick(userItem.id) },
                leadingContent = {
                    Icon(
                        painter = painterResource(DesignRes.drawable.ic_person),
                        contentDescription = null,
                    )
                },
                headlineContent = {
                    Text(
                        text = userItem.email,
                        style = MaterialTheme.typography.titleMedium,
                    )
                },
                supportingContent = {
                    userItem.role?.let { role ->
                        Text(text = role.toDisplayName())
                    }
                },
            )
        }
    }
}
