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

package cz.adamec.timotej.snag.projects.fe.driving.impl.internal.project.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cz.adamec.timotej.snag.projects.fe.driving.impl.internal.project.vm.ProjectDetailsEditState
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import snag.feat.projects.fe.driving.impl.generated.resources.Res
import snag.feat.projects.fe.driving.impl.generated.resources.new_project
import snag.feat.projects.fe.driving.impl.generated.resources.project_address_label
import snag.feat.projects.fe.driving.impl.generated.resources.project_name_label
import snag.feat.projects.fe.driving.impl.generated.resources.required
import snag.lib.design.fe.generated.resources.close
import snag.lib.design.fe.generated.resources.ic_close
import snag.lib.design.fe.generated.resources.Res as DesignRes
import snag.lib.design.fe.generated.resources.ic_location
import snag.lib.design.fe.generated.resources.save
import kotlin.uuid.Uuid

private val HorizontalPadding = 12.dp

@Composable
internal fun ProjectDetailsEditContent(
    projectId: Uuid?,
    state: ProjectDetailsEditState,
    onProjectNameChange: (String) -> Unit,
    onProjectAddressChange: (String) -> Unit,
    onSaveClick: () -> Unit,
    onCancelClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                modifier = Modifier
                    .fillMaxWidth(),
                title = {
                    val text = if (projectId == null && state.projectName.isBlank()) {
                        stringResource(Res.string.new_project)
                    } else {
                        state.projectName
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
                            text = stringResource(DesignRes.string.save)
                        )
                    }
                },
                contentPadding = PaddingValues(
                    end = HorizontalPadding,
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = HorizontalPadding),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth(),
                label = { Text(text = stringResource(Res.string.project_name_label) + "*") },
                supportingText = { Text(text = stringResource(Res.string.required) + "*") },
                value = state.projectName,
                onValueChange = {
                    onProjectNameChange(it)
                },
            )
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth(),
                label = { Text(text = stringResource(Res.string.project_address_label) + "*") },
                supportingText = { Text(text = stringResource(Res.string.required) + "*") },
                value = state.projectAddress,
                onValueChange = {
                    onProjectAddressChange(it)
                },
                leadingIcon = {
                    Icon(
                        painter = painterResource(DesignRes.drawable.ic_location),
                        contentDescription = "Address",
                    )
                }
            )
        }
    }
}
