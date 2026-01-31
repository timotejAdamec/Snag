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

package cz.adamec.timotej.snag.structures.fe.driving.impl.internal.structureDetails.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.github.panpf.zoomimage.CoilZoomAsyncImage
import cz.adamec.timotej.snag.lib.design.fe.scaffold.BackNavigationIcon
import cz.adamec.timotej.snag.structures.fe.driving.impl.internal.structureDetails.vm.StructureDetailsUiState
import cz.adamec.timotej.snag.structures.fe.driving.impl.internal.structureDetails.vm.StructureDetailsUiStatus
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import snag.feat.structures.fe.driving.impl.generated.resources.Res
import snag.feat.structures.fe.driving.impl.generated.resources.delete_structure_confirmation_text
import snag.feat.structures.fe.driving.impl.generated.resources.delete_structure_confirmation_title
import snag.feat.structures.fe.driving.impl.generated.resources.no_floor_plan
import snag.feat.structures.fe.driving.impl.generated.resources.structure_not_found
import snag.lib.design.fe.generated.resources.delete
import snag.lib.design.fe.generated.resources.ic_delete
import snag.lib.design.fe.generated.resources.Res as DesignRes

@Composable
internal fun StructureDetailsContent(
    state: StructureDetailsUiState,
    onBack: () -> Unit,
    onDelete: () -> Unit,
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
                    onDelete = onDelete,
                )
            }

            StructureDetailsUiStatus.ERROR -> {
                onBack()
            }
        }
    }
}

@Composable
private fun LoadedStructureDetailsContent(
    state: StructureDetailsUiState,
    onBack: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(text = state.structure?.name.orEmpty())
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
            val floorPlanUrl = state.structure?.floorPlanUrl
            if (floorPlanUrl != null) {
                CoilZoomAsyncImage(
                    model = floorPlanUrl,
                    contentDescription = state.structure?.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit,
                )
            } else {
                FloorPlanPlaceholder(
                    modifier = Modifier.align(Alignment.Center),
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
            HorizontalFloatingToolbar(
                modifier =
                    Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 16.dp),
                expanded = true,
            ) {
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
private fun FloorPlanPlaceholder(modifier: Modifier = Modifier) {
    Text(
        modifier = modifier,
        text = stringResource(Res.string.no_floor_plan),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun StructureDeletionAlertDialog(
    areButtonsEnabled: Boolean,
    onDelete: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        title = {
            Text(text = stringResource(Res.string.delete_structure_confirmation_title))
        },
        text = {
            Text(text = stringResource(Res.string.delete_structure_confirmation_text))
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
