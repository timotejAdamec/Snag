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
import androidx.compose.ui.unit.dp
import cz.adamec.timotej.snag.findings.fe.driving.impl.internal.findingDetailsEdit.vm.FindingDetailsEditUiState
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import snag.feat.findings.fe.driving.impl.generated.resources.Res
import snag.feat.findings.fe.driving.impl.generated.resources.finding_description_label
import snag.feat.findings.fe.driving.impl.generated.resources.finding_name_label
import snag.feat.findings.fe.driving.impl.generated.resources.new_finding
import snag.feat.findings.fe.driving.impl.generated.resources.required
import snag.lib.design.fe.generated.resources.close
import snag.lib.design.fe.generated.resources.ic_close
import snag.lib.design.fe.generated.resources.save
import snag.lib.design.fe.generated.resources.Res as DesignRes

private val HorizontalPadding = 12.dp

@Composable
internal fun FindingDetailsEditContent(
    isEditMode: Boolean,
    state: FindingDetailsEditUiState,
    snackbarHostState: SnackbarHostState,
    onFindingNameChange: (String) -> Unit,
    onFindingDescriptionChange: (String) -> Unit,
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
                        end = HorizontalPadding,
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
                    .padding(horizontal = HorizontalPadding),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedTextField(
                modifier =
                    Modifier
                        .fillMaxWidth(),
                label = { Text(text = stringResource(Res.string.finding_name_label) + "*") },
                supportingText = { Text(text = stringResource(Res.string.required) + "*") },
                value = state.findingName,
                onValueChange = {
                    onFindingNameChange(it)
                },
            )
            OutlinedTextField(
                modifier =
                    Modifier
                        .fillMaxWidth(),
                label = { Text(text = stringResource(Res.string.finding_description_label)) },
                value = state.findingDescription,
                onValueChange = {
                    onFindingDescriptionChange(it)
                },
            )
        }
    }
}
