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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import cz.adamec.timotej.snag.authorization.business.UserRole
import cz.adamec.timotej.snag.core.foundation.common.Timestamp
import cz.adamec.timotej.snag.core.foundation.common.UuidProvider
import cz.adamec.timotej.snag.feat.inspections.fe.driving.api.InspectionCard
import cz.adamec.timotej.snag.feat.structures.fe.driving.api.StructureCard
import cz.adamec.timotej.snag.lib.design.fe.button.AdaptiveTonalButton
import cz.adamec.timotej.snag.lib.design.fe.scaffold.BackNavigationIcon
import cz.adamec.timotej.snag.lib.design.fe.scaffold.CollapsableTopAppBarScaffold
import cz.adamec.timotej.snag.lib.design.fe.scenes.StatusBarAwareDragHandle
import cz.adamec.timotej.snag.lib.design.fe.theme.SnagPreview
import cz.adamec.timotej.snag.lib.design.fe.theme.SnagTheme
import cz.adamec.timotej.snag.projects.app.model.AppProjectData
import cz.adamec.timotej.snag.projects.fe.driving.impl.internal.projectAssignments.ui.components.AddUserBottomSheetContent
import cz.adamec.timotej.snag.projects.fe.driving.impl.internal.projectAssignments.vm.AssignedUserItem
import cz.adamec.timotej.snag.projects.fe.driving.impl.internal.projectDetails.ui.components.CreatorInfo
import cz.adamec.timotej.snag.projects.fe.driving.impl.internal.projectDetails.vm.ProjectDetailsUiState
import cz.adamec.timotej.snag.projects.fe.driving.impl.internal.projectDetails.vm.ProjectDetailsUiStatus
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import snag.feat.projects.fe.driving.impl.generated.resources.Res
import snag.feat.projects.fe.driving.impl.generated.resources.add_user_to_project
import snag.feat.projects.fe.driving.impl.generated.resources.close_project
import snag.feat.projects.fe.driving.impl.generated.resources.delete_project_confirmation_text
import snag.feat.projects.fe.driving.impl.generated.resources.delete_project_confirmation_title
import snag.feat.projects.fe.driving.impl.generated.resources.download_report
import snag.feat.projects.fe.driving.impl.generated.resources.inspections_section_title
import snag.feat.projects.fe.driving.impl.generated.resources.new_inspection
import snag.feat.projects.fe.driving.impl.generated.resources.new_structure
import snag.feat.projects.fe.driving.impl.generated.resources.project_assignments_title
import snag.feat.projects.fe.driving.impl.generated.resources.project_not_found
import snag.feat.projects.fe.driving.impl.generated.resources.reopen_project
import snag.feat.projects.fe.driving.impl.generated.resources.structures_section_title
import snag.lib.design.fe.generated.resources.delete
import snag.lib.design.fe.generated.resources.edit
import snag.lib.design.fe.generated.resources.ic_add
import snag.lib.design.fe.generated.resources.ic_close
import snag.lib.design.fe.generated.resources.ic_delete
import snag.lib.design.fe.generated.resources.ic_edit
import snag.lib.design.fe.generated.resources.ic_event_note
import snag.lib.design.fe.generated.resources.ic_file_export
import snag.lib.design.fe.generated.resources.ic_group
import snag.lib.design.fe.generated.resources.ic_lock
import snag.lib.design.fe.generated.resources.ic_lock_open
import snag.lib.design.fe.generated.resources.ic_person
import snag.lib.design.fe.generated.resources.ic_space_dashboard
import kotlin.uuid.Uuid
import snag.lib.design.fe.generated.resources.Res as DesignRes

