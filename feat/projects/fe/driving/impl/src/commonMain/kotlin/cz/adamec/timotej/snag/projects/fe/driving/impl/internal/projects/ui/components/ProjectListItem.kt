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
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cz.adamec.timotej.snag.lib.core.common.Timestamp
import cz.adamec.timotej.snag.lib.core.common.UuidProvider
import cz.adamec.timotej.snag.lib.design.fe.theme.SnagTheme
import cz.adamec.timotej.snag.projects.business.Project
import cz.adamec.timotej.snag.projects.fe.model.FrontendProject
import org.jetbrains.compose.resources.painterResource
import snag.lib.design.fe.generated.resources.ic_chevron_right
import snag.lib.design.fe.generated.resources.Res as DesignRes

@Composable
internal fun ProjectListItem(
    project: FrontendProject,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ListItem(
        modifier =
            modifier
                .clip(shape = MaterialTheme.shapes.large)
                .clickable(onClick = onClick),
        headlineContent = {
            Text(
                text = project.project.name,
                style = MaterialTheme.typography.titleLargeEmphasized,
            )
        },
        supportingContent = {
            Text(
                text = project.project.address,
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
    )
}

@Composable
@Preview
internal fun ProjectListItemPreview() {
    SnagTheme {
        ProjectListItem(
            modifier =
                Modifier.size(
                    width = 400.dp,
                    height = 100.dp,
                ),
            project =
                FrontendProject(
                    project =
                        Project(
                            id = UuidProvider.getUuid(),
                            name = "Project A",
                            address = "Client A",
                            updatedAt = Timestamp(0L),
                        ),
                ),
            onClick = {},
        )
    }
}
