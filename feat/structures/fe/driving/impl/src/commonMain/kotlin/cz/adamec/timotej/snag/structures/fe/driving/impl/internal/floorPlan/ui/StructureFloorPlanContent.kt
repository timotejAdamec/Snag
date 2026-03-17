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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cz.adamec.timotej.snag.feat.findings.business.FindingTypeKey
import cz.adamec.timotej.snag.feat.findings.business.RelativeCoordinate
import cz.adamec.timotej.snag.feat.findings.fe.driving.api.FindingTypePickerDialog
import cz.adamec.timotej.snag.lib.design.fe.adaptive.LocalIsInContentPane
import cz.adamec.timotej.snag.lib.design.fe.scaffold.BackNavigationIcon
import cz.adamec.timotej.snag.lib.design.fe.scenes.LocalSheetPeekHeight
import cz.adamec.timotej.snag.structures.fe.driving.impl.internal.floorPlan.ui.components.FloorPlanAddPlaceholder
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
    onCreateFinding: (coordinate: RelativeCoordinate, findingTypeKey: FindingTypeKey) -> Unit,
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

            StructureDetailsUiStatus.LOADING -> {}

            StructureDetailsUiStatus.LOADED,
            StructureDetailsUiStatus.DELETED,
            -> {
                LoadedStructureDetailsContent(
                    state = state,
                    onBack = onBack,
                    onEditClick = onEditClick,
                    onDelete = onDelete,
                    onFindingClick = onFindingClick,
                    onCreateFinding = onCreateFinding,
                )
            }

            StructureDetailsUiStatus.ERROR -> {
                onBack()
            }
        }
    }
}

@Suppress("LongMethod", "CognitiveComplexMethod")
@Composable
private fun LoadedStructureDetailsContent(
    state: StructureDetailsUiState,
    onBack: () -> Unit,
    onEditClick: () -> Unit,
    onDelete: () -> Unit,
    onFindingClick: (Uuid) -> Unit,
    onCreateFinding: (coordinate: RelativeCoordinate, findingTypeKey: FindingTypeKey) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        containerColor =
            if (LocalIsInContentPane.current) Color.Transparent else Color.Unspecified,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        modifier =
                            Modifier.padding(
                                end = 4.dp,
                            ),
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
        val density = LocalDensity.current
        val layoutDirection = LocalLayoutDirection.current
        var boxHeightPx by remember { mutableIntStateOf(0) }
        var toolbarTopPx by remember { mutableIntStateOf(0) }

        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .onGloballyPositioned { boxHeightPx = it.size.height },
        ) {
            var pendingCreationCoordinate by remember {
                mutableStateOf<RelativeCoordinate?>(null)
            }
            val toolbarBottomPadding =
                if (LocalSheetPeekHeight.current == 0.dp) {
                    16.dp
                } else {
                    LocalSheetPeekHeight.current + 16.dp
                }
            val bottomFromToolbar =
                with(density) { (boxHeightPx - toolbarTopPx).toDp() } + 8.dp
            val floorPlanContentPadding =
                PaddingValues(
                    start = paddingValues.calculateStartPadding(layoutDirection),
                    top = paddingValues.calculateTopPadding(),
                    bottom = bottomFromToolbar,
                )

            val floorPlanUrl = state.feStructure?.structure?.floorPlanUrl
            if (floorPlanUrl != null) {
                FloorPlanWithPins(
                    modifier = Modifier.fillMaxSize(),
                    floorPlanUrl = floorPlanUrl,
                    contentDescription = state.feStructure.structure.name,
                    findings = state.findings,
                    selectedFindingId = state.selectedFindingId,
                    contentPadding = floorPlanContentPadding,
                    onFindingClick = onFindingClick,
                    onEmptySpaceTap = { coordinate ->
                        if (state.canCreateFinding) {
                            pendingCreationCoordinate = coordinate
                        }
                    },
                )
            } else {
                FloorPlanAddPlaceholder(
                    modifier =
                        Modifier
                            .fillMaxSize(),
                )
            }

            pendingCreationCoordinate?.let { coordinate ->
                FindingTypePickerDialog(
                    onTypeSelect = { findingTypeKey ->
                        onCreateFinding(coordinate, findingTypeKey)
                        pendingCreationCoordinate = null
                    },
                    onDismiss = {
                        pendingCreationCoordinate = null
                    },
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
            AnimatedVisibility(
                modifier = Modifier.align(Alignment.BottomCenter),
                visible = state.selectedFindingId == null,
                enter = fadeIn() + slideInVertically { it },
                exit = fadeOut() + slideOutVertically { it },
            ) {
                HorizontalFloatingToolbar(
                    modifier =
                        Modifier
                            .padding(bottom = toolbarBottomPadding)
                            .onGloballyPositioned {
                                toolbarTopPx = it.positionInParent().y.toInt()
                            },
                    expanded = true,
                ) {
                    IconButton(
                        enabled = state.canEdit,
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
}