@Composable
internal fun ProjectDetailsContent(
    state: ProjectDetailsUiState,
    onNewStructureClick: () -> Unit,
    onStructureClick: (projectId: Uuid, structureId: Uuid) -> Unit,
    onNewInspectionClick: () -> Unit,
    onInspectionClick: (inspectionId: Uuid) -> Unit,
    onStartInspection: (Uuid) -> Unit,
    onEndInspection: (Uuid) -> Unit,
    onBack: () -> Unit,
    onEditClick: () -> Unit,
    onDelete: () -> Unit,
    onDownloadReportClick: () -> Unit,
    onToggleClose: () -> Unit,
    onManageAssignmentsClick: () -> Unit,
    onAssignUser: (Uuid) -> Unit,
    onRemoveUser: (Uuid) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier,
    ) {
        when (state.projectStatus) {
            ProjectDetailsUiStatus.NOT_FOUND -> {
                Text(
                    modifier = Modifier.align(Alignment.Center),
                    text = stringResource(Res.string.project_not_found),
                )
            }

            ProjectDetailsUiStatus.LOADING,
            ProjectDetailsUiStatus.LOADED,
            ProjectDetailsUiStatus.DELETED,
            -> {
                LoadedProjectDetailsContent(
                    state = state,
                    onNewStructureClick = onNewStructureClick,
                    onStructureClick = onStructureClick,
                    onNewInspectionClick = onNewInspectionClick,
                    onInspectionClick = onInspectionClick,
                    onStartInspection = onStartInspection,
                    onEndInspection = onEndInspection,
                    onBack = onBack,
                    onEditClick = onEditClick,
                    onDelete = onDelete,
                    onDownloadReportClick = onDownloadReportClick,
                    onToggleClose = onToggleClose,
                    onManageAssignmentsClick = onManageAssignmentsClick,
                    onAssignUser = onAssignUser,
                    onRemoveUser = onRemoveUser,
                )
            }

            ProjectDetailsUiStatus.ERROR -> {
                val currentOnBack = rememberUpdatedState(onBack)
                LaunchedEffect(Unit) { currentOnBack.value() }
            }
        }
    }
}

