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
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import cz.adamec.timotej.snag.feat.inspections.fe.driving.impl.internal.ui.components.DateTimePickerField
import cz.adamec.timotej.snag.feat.inspections.fe.driving.impl.internal.vm.InspectionEditUiState
import cz.adamec.timotej.snag.lib.core.common.Timestamp
import cz.adamec.timotej.snag.lib.design.fe.dialog.FullScreenDialogMeasurements
import cz.adamec.timotej.snag.lib.design.fe.dialog.TimePickerDialog
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import snag.feat.inspections.fe.driving.impl.generated.resources.Res
import snag.feat.inspections.fe.driving.impl.generated.resources.climate_label
import snag.feat.inspections.fe.driving.impl.generated.resources.ended_at_label
import snag.feat.inspections.fe.driving.impl.generated.resources.new_inspection
import snag.feat.inspections.fe.driving.impl.generated.resources.not_set
import snag.feat.inspections.fe.driving.impl.generated.resources.note_label
import snag.feat.inspections.fe.driving.impl.generated.resources.participants_label
import snag.feat.inspections.fe.driving.impl.generated.resources.started_at_label
import snag.lib.design.fe.generated.resources.cancel
import snag.lib.design.fe.generated.resources.close
import snag.lib.design.fe.generated.resources.ic_close
import snag.lib.design.fe.generated.resources.ok
import snag.lib.design.fe.generated.resources.save
import snag.lib.design.fe.generated.resources.Res as DesignRes

private sealed interface PickerStep {
    data object None : PickerStep

    data object DateStep : PickerStep

    data class TimeStep(
        val selectedDateMillis: Long,
    ) : PickerStep
}

@Suppress("LongMethod", "CognitiveComplexMethod", "CyclomaticComplexMethod")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun InspectionEditContent(
    isEditMode: Boolean,
    state: InspectionEditUiState,
    snackbarHostState: SnackbarHostState,
    onStartedAtChange: (Timestamp?) -> Unit,
    onEndedAtChange: (Timestamp?) -> Unit,
    onParticipantsChange: (String) -> Unit,
    onClimateChange: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onSaveClick: () -> Unit,
    onCancelClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var startedAtPickerStep by remember { mutableStateOf<PickerStep>(PickerStep.None) }
    var endedAtPickerStep by remember { mutableStateOf<PickerStep>(PickerStep.None) }

    when (val step = startedAtPickerStep) {
        PickerStep.None -> {}
        PickerStep.DateStep -> {
            val datePickerState =
                rememberDatePickerState(
                    initialSelectedDateMillis = state.startedAt?.value,
                )
            DatePickerDialog(
                onDismissRequest = { startedAtPickerStep = PickerStep.None },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val dateMillis = datePickerState.selectedDateMillis
                            if (dateMillis != null) {
                                startedAtPickerStep = PickerStep.TimeStep(selectedDateMillis = dateMillis)
                            }
                        },
                    ) {
                        Text(stringResource(DesignRes.string.ok))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { startedAtPickerStep = PickerStep.None }) {
                        Text(stringResource(DesignRes.string.cancel))
                    }
                },
            ) {
                DatePicker(state = datePickerState)
            }
        }
        is PickerStep.TimeStep -> {
            val existingLocal = state.startedAt?.toLocalDateTime()
            val timePickerState =
                rememberTimePickerState(
                    initialHour = existingLocal?.hour ?: 0,
                    initialMinute = existingLocal?.minute ?: 0,
                )
            TimePickerDialog(
                state = timePickerState,
                onDismiss = { startedAtPickerStep = PickerStep.None },
                onConfirm = {
                    onStartedAtChange(
                        timestampFrom(
                            dateMillis = step.selectedDateMillis,
                            hour = timePickerState.hour,
                            minute = timePickerState.minute,
                        ),
                    )
                    startedAtPickerStep = PickerStep.None
                },
            )
        }
    }

    when (val step = endedAtPickerStep) {
        PickerStep.None -> {}
        PickerStep.DateStep -> {
            val datePickerState =
                rememberDatePickerState(
                    initialSelectedDateMillis = state.endedAt?.value,
                )
            DatePickerDialog(
                onDismissRequest = { endedAtPickerStep = PickerStep.None },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val dateMillis = datePickerState.selectedDateMillis
                            if (dateMillis != null) {
                                endedAtPickerStep = PickerStep.TimeStep(selectedDateMillis = dateMillis)
                            }
                        },
                    ) {
                        Text(stringResource(DesignRes.string.ok))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { endedAtPickerStep = PickerStep.None }) {
                        Text(stringResource(DesignRes.string.cancel))
                    }
                },
            ) {
                DatePicker(state = datePickerState)
            }
        }
        is PickerStep.TimeStep -> {
            val existingLocal = state.endedAt?.toLocalDateTime()
            val timePickerState =
                rememberTimePickerState(
                    initialHour = existingLocal?.hour ?: 0,
                    initialMinute = existingLocal?.minute ?: 0,
                )
            TimePickerDialog(
                state = timePickerState,
                onDismiss = { endedAtPickerStep = PickerStep.None },
                onConfirm = {
                    onEndedAtChange(
                        timestampFrom(
                            dateMillis = step.selectedDateMillis,
                            hour = timePickerState.hour,
                            minute = timePickerState.minute,
                        ),
                    )
                    endedAtPickerStep = PickerStep.None
                },
            )
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                modifier = Modifier.fillMaxWidth(),
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
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onCancelClick) {
                        Icon(
                            painter = painterResource(DesignRes.drawable.ic_close),
                            contentDescription = stringResource(DesignRes.string.close),
                        )
                    }
                },
                actions = {
                    Button(onClick = onSaveClick) {
                        Text(text = stringResource(DesignRes.string.save))
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
            DateTimePickerField(
                label = stringResource(Res.string.started_at_label),
                value = state.startedAt?.toDisplayString(),
                placeholder = stringResource(Res.string.not_set),
                onEditClick = { startedAtPickerStep = PickerStep.DateStep },
                onClearClick = { onStartedAtChange(null) },
                modifier = Modifier.fillMaxWidth(),
            )
            DateTimePickerField(
                label = stringResource(Res.string.ended_at_label),
                value = state.endedAt?.toDisplayString(),
                placeholder = stringResource(Res.string.not_set),
                onEditClick = { endedAtPickerStep = PickerStep.DateStep },
                onClearClick = { onEndedAtChange(null) },
                modifier = Modifier.fillMaxWidth(),
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
