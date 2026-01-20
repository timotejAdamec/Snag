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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cz.adamec.timotej.snag.projects.fe.driving.impl.internal.project.vm.ProjectDetailsEditState
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import snag.feat.projects.fe.driving.impl.generated.resources.Res
import snag.feat.projects.fe.driving.impl.generated.resources.project_address_label
import snag.feat.projects.fe.driving.impl.generated.resources.project_name_label
import snag.feat.projects.fe.driving.impl.generated.resources.required
import snag.lib.design.fe.generated.resources.Res as DesignRes
import snag.lib.design.fe.generated.resources.ic_location

@Composable
internal fun ProjectDetailsEditContent(
    state: ProjectDetailsEditState,
    onProjectNameChange: (String) -> Unit,
    onProjectAddressChange: (String) -> Unit,
    onSaveClick: () -> Unit,
    onCancelClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .padding(start = 12.dp, end = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        OutlinedTextField(
            modifier = Modifier
                .widthIn(max = 300.dp)
                .fillMaxWidth(),
            label = { Text(text = stringResource(Res.string.project_name_label) + "*") },
            supportingText = { Text(text = stringResource(Res.string.required) + "*") },
            value = state.projectName,
            singleLine = true,
            onValueChange = {
                onProjectNameChange(it)
            },
        )
        OutlinedTextField(
            modifier = Modifier
                .widthIn(max = 300.dp)
                .fillMaxWidth(),
            label = { Text(text = stringResource(Res.string.project_address_label) + "*") },
            supportingText = { Text(text = stringResource(Res.string.required) + "*") },
            value = state.projectAddress,
            onValueChange = {
                onProjectAddressChange(it)
            },
            singleLine = true,
            leadingIcon = {
                Icon(
                    painter = painterResource(DesignRes.drawable.ic_location),
                    contentDescription = "Address",
                )
            }
        )
    }
}
