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

import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cz.adamec.timotej.snag.authorization.business.UserRole
import cz.adamec.timotej.snag.users.fe.driving.impl.internal.userManagement.vm.UserItem
import org.jetbrains.compose.resources.painterResource
import snag.lib.design.fe.generated.resources.ic_person
import snag.lib.design.fe.generated.resources.Res as DesignRes

@Composable
internal fun UserListItem(
    userItem: UserItem,
    onRoleSelect: (UserRole?) -> Unit,
    modifier: Modifier = Modifier,
) {
    ListItem(
        modifier = modifier,
        leadingContent = {
            Icon(
                painter = painterResource(DesignRes.drawable.ic_person),
                contentDescription = null,
            )
        },
        headlineContent = {
            Text(
                text = userItem.email,
                style = MaterialTheme.typography.titleMedium,
            )
        },
        supportingContent = {
            RoleDropdown(
                selectedRole = userItem.role,
                allowedRoleOptions = userItem.allowedRoleOptions,
                onRoleSelect = onRoleSelect,
                enabled = userItem.isRoleChangeEnabled,
            )
        },
    )
}
