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

package cz.adamec.timotej.snag.projects.fe.nonwear.driving.internal.projectAssignments.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import cz.adamec.timotej.snag.authorization.business.UserRole
import cz.adamec.timotej.snag.core.foundation.common.UuidProvider
import cz.adamec.timotej.snag.lib.design.fe.button.AdaptiveTonalButton
import cz.adamec.timotej.snag.lib.design.fe.scaffold.BackNavigationIcon
import cz.adamec.timotej.snag.lib.design.fe.scaffold.CollapsableTopAppBarScaffold
import cz.adamec.timotej.snag.lib.design.fe.scenes.StatusBarAwareDragHandle
import cz.adamec.timotej.snag.lib.design.fe.theme.SnagPreview
import cz.adamec.timotej.snag.lib.design.fe.theme.SnagTheme
import cz.adamec.timotej.snag.projects.fe.common.driving.internal.projectAssignments.vm.AssignedUserItem
import cz.adamec.timotej.snag.projects.fe.common.driving.internal.projectAssignments.vm.ProjectAssignmentsUiState
import cz.adamec.timotej.snag.projects.fe.nonwear.driving.internal.projectAssignments.ui.components.AddUserBottomSheetContent
import cz.adamec.timotej.snag.projects.fe.nonwear.driving.internal.projectAssignments.ui.components.AssignedUserListItem
import kotlinx.collections.immutable.persistentListOf
import org.jetbrains.compose.resources.stringResource
import snag.feat.projects.fe.nonwear.driving.generated.resources.Res
import snag.feat.projects.fe.nonwear.driving.generated.resources.add_user_to_project
import snag.feat.projects.fe.nonwear.driving.generated.resources.project_assignments_title
import snag.lib.design.fe.generated.resources.ic_add
import kotlin.uuid.Uuid
import snag.lib.design.fe.generated.resources.Res as DesignRes

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
        topAppBarActions = {
            if (state.canManageAssignments) {
                AdaptiveTonalButton(
                    onClick = { showAddUserSheet = true },
                    icon = DesignRes.drawable.ic_add,
                    label = stringResource(Res.string.add_user_to_project),
                )
                Spacer(
                    modifier = Modifier.size(16.dp),
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
                dragHandle = {
                    StatusBarAwareDragHandle(sheetState = bottomSheetState)
                },
                containerColor = SnagTheme.surfaceColors.sheetColor,
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

@Preview
@PreviewScreenSizes
@Composable
@Suppress("FunctionNameMaxLength")
private fun ProjectAssignmentsContentPreview() {
    SnagPreview {
        ProjectAssignmentsContent(
            state =
                ProjectAssignmentsUiState(
                    assignedUsers =
                        persistentListOf(
                            AssignedUserItem(
                                id = UuidProvider.getUuid(),
                                email = "user1@example.com",
                                role = UserRole.PASSPORT_LEAD,
                            ),
                            AssignedUserItem(
                                id = UuidProvider.getUuid(),
                                email = "user2@example.com",
                                role = UserRole.SERVICE_WORKER,
                            ),
                            AssignedUserItem(
                                id = UuidProvider.getUuid(),
                                email = "user3@example.com",
                                role = null,
                            ),
                        ),
                    canManageAssignments = true,
                    isLoading = false,
                ),
            onAssignUser = {},
            onRemoveUser = {},
            onBack = {},
        )
    }
}
