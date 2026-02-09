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

package cz.adamec.timotej.snag.findings.fe.driving.impl.internal.findingDetailsEdit.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import cz.adamec.timotej.snag.feat.findings.business.Importance
import cz.adamec.timotej.snag.lib.design.fe.dialog.FullScreenDialogMeasurements
import cz.adamec.timotej.snag.feat.findings.business.Term
import cz.adamec.timotej.snag.findings.fe.driving.impl.internal.findingDetailsEdit.vm.FindingDetailsEditUiState
import cz.adamec.timotej.snag.lib.design.fe.theme.SnagTheme
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import snag.feat.findings.fe.driving.impl.generated.resources.Res
import snag.feat.findings.fe.driving.impl.generated.resources.finding_description_label
import snag.feat.findings.fe.driving.impl.generated.resources.finding_name_label
import snag.feat.findings.fe.driving.impl.generated.resources.importance_high
import snag.feat.findings.fe.driving.impl.generated.resources.importance_label
import snag.feat.findings.fe.driving.impl.generated.resources.importance_low
import snag.feat.findings.fe.driving.impl.generated.resources.importance_medium
import snag.feat.findings.fe.driving.impl.generated.resources.new_finding
import snag.feat.findings.fe.driving.impl.generated.resources.term_con
import snag.feat.findings.fe.driving.impl.generated.resources.term_label
import snag.feat.findings.fe.driving.impl.generated.resources.term_t1
import snag.feat.findings.fe.driving.impl.generated.resources.term_t2
import snag.feat.findings.fe.driving.impl.generated.resources.term_t3
import snag.feat.findings.fe.driving.impl.generated.resources.required
import snag.lib.design.fe.generated.resources.close
import snag.lib.design.fe.generated.resources.ic_close
import snag.lib.design.fe.generated.resources.save
import snag.lib.design.fe.generated.resources.Res as DesignRes

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Suppress("LongMethod")
@Composable
internal fun FindingDetailsEditContent(
    isEditMode: Boolean,
    state: FindingDetailsEditUiState,
    snackbarHostState: SnackbarHostState,
    onFindingNameChange: (String) -> Unit,
    onFindingDescriptionChange: (String) -> Unit,
    onImportanceChange: (Importance) -> Unit,
    onTermChange: (Term) -> Unit,
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
                        if (!isEditMode && state.findingName.isBlank()) {
                            stringResource(Res.string.new_finding)
                        } else {
                            state.findingName
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
                    .padding(horizontal = FullScreenDialogMeasurements.HorizontalPadding),
            verticalArrangement = Arrangement.spacedBy(FullScreenDialogMeasurements.ElementSpacing),
        ) {
            OutlinedTextField(
                modifier =
                    Modifier
                        .fillMaxWidth(),
                label = { Text(text = stringResource(Res.string.finding_name_label) + "*") },
                supportingText = { Text(text = stringResource(Res.string.required) + "*") },
                value = state.findingName,
                singleLine = true,
                onValueChange = {
                    onFindingNameChange(it)
                },
            )
            Text(
                text = stringResource(Res.string.importance_label),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            val importanceOptions = Importance.entries
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
            ) {
                importanceOptions.forEachIndexed { index, importance ->
                    ToggleButton(
                        checked = state.findingImportance == importance,
                        onCheckedChange = { onImportanceChange(importance) },
                        modifier =
                            Modifier
                                .weight(1f)
                                .semantics { role = Role.RadioButton },
                        colors = ToggleButtonDefaults.tonalToggleButtonColors(),
                        shapes =
                            when (index) {
                                0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                                importanceOptions.lastIndex ->
                                    ButtonGroupDefaults.connectedTrailingButtonShapes()
                                else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                            },
                    ) {
                        Text(
                            text =
                                when (importance) {
                                    Importance.HIGH -> stringResource(Res.string.importance_high)
                                    Importance.MEDIUM -> stringResource(Res.string.importance_medium)
                                    Importance.LOW -> stringResource(Res.string.importance_low)
                                },
                        )
                    }
                }
            }
            Text(
                text = stringResource(Res.string.term_label),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            val termOptions = Term.entries
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
            ) {
                termOptions.forEachIndexed { index, term ->
                    ToggleButton(
                        checked = state.findingTerm == term,
                        onCheckedChange = { onTermChange(term) },
                        modifier =
                            Modifier
                                .weight(1f)
                                .semantics { role = Role.RadioButton },
                        colors = ToggleButtonDefaults.tonalToggleButtonColors(),
                        shapes =
                            when (index) {
                                0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                                termOptions.lastIndex ->
                                    ButtonGroupDefaults.connectedTrailingButtonShapes()
                                else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                            },
                    ) {
                        Text(
                            text =
                                when (term) {
                                    Term.T1 -> stringResource(Res.string.term_t1)
                                    Term.T2 -> stringResource(Res.string.term_t2)
                                    Term.T3 -> stringResource(Res.string.term_t3)
                                    Term.CON -> stringResource(Res.string.term_con)
                                },
                        )
                    }
                }
            }
            OutlinedTextField(
                modifier =
                    Modifier
                        .fillMaxWidth(),
                label = { Text(text = stringResource(Res.string.finding_description_label)) },
                value = state.findingDescription,
                minLines = 2,
                maxLines = 6,
                onValueChange = {
                    onFindingDescriptionChange(it)
                },
            )
        }
    }
}

@Preview
@Composable
private fun FindingDetailsEditContentPreview() {
    SnagTheme {
        FindingDetailsEditContent(
            isEditMode = true,
            state = FindingDetailsEditUiState(
                findingName = "Example Finding",
                findingDescription = "Example Description",
                findingImportance = Importance.MEDIUM,
            ),
            snackbarHostState = SnackbarHostState(),
            onFindingNameChange = {},
            onFindingDescriptionChange = {},
            onImportanceChange = {},
            onTermChange = {},
            onSaveClick = {},
            onCancelClick = {},
        )
    }
}