@Suppress("LongMethod", "CognitiveComplexMethod")
@Composable
private fun LoadedProjectDetailsContent(
    state: ProjectDetailsUiState,
    onNewStructureClick: () -> Unit,
    onStructureClick: (projectId: Uuid, structureId: Uuid) -> Unit,
    onNewInspectionClick: () -> Unit,
    onInspectionClick: (inspectionId: Uuid) -> Unit,
    onStartInspection: (Uuid) -> Unit,
    onEndInspection: (Uuid) -> Unit,
    onBack: () -> Unit,
    onEditClick: () -> Unit,
    onDelete: () -> Unit,
    onDownloadReportClick: () -> Unit,
    onToggleClose: () -> Unit,
    onManageAssignmentsClick: () -> Unit,
    onAssignUser: (Uuid) -> Unit,
    onRemoveUser: (Uuid) -> Unit,
    modifier: Modifier = Modifier,
) {
    val projectName = state.project?.name.orEmpty()
    val projectAddress = state.project?.address.orEmpty()
    CollapsableTopAppBarScaffold(
        modifier = modifier,
        title = projectName,
        subtitle = projectAddress,
        topAppBarNavigationIcon = {
            BackNavigationIcon(
                onClick = onBack,
            )
        },
    ) { paddingValues ->
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(
                        top = paddingValues.calculateTopPadding(),
                        start = paddingValues.calculateStartPadding(LayoutDirection.Ltr),
                        end = paddingValues.calculateEndPadding(LayoutDirection.Ltr),
                    ).consumeWindowInsets(paddingValues),
        ) {
            var showAddUserSheet by remember { mutableStateOf(false) }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding =
                    PaddingValues(
                        top = 16.dp,
                        bottom = 116.dp,
                    ),
            ) {
                state.creatorEmail?.let { email ->
                    item {
                        CreatorInfo(
                            email = email,
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                                    .padding(bottom = 16.dp),
                        )
                    }
                }
                item {
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .padding(bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Icon(
                                painter = painterResource(DesignRes.drawable.ic_group),
                                contentDescription = stringResource(Res.string.project_assignments_title),
                            )
                            Text(
                                text = stringResource(Res.string.project_assignments_title),
                                style = MaterialTheme.typography.titleMedium,
                            )
                        }
                        if (state.canAssignUsers) {
                            AdaptiveTonalButton(
                                onClick = { showAddUserSheet = true },
                                icon = DesignRes.drawable.ic_add,
                                label = stringResource(Res.string.add_user_to_project),
                            )
                        }
                    }
                }
                item {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                    ) {
                        items(
                            items = state.assignedUsers,
                            key = { it.id },
                        ) { user ->
                            InputChip(
                                selected = false,
                                onClick = onManageAssignmentsClick,
                                label = { Text(text = user.email) },
                                leadingIcon = {
                                    Icon(
                                        modifier = Modifier.size(InputChipDefaults.AvatarSize),
                                        painter = painterResource(DesignRes.drawable.ic_person),
                                        contentDescription = null,
                                    )
                                },
                                trailingIcon =
                                    if (state.canAssignUsers) {
                                        {
                                            IconButton(
                                                onClick = { onRemoveUser(user.id) },
                                            ) {
                                                Icon(
                                                    modifier = Modifier.size(InputChipDefaults.AvatarSize),
                                                    painter = painterResource(DesignRes.drawable.ic_close),
                                                    contentDescription = null,
                                                )
                                            }
                                        }
                                    } else {
                                        null
                                    },
                                colors = InputChipDefaults.inputChipColors(
                                    containerColor = Color.Transparent,
                                )
                            )
                        }
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
                item {
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .padding(bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Icon(
                                painter = painterResource(DesignRes.drawable.ic_event_note),
                                contentDescription = stringResource(Res.string.inspections_section_title),
                            )
                            Text(
                                text = stringResource(Res.string.inspections_section_title),
                                style = MaterialTheme.typography.titleMedium,
                            )
                        }
                        if (state.isProjectEditable) {
                            AdaptiveTonalButton(
                                onClick = onNewInspectionClick,
                                icon = DesignRes.drawable.ic_add,
                                label = stringResource(Res.string.new_inspection),
                            )
                        }
                    }
                }
                item {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                    ) {
                        items(
                            items = state.inspections,
                            key = { it.id },
                        ) { inspection ->
                            InspectionCard(
                                modifier = Modifier.width(200.dp),
                                feInspection = inspection,
                                onClick = {
                                    onInspectionClick(inspection.id)
                                },
                                onStartClick = {
                                    onStartInspection(inspection.id)
                                },
                                onEndClick = {
                                    onEndInspection(inspection.id)
                                },
                                actionsEnabled = state.isProjectEditable,
                            )
                        }
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .padding(bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Icon(
                                painter = painterResource(DesignRes.drawable.ic_space_dashboard),
                                contentDescription = stringResource(Res.string.structures_section_title),
                            )
                            Text(
                                text = stringResource(Res.string.structures_section_title),
                                style = MaterialTheme.typography.titleMedium,
                            )
                        }
                        if (state.isProjectEditable) {
                            AdaptiveTonalButton(
                                onClick = onNewStructureClick,
                                icon = DesignRes.drawable.ic_add,
                                label = stringResource(Res.string.new_structure),
                            )
                        }
                    }
                }
                @OptIn(ExperimentalLayoutApi::class)
                item {
                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.TopStart,
                    ) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            maxItemsInEachRow = 3,
                        ) {
                            state.structures.forEach { structure ->
                                val minWidth = 250.dp
                                StructureCard(
                                    modifier =
                                        Modifier
                                            .widthIn(min = minWidth)
                                            .height(minWidth + 30.dp)
                                            .weight(1f),
                                    feStructure = structure,
                                    onClick = {
                                        onStructureClick(
                                            structure.projectId,
                                            structure.id,
                                        )
                                    },
                                )
                            }
                        }
                    }
                }
            }

            if (showAddUserSheet) {
                val bottomSheetState = rememberModalBottomSheetState()
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

            var isShowingDeleteConfirmation by remember { mutableStateOf(false) }
            if (isShowingDeleteConfirmation) {
                ProjectDeletionAlertDialog(
                    areButtonsEnabled = state.canInvokeDeletion,
                    onDelete = onDelete,
                    onDismiss = {
                        isShowingDeleteConfirmation = false
                    },
                )
            }
            HorizontalFloatingToolbar(
                modifier =
                    Modifier
                        .align(Alignment.BottomCenter)
                        .padding(
                            bottom = paddingValues.calculateBottomPadding() + 16.dp,
                        ),
                expanded = true,
                colors = FloatingToolbarDefaults.standardFloatingToolbarColors(
                    toolbarContainerColor = SnagTheme.surfaceColors.containerColor,
                )
            ) {
                IconButton(
                    enabled = state.canToggleClosed,
                    onClick = onToggleClose,
                ) {
                    if (state.isClosingOrReopening) {
                        LoadingIndicator(
                            modifier = Modifier.size(24.dp),
                        )
                    } else {
                        Icon(
                            painter =
                                painterResource(
                                    if (state.isClosed) {
                                        DesignRes.drawable.ic_lock_open
                                    } else {
                                        DesignRes.drawable.ic_lock
                                    },
                                ),
                            contentDescription =
                                stringResource(
                                    if (state.isClosed) {
                                        Res.string.reopen_project
                                    } else {
                                        Res.string.close_project
                                    },
                                ),
                        )
                    }
                }
                IconButton(
                    enabled = state.isProjectEditable,
                    onClick = onEditClick,
                ) {
                    Icon(
                        painter = painterResource(DesignRes.drawable.ic_edit),
                        contentDescription = stringResource(DesignRes.string.edit),
                    )
                }
                IconButton(
                    enabled = state.canDownloadReport,
                    onClick = onDownloadReportClick,
                ) {
                    if (state.isDownloadingReport) {
                        LoadingIndicator(
                            modifier = Modifier.size(24.dp),
                        )
                    } else {
                        Icon(
                            painter = painterResource(DesignRes.drawable.ic_file_export),
                            contentDescription = stringResource(Res.string.download_report),
                        )
                    }
                }
                IconButton(
                    enabled = state.canInvokeDeletion,
                    onClick = {
                        isShowingDeleteConfirmation = true
                    },
                ) {
                    Icon(
                        painter = painterResource(DesignRes.drawable.ic_delete),
                        contentDescription = stringResource(DesignRes.string.delete),
                    )
                }
            }
        }
    }
}

