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

package cz.adamec.timotej.snag.feat.inspections.fe.driving.impl.internal.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import cz.adamec.timotej.snag.feat.inspections.fe.driving.impl.internal.vm.InspectionEditUiState
import cz.adamec.timotej.snag.lib.design.fe.dialog.FullScreenDialogMeasurements
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import snag.feat.inspections.fe.driving.impl.generated.resources.Res
import snag.feat.inspections.fe.driving.impl.generated.resources.climate_label
import snag.feat.inspections.fe.driving.impl.generated.resources.ended_at_label
import snag.feat.inspections.fe.driving.impl.generated.resources.new_inspection
import snag.feat.inspections.fe.driving.impl.generated.resources.note_label
import snag.feat.inspections.fe.driving.impl.generated.resources.participants_label
import snag.feat.inspections.fe.driving.impl.generated.resources.started_at_label
import snag.lib.design.fe.generated.resources.close
import snag.lib.design.fe.generated.resources.ic_close
import snag.lib.design.fe.generated.resources.save
import snag.lib.design.fe.generated.resources.Res as DesignRes

@Suppress("LongMethod")
@Composable
internal fun InspectionEditContent(
    isEditMode: Boolean,
    state: InspectionEditUiState,
    snackbarHostState: SnackbarHostState,
    onStartedAtChange: (String) -> Unit,
    onEndedAtChange: (String) -> Unit,
    onParticipantsChange: (String) -> Unit,
    onClimateChange: (String) -> Unit,
    onNoteChange: (String) -> Unit,
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
                        if (!isEditMode && state.participants.isBlank()) {
                            stringResource(Res.string.new_inspection)
                        } else {
                            state.participants.ifBlank {
                                stringResource(Res.string.new_inspection)
                            }
                        }
                    Text(
                        text = text,
                        maxLines = 1,
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
                    .consumeWindowInsets(paddingValues),
            verticalArrangement = Arrangement.spacedBy(FullScreenDialogMeasurements.ElementSpacing),
        ) {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                label = { Text(text = stringResource(Res.string.participants_label)) },
                value = state.participants,
                onValueChange = onParticipantsChange,
            )
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                label = { Text(text = stringResource(Res.string.climate_label)) },
                value = state.climate,
                onValueChange = onClimateChange,
            )
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                label = { Text(text = stringResource(Res.string.started_at_label)) },
                value = state.startedAt,
                onValueChange = onStartedAtChange,
            )
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                label = { Text(text = stringResource(Res.string.ended_at_label)) },
                value = state.endedAt,
                onValueChange = onEndedAtChange,
            )
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                label = { Text(text = stringResource(Res.string.note_label)) },
                value = state.note,
                onValueChange = onNoteChange,
                minLines = 3,
            )
        }
    }
}
