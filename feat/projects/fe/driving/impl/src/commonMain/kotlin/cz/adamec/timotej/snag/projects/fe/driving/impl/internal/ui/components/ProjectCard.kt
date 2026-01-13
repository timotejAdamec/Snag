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

package cz.adamec.timotej.snag.projects.fe.driving.impl.internal.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cz.adamec.timotej.snag.lib.core.UuidProvider
import cz.adamec.timotej.snag.lib.design.fe.theme.SnagTheme
import cz.adamec.timotej.snag.projects.business.Project

@Composable
internal fun ProjectCard(
    project: Project,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedCard(
        modifier = modifier,
        onClick = onClick,
        colors =
            CardDefaults.outlinedCardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
            ),
    ) {
        Column(
            modifier =
                Modifier
                    .padding(
                        start = 24.dp,
                        end = 24.dp,
                        top = 12.dp,
                        bottom = 12.dp,
                    ),
        ) {
            Text(
                text = project.name,
                style = MaterialTheme.typography.titleLargeEmphasized,
            )
            Text(
                text = "Client A",
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}

@Composable
@Preview
internal fun ProjectCardPreview() {
    SnagTheme {
        ProjectCard(
            modifier =
                Modifier.size(
                    width = 200.dp,
                    height = 100.dp,
                ),
            project =
                Project(
                    id = UuidProvider.getUuid(),
                    name = "Project A",
                    address = "Client A",
                ),
            onClick = {},
        )
    }
}
