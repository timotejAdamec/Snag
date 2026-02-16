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

package cz.adamec.timotej.snag.feat.structures.fe.driving.api

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import cz.adamec.timotej.snag.feat.structures.business.Structure
import cz.adamec.timotej.snag.feat.structures.fe.model.FrontendStructure
import cz.adamec.timotej.snag.lib.core.common.Timestamp
import cz.adamec.timotej.snag.lib.core.common.UuidProvider
import cz.adamec.timotej.snag.lib.design.fe.theme.SnagPreview

@Composable
fun StructureCard(
    feStructure: FrontendStructure,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ElevatedCard(
        modifier = modifier,
        onClick = onClick,
    ) {
        if (feStructure.structure.floorPlanUrl != null) {
            Column {
                AsyncImage(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .clip(MaterialTheme.shapes.medium),
                    model = feStructure.structure.floorPlanUrl,
                    contentDescription = feStructure.structure.name,
                    contentScale = ContentScale.Fit,
                )
                Text(
                    modifier = Modifier.padding(12.dp),
                    text = feStructure.structure.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                )
            }
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    modifier = Modifier.padding(12.dp),
                    text = feStructure.structure.name,
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                )
            }
        }
    }
}

@Preview
@Composable
private fun StructureCardPreview() {
    SnagPreview {
        Box(
            modifier = Modifier.size(400.dp),
            contentAlignment = Alignment.Center,
        ) {
            StructureCard(
                feStructure =
                    FrontendStructure(
                        Structure(
                            id = UuidProvider.getUuid(),
                            projectId = UuidProvider.getUuid(),
                            name = "Structure Name",
                            floorPlanUrl = "https://saterdesign.com/cdn/shop/products/6842.M_1200x.jpeg?v=1547874083",
                            updatedAt = Timestamp(1L),
                        ),
                    ),
                onClick = {},
            )
        }
    }
}
