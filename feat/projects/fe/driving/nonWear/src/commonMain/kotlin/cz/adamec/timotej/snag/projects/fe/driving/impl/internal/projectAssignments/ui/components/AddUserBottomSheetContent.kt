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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cz.adamec.timotej.snag.authorization.business.UserRole
import cz.adamec.timotej.snag.core.foundation.common.UuidProvider
import cz.adamec.timotej.snag.lib.design.fe.theme.SnagPreview
import cz.adamec.timotej.snag.projects.fe.driving.impl.internal.projectAssignments.vm.AssignedUserItem
import cz.adamec.timotej.snag.users.fe.driving.api.toDisplayName
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import org.jetbrains.compose.resources.painterResource
import snag.lib.design.fe.generated.resources.ic_person
import kotlin.uuid.Uuid
import snag.lib.design.fe.generated.resources.Res as DesignRes

@Composable
internal fun AddUserBottomSheetContent(
    availableUsers: ImmutableList<AssignedUserItem>,
    onUserClick: (Uuid) -> Unit,
) {
    LazyColumn(
        contentPadding = PaddingValues(bottom = 32.dp),
    ) {
        items(
            items = availableUsers,
            key = { it.id },
        ) { userItem ->
            ListItem(
                modifier =
                    Modifier
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                        .clip(MaterialTheme.shapes.small)
                        .clickable { onUserClick(userItem.id) },
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
            )
        }
    }
}

@Preview
@Composable
private fun AddUserBottomSheetContentPreview() {
    SnagPreview {
        AddUserBottomSheetContent(
            availableUsers =
                persistentListOf(
                    AssignedUserItem(
                        id = UuidProvider.getUuid(),
                        email = "available1@example.com",
                        role = UserRole.PASSPORT_LEAD,
                    ),
                    AssignedUserItem(
                        id = UuidProvider.getUuid(),
                        email = "available2@example.com",
                        role = UserRole.SERVICE_WORKER,
                    ),
                    AssignedUserItem(
                        id = UuidProvider.getUuid(),
                        email = "available3@example.com",
                        role = null,
                    ),
                ),
            onUserClick = {},
        )
    }
}
