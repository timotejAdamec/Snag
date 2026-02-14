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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import cz.adamec.timotej.snag.feat.inspections.fe.driving.api.InspectionCard
import cz.adamec.timotej.snag.feat.structures.fe.driving.api.StructureCard
import cz.adamec.timotej.snag.lib.core.common.Timestamp
import cz.adamec.timotej.snag.lib.core.common.UuidProvider
import cz.adamec.timotej.snag.lib.design.fe.button.AdaptiveTonalButton
import cz.adamec.timotej.snag.lib.design.fe.scaffold.BackNavigationIcon
import cz.adamec.timotej.snag.lib.design.fe.scaffold.CollapsableTopAppBarScaffold
import cz.adamec.timotej.snag.lib.design.fe.theme.SnagTheme
import cz.adamec.timotej.snag.projects.business.Project
import cz.adamec.timotej.snag.projects.fe.driving.impl.internal.projectDetails.vm.InspectionsUiStatus
import cz.adamec.timotej.snag.projects.fe.driving.impl.internal.projectDetails.vm.ProjectDetailsUiState
import cz.adamec.timotej.snag.projects.fe.driving.impl.internal.projectDetails.vm.ProjectDetailsUiStatus
import cz.adamec.timotej.snag.projects.fe.driving.impl.internal.projectDetails.vm.StructuresUiStatus
import cz.adamec.timotej.snag.projects.fe.model.FrontendProject
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import snag.feat.projects.fe.driving.impl.generated.resources.Res
import snag.feat.projects.fe.driving.impl.generated.resources.delete_project_confirmation_text
import snag.feat.projects.fe.driving.impl.generated.resources.delete_project_confirmation_title
import snag.feat.projects.fe.driving.impl.generated.resources.inspections_section_title
import snag.feat.projects.fe.driving.impl.generated.resources.new_inspection
import snag.feat.projects.fe.driving.impl.generated.resources.new_structure
import snag.feat.projects.fe.driving.impl.generated.resources.project_not_found
import snag.feat.projects.fe.driving.impl.generated.resources.structures_section_title
import snag.lib.design.fe.generated.resources.delete
import snag.lib.design.fe.generated.resources.edit
import snag.lib.design.fe.generated.resources.ic_add
import snag.lib.design.fe.generated.resources.ic_delete
import snag.lib.design.fe.generated.resources.ic_edit
import kotlin.uuid.Uuid
import snag.lib.design.fe.generated.resources.Res as DesignRes

@Composable
internal fun ProjectDetailsContent(
    state: ProjectDetailsUiState,
    onNewStructureClick: () -> Unit,
    onStructureClick: (structureId: Uuid) -> Unit,
    onNewInspectionClick: () -> Unit,
    onInspectionClick: (inspectionId: Uuid) -> Unit,
    onBack: () -> Unit,
    onEditClick: () -> Unit,
    onDelete: () -> Unit,
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

            ProjectDetailsUiStatus.LOADING -> {
                ContainedLoadingIndicator(
                    modifier = Modifier.align(Alignment.Center),
                )
            }

            ProjectDetailsUiStatus.LOADED,
            ProjectDetailsUiStatus.DELETED,
            -> {
                LoadedProjectDetailsContent(
                    state = state,
                    onNewStructureClick = onNewStructureClick,
                    onStructureClick = onStructureClick,
                    onNewInspectionClick = onNewInspectionClick,
                    onInspectionClick = onInspectionClick,
                    onBack = onBack,
                    onEditClick = onEditClick,
                    onDelete = onDelete,
                )
            }

            ProjectDetailsUiStatus.ERROR -> {
                onBack()
            }
        }
    }
}

@Suppress("LongMethod")
@Composable
private fun LoadedProjectDetailsContent(
    state: ProjectDetailsUiState,
    onNewStructureClick: () -> Unit,
    onStructureClick: (structureId: Uuid) -> Unit,
    onNewInspectionClick: () -> Unit,
    onInspectionClick: (inspectionId: Uuid) -> Unit,
    onBack: () -> Unit,
    onEditClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val project = state.project?.project
    val projectName = project?.name.orEmpty()
    val projectAddress = project?.address.orEmpty()
    CollapsableTopAppBarScaffold(
        modifier = modifier,
        title = projectName,
        subtitle = projectAddress,
        topAppBarNavigationIcon = {
            BackNavigationIcon(
                onClick = onBack,
            )
        },
        topAppBarActions = {
            AdaptiveTonalButton(
                onClick = onNewStructureClick,
                icon = painterResource(DesignRes.drawable.ic_add),
                label = stringResource(Res.string.new_structure),
            )
            AdaptiveTonalButton(
                onClick = onNewInspectionClick,
                icon = painterResource(DesignRes.drawable.ic_add),
                label = stringResource(Res.string.new_inspection),
            )
        },
    ) { paddingValues ->
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .consumeWindowInsets(paddingValues),
        ) {
            @OptIn(ExperimentalLayoutApi::class)
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding =
                    PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 16.dp,
                        bottom = 72.dp,
                    ),
            ) {
                item {
                    Text(
                        text = stringResource(Res.string.structures_section_title),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp),
                    )
                }
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.TopCenter,
                    ) {
                        FlowRow(
                            modifier = Modifier.widthIn(max = 936.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            maxItemsInEachRow = 3,
                        ) {
                            state.structures.forEach { structure ->
                                StructureCard(
                                    modifier =
                                        Modifier
                                            .widthIn(min = 150.dp)
                                            .weight(1f)
                                            .heightIn(min = 200.dp, max = 260.dp),
                                    feStructure = structure,
                                    onClick = { onStructureClick(structure.structure.id) },
                                )
                            }
                        }
                    }
                }
                item {
                    AnimatedVisibility(
                        visible = state.structureStatus == StructuresUiStatus.LOADING,
                        exit = fadeOut(),
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                        ) {
                            LoadingIndicator()
                        }
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = stringResource(Res.string.inspections_section_title),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp),
                    )
                }
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.TopCenter,
                    ) {
                        FlowRow(
                            modifier = Modifier.widthIn(max = 936.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            maxItemsInEachRow = 3,
                        ) {
                            state.inspections.forEach { inspection ->
                                InspectionCard(
                                    modifier =
                                        Modifier
                                            .widthIn(min = 150.dp)
                                            .weight(1f),
                                    feInspection = inspection,
                                    onClick = {
                                        onInspectionClick(inspection.inspection.id)
                                    },
                                )
                            }
                        }
                    }
                }
                item {
                    AnimatedVisibility(
                        visible = state.inspectionStatus == InspectionsUiStatus.LOADING,
                        exit = fadeOut(),
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                        ) {
                            LoadingIndicator()
                        }
                    }
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
                        .padding(bottom = 16.dp),
                expanded = true,
            ) {
                IconButton(
                    onClick = onEditClick,
                ) {
                    Icon(
                        painter = painterResource(DesignRes.drawable.ic_edit),
                        contentDescription = stringResource(DesignRes.string.edit),
                    )
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
    SnagTheme {
        LoadedProjectDetailsContent(
            state =
                ProjectDetailsUiState(
                    projectStatus = ProjectDetailsUiStatus.LOADED,
                    project =
                        FrontendProject(
                            project =
                                Project(
                                    id = UuidProvider.getUuid(),
                                    name = "Example project name",
                                    address = "Example project address",
                                    updatedAt = Timestamp(0L),
                                ),
                        ),
                ),
            onNewStructureClick = {},
            onStructureClick = {},
            onNewInspectionClick = {},
            onInspectionClick = {},
            onBack = {},
            onEditClick = {},
            onDelete = {},
        )
    }
}
