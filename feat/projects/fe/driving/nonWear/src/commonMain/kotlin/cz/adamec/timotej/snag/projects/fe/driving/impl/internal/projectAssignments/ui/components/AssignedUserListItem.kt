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

package cz.adamec.timotej.snag.projects.fe.driving.impl.internal.projectAssignments.ui.components

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cz.adamec.timotej.snag.projects.fe.driving.impl.internal.projectAssignments.vm.AssignedUserItem
import cz.adamec.timotej.snag.users.fe.driving.api.toDisplayName
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import snag.feat.projects.fe.driving.nonwear.generated.resources.Res
import snag.feat.projects.fe.driving.nonwear.generated.resources.remove_user_from_project
import snag.lib.design.fe.generated.resources.ic_delete
import snag.lib.design.fe.generated.resources.ic_person
import kotlin.uuid.Uuid
import snag.lib.design.fe.generated.resources.Res as DesignRes

@Composable
internal fun AssignedUserListItem(
    userItem: AssignedUserItem,
    canRemove: Boolean,
    onRemoveClick: (Uuid) -> Unit,
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
            userItem.role?.let { role ->
                Text(text = role.toDisplayName())
            }
        },
        trailingContent = {
            if (canRemove) {
                IconButton(
                    onClick = { onRemoveClick(userItem.id) },
                ) {
                    Icon(
                        painter = painterResource(DesignRes.drawable.ic_delete),
                        contentDescription = stringResource(Res.string.remove_user_from_project),
                    )
                }
            }
        },
    )
}
