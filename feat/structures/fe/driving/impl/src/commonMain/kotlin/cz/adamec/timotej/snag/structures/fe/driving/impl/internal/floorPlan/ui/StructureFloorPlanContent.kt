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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cz.adamec.timotej.snag.lib.design.fe.scaffold.BackNavigationIcon
import cz.adamec.timotej.snag.lib.design.fe.scenes.LocalSheetPeekHeight
import cz.adamec.timotej.snag.structures.fe.driving.impl.internal.floorPlan.ui.components.FloorPlanPlaceholder
import cz.adamec.timotej.snag.structures.fe.driving.impl.internal.floorPlan.ui.components.FloorPlanWithPins
import cz.adamec.timotej.snag.structures.fe.driving.impl.internal.floorPlan.ui.components.StructureDeletionAlertDialog
import cz.adamec.timotej.snag.structures.fe.driving.impl.internal.floorPlan.vm.StructureDetailsUiState
import cz.adamec.timotej.snag.structures.fe.driving.impl.internal.floorPlan.vm.StructureDetailsUiStatus
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import snag.feat.structures.fe.driving.impl.generated.resources.Res
import snag.feat.structures.fe.driving.impl.generated.resources.structure_not_found
import snag.lib.design.fe.generated.resources.delete
import snag.lib.design.fe.generated.resources.edit
import snag.lib.design.fe.generated.resources.ic_delete
import snag.lib.design.fe.generated.resources.ic_edit
import kotlin.uuid.Uuid
import snag.lib.design.fe.generated.resources.Res as DesignRes

@Composable
internal fun StructureFloorPlanContent(
    state: StructureDetailsUiState,
    onBack: () -> Unit,
    onEditClick: () -> Unit,
    onDelete: () -> Unit,
    onFindingClick: (Uuid) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier,
    ) {
        when (state.status) {
            StructureDetailsUiStatus.NOT_FOUND -> {
                Text(
                    modifier = Modifier.align(Alignment.Center),
                    text = stringResource(Res.string.structure_not_found),
                )
            }

            StructureDetailsUiStatus.LOADING -> {
                ContainedLoadingIndicator(
                    modifier = Modifier.align(Alignment.Center),
                )
            }

            StructureDetailsUiStatus.LOADED,
            StructureDetailsUiStatus.DELETED,
            -> {
                LoadedStructureDetailsContent(
                    state = state,
                    onBack = onBack,
                    onEditClick = onEditClick,
                    onDelete = onDelete,
                    onFindingClick = onFindingClick,
                )
            }

            StructureDetailsUiStatus.ERROR -> {
                onBack()
            }
        }
    }
}

@Suppress("LongMethod")
@Composable
private fun LoadedStructureDetailsContent(
    state: StructureDetailsUiState,
    onBack: () -> Unit,
    onEditClick: () -> Unit,
    onDelete: () -> Unit,
    onFindingClick: (Uuid) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text =
                            state.feStructure
                                ?.structure
                                ?.name
                                .orEmpty(),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                navigationIcon = {
                    BackNavigationIcon(
                        onClick = onBack,
                    )
                },
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
            val floorPlanUrl = state.feStructure?.structure?.floorPlanUrl
            if (floorPlanUrl != null) {
                FloorPlanWithPins(
                    modifier = Modifier.fillMaxSize(),
                    floorPlanUrl = floorPlanUrl,
                    contentDescription = state.feStructure.structure.name,
                    findings = state.findings,
                    selectedFindingId = state.selectedFindingId,
                    onFindingClick = onFindingClick,
                )
            } else {
                FloorPlanPlaceholder(
                    modifier =
                        Modifier
                            .fillMaxSize(),
                )
            }

            var isShowingDeleteConfirmation by remember { mutableStateOf(false) }
            if (isShowingDeleteConfirmation) {
                StructureDeletionAlertDialog(
                    areButtonsEnabled = state.canInvokeDeletion,
                    onDelete = onDelete,
                    onDismiss = {
                        isShowingDeleteConfirmation = false
                    },
                )
            }
            val toolbarBottomPadding =
                if (LocalSheetPeekHeight.current == 0.dp) {
                    16.dp
                } else {
                    LocalSheetPeekHeight.current
                }
            HorizontalFloatingToolbar(
                modifier =
                    Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = toolbarBottomPadding),
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
