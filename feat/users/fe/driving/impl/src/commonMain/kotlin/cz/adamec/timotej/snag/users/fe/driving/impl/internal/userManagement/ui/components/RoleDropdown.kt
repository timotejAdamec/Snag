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

package cz.adamec.timotej.snag.users.fe.driving.impl.internal.userManagement.ui.components

import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cz.adamec.timotej.snag.users.business.UserRole
import cz.adamec.timotej.snag.users.fe.driving.impl.internal.userManagement.ui.toDisplayName
import org.jetbrains.compose.resources.stringResource
import snag.feat.users.fe.driving.impl.generated.resources.Res
import snag.feat.users.fe.driving.impl.generated.resources.no_role
import snag.feat.users.fe.driving.impl.generated.resources.role_label

@Composable
internal fun RoleDropdown(
    selectedRole: UserRole?,
    onRoleSelect: (UserRole?) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        modifier = modifier.widthIn(max = 220.dp),
        expanded = expanded,
        onExpandedChange = { if (enabled) expanded = it },
    ) {
        OutlinedTextField(
            modifier =
                Modifier
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
            label = { Text(stringResource(Res.string.role_label)) },
            value = selectedRole?.toDisplayName() ?: stringResource(Res.string.no_role),
            onValueChange = {},
            readOnly = true,
            singleLine = true,
            enabled = enabled,
            textStyle = MaterialTheme.typography.titleMedium,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(Res.string.no_role)) },
                onClick = {
                    onRoleSelect(null)
                    expanded = false
                },
            )
            HorizontalDivider()
            UserRole.entries.forEach { role ->
                DropdownMenuItem(
                    text = { Text(role.toDisplayName()) },
                    onClick = {
                        onRoleSelect(role)
                        expanded = false
                    },
                )
            }
        }
    }
}
