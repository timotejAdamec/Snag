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

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cz.adamec.timotej.snag.projects.fe.driving.impl.internal.project.vm.ProjectDetailsEditState
import org.jetbrains.compose.resources.stringResource
import snag.feat.projects.fe.driving.impl.generated.resources.Res
import snag.feat.projects.fe.driving.impl.generated.resources.project_address_label
import snag.feat.projects.fe.driving.impl.generated.resources.project_name_label

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
        modifier = modifier,
    ) {
        OutlinedTextField(
            label = { Text(text = stringResource(Res.string.project_name_label)) },
            value = state.projectName,
            onValueChange = {
                onProjectNameChange(it)
            },
        )
        OutlinedTextField(
            label = { Text(text = stringResource(Res.string.project_address_label)) },
            value = state.projectAddress,
            onValueChange = {
                onProjectAddressChange(it)
            },
        )
    }
}
