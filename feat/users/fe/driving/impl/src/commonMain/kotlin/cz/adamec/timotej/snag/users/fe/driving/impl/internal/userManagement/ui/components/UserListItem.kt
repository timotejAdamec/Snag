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

import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cz.adamec.timotej.snag.users.business.UserRole
import cz.adamec.timotej.snag.users.fe.driving.impl.internal.userManagement.vm.UserItem

@Composable
internal fun UserListItem(
    userItem: UserItem,
    onRoleSelect: (UserRole?) -> Unit,
    modifier: Modifier = Modifier,
) {
    ListItem(
        modifier = modifier,
        headlineContent = {
            Text(
                text = userItem.email,
                style = MaterialTheme.typography.bodyLarge,
            )
        },
        trailingContent = {
            RoleDropdown(
                selectedRole = userItem.role,
                onRoleSelect = onRoleSelect,
                enabled = !userItem.isUpdatingRole,
            )
        },
    )
}
