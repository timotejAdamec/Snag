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

package cz.adamec.timotej.snag.structures.fe.driving.impl.internal.structureDetailsEdit.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import cz.adamec.timotej.snag.lib.design.fe.dialog.FullScreenDialogMeasurements
import cz.adamec.timotej.snag.lib.design.fe.theme.SnagPreview
import cz.adamec.timotej.snag.structures.fe.driving.impl.internal.structureDetailsEdit.ui.components.FloorPlanEditSection
import cz.adamec.timotej.snag.structures.fe.driving.impl.internal.structureDetailsEdit.vm.StructureDetailsEditUiState
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import snag.feat.structures.fe.driving.impl.generated.resources.Res
import snag.feat.structures.fe.driving.impl.generated.resources.new_structure
import snag.feat.structures.fe.driving.impl.generated.resources.required
import snag.feat.structures.fe.driving.impl.generated.resources.structure_name_label
import snag.lib.design.fe.generated.resources.close
import snag.lib.design.fe.generated.resources.ic_close
import snag.lib.design.fe.generated.resources.save
import snag.lib.design.fe.generated.resources.Res as DesignRes

@Suppress("LongMethod")
@Composable
internal fun StructureDetailsEditContent(
    state: StructureDetailsEditUiState,
    snackbarHostState: SnackbarHostState,
    onStructureNameChange: (String) -> Unit,
    onImagePick: (bytes: ByteArray, fileName: String) -> Unit,
    onRemoveImage: () -> Unit,
    onSaveClick: () -> Unit,
    onCancelClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                modifier =
                    Modifier
                        .fillMaxWidth(),
                title = {
                    val text =
                        if (!state.isEditMode && state.structureName.isBlank()) {
                            stringResource(Res.string.new_structure)
                        } else {
                            state.structureName
                        }
                    Text(
                        text = text,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onCancelClick,
                    ) {
                        Icon(
                            painter = painterResource(DesignRes.drawable.ic_close),
                            contentDescription = stringResource(DesignRes.string.close),
                        )
                    }
                },
                actions = {
                    Button(
                        onClick = onSaveClick,
                        enabled = state.canSave,
                    ) {
                        Text(
                            text = stringResource(DesignRes.string.save),
                        )
                    }
                },
                contentPadding =
                    PaddingValues(
                        end = FullScreenDialogMeasurements.HorizontalPadding,
                    ),
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
    ) { paddingValues ->
        Column(
            modifier =
                Modifier
                    .padding(paddingValues)
                    .padding(horizontal = FullScreenDialogMeasurements.HorizontalPadding)
                    .consumeWindowInsets(paddingValues)
                    .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(FullScreenDialogMeasurements.ElementSpacing),
        ) {
            FloorPlanEditSection(
                floorPlanUrl = state.floorPlanUrl,
                isUploading = state.isUploadingImage,
                canModifyImage = state.canModifyFloorPlanImage,
                onImagePick = onImagePick,
                onRemoveImage = onRemoveImage,
            )
            OutlinedTextField(
                modifier =
                    Modifier
                        .fillMaxWidth(),
                label = { Text(text = stringResource(Res.string.structure_name_label) + "*") },
                isError = state.structureNameError != null,
                supportingText = {
                    Text(
                        text =
                            state.structureNameError?.let { stringResource(it) }
                                ?: (stringResource(Res.string.required) + "*"),
                    )
                },
                value = state.structureName,
                onValueChange = {
                    onStructureNameChange(it)
                },
            )
        }
    }
}

@Preview
@Composable
private fun StructureDetailsEditContentPreview() {
    SnagPreview {
        StructureDetailsEditContent(
            state =
                StructureDetailsEditUiState(
                    structureName = "Ground Floor",
                ),
            snackbarHostState = SnackbarHostState(),
            onStructureNameChange = {},
            onImagePick = { _, _ -> },
            onRemoveImage = {},
            onSaveClick = {},
            onCancelClick = {},
        )
    }
}