@Composable
private fun ProjectDeletionAlertDialog(
    areButtonsEnabled: Boolean,
    onDelete: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        title = {
            Text(text = stringResource(Res.string.delete_project_confirmation_title))
        },
        text = {
            Text(text = stringResource(Res.string.delete_project_confirmation_text))
        },
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                enabled = areButtonsEnabled,
                onClick = {
                    onDelete()
                },
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(
                enabled = areButtonsEnabled,
                onClick = onDismiss,
            ) {
                Text("Dismiss")
            }
        },
    )
}

@Preview
@PreviewScreenSizes
@Composable
@Suppress("FunctionNameMaxLength")
private fun LoadedProjectDetailsContentPreview() {
    SnagPreview {
        LoadedProjectDetailsContent(
            state =
                ProjectDetailsUiState(
                    projectStatus = ProjectDetailsUiStatus.LOADED,
                    project =
                        AppProjectData(
                            id = UuidProvider.getUuid(),
                            name = "Example project name",
                            address = "Example project address",
                            creatorId = UuidProvider.getUuid(),
                            updatedAt = Timestamp(0L),
                        ),
                    assignedUsers =
                        kotlinx.collections.immutable.persistentListOf(
                            AssignedUserItem(
                                id = UuidProvider.getUuid(),
                                email = "user1@example.com",
                                role = UserRole.ADMINISTRATOR,
                            ),
                            AssignedUserItem(
                                id = UuidProvider.getUuid(),
                                email = "user2@example.com",
                                role = UserRole.SERVICE_WORKER,
                            ),
                        ),
                    creatorEmail = "creator@example.com",
                ),
            onNewStructureClick = {},
            onStructureClick = { _, _ -> },
            onNewInspectionClick = {},
            onInspectionClick = {},
            onStartInspection = {},
            onEndInspection = {},
            onBack = {},
            onEditClick = {},
            onDelete = {},
            onDownloadReportClick = {},
            onToggleClose = {},
            onManageAssignmentsClick = {},
            onAssignUser = {},
            onRemoveUser = {},
        )
    }
}
