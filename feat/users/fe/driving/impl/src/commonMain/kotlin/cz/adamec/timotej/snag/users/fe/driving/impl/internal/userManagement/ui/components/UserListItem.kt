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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cz.adamec.timotej.snag.authorization.business.UserRole
import cz.adamec.timotej.snag.users.fe.driving.impl.internal.userManagement.vm.UserItem

@Composable
internal fun UserListItem(
    userItem: UserItem,
    onRoleSelect: (UserRole?) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .padding(
                    horizontal = 16.dp,
                    vertical = 12.dp,
                ),
    ) {
        Text(
            text = userItem.email,
            style = MaterialTheme.typography.titleMedium,
        )
        RoleDropdown(
            modifier = Modifier.padding(top = 2.dp),
            selectedRole = userItem.role,
            onRoleSelect = onRoleSelect,
            enabled = !userItem.isUpdatingRole,
        )
    }
}
