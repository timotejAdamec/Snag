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

package cz.adamec.timotej.snag.projects.fe.driving.impl.internal.projects.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cz.adamec.timotej.snag.core.foundation.common.Timestamp
import cz.adamec.timotej.snag.core.foundation.common.UuidProvider
import cz.adamec.timotej.snag.lib.design.fe.theme.SnagPreview
import cz.adamec.timotej.snag.projects.app.model.AppProject
import cz.adamec.timotej.snag.projects.app.model.AppProjectData
import org.jetbrains.compose.resources.painterResource
import snag.lib.design.fe.generated.resources.ic_chevron_right
import snag.lib.design.fe.generated.resources.ic_lock
import snag.lib.design.fe.generated.resources.Res as DesignRes

private const val CLOSED_ALPHA = 0.6f

@Composable
internal fun ProjectListItem(
    project: AppProject,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isClosed = project.isClosed
    ListItem(
        modifier =
            modifier
                .clip(shape = MaterialTheme.shapes.large)
                .clickable(onClick = onClick)
                .then(if (isClosed) Modifier.alpha(CLOSED_ALPHA) else Modifier),
        headlineContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = project.name,
                    style = MaterialTheme.typography.titleLargeEmphasized,
                )
                if (isClosed) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        modifier = Modifier.size(18.dp),
                        painter = painterResource(DesignRes.drawable.ic_lock),
                        contentDescription = null,
                    )
                }
            }
        },
        supportingContent = {
            Text(
                text = project.address,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 2,
            )
        },
        trailingContent = {
            Icon(
                painter = painterResource(DesignRes.drawable.ic_chevron_right),
                contentDescription = null,
            )
        },
        colors =
            ListItemDefaults.colors(
                containerColor = Color.Transparent,
            ),
    )
}

@Composable
@Preview
internal fun ProjectListItemPreview() {
    SnagPreview {
        ProjectListItem(
            modifier =
                Modifier.size(
                    width = 400.dp,
                    height = 100.dp,
                ),
            project =
                AppProjectData(
                    id = UuidProvider.getUuid(),
                    name = "Project A",
                    address = "Client A",
                    creatorId = UuidProvider.getUuid(),
                    updatedAt = Timestamp(0L),
                ),
            onClick = {},
        )
    }
}
