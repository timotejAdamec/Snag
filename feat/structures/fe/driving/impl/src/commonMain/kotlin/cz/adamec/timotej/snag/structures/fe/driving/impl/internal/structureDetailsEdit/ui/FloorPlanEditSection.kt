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

package cz.adamec.timotej.snag.structures.fe.driving.impl.internal.structureDetailsEdit.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import cz.adamec.timotej.snag.lib.design.fe.button.OutlinedIconTextButton
import cz.adamec.timotej.snag.lib.design.fe.theme.SnagTheme
import cz.adamec.timotej.snag.structures.fe.driving.impl.internal.structureDetailsEdit.ui.components.ChangePlanImageButton
import cz.adamec.timotej.snag.structures.fe.driving.impl.internal.structureDetailsEdit.ui.components.PlanImagePickButton
import org.jetbrains.compose.resources.stringResource
import snag.feat.structures.fe.driving.impl.generated.resources.Res
import snag.feat.structures.fe.driving.impl.generated.resources.add
import snag.feat.structures.fe.driving.impl.generated.resources.floor_plan
import snag.feat.structures.fe.driving.impl.generated.resources.remove
import snag.lib.design.fe.generated.resources.ic_add
import snag.lib.design.fe.generated.resources.ic_close
import snag.lib.design.fe.generated.resources.Res as DesignRes

private const val ASPECT_RATIO_WIDTH = 16f
private const val ASPECT_RATIO_HEIGHT = 9f

@Suppress("CognitiveComplexMethod")
@Composable
internal fun FloorPlanEditSection(
    floorPlanUrl: String?,
    isUploading: Boolean,
    onImagePick: (bytes: ByteArray, fileName: String) -> Unit,
    onRemoveImage: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = MaterialTheme.shapes.medium,
    ) {
        Column(
            modifier = Modifier
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(Res.string.floor_plan),
                style = MaterialTheme.typography.labelMedium,
            )
            if (floorPlanUrl == null && !isUploading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(ASPECT_RATIO_WIDTH / ASPECT_RATIO_HEIGHT),
                    contentAlignment = Alignment.Center,
                ) {
                    PlanImagePickButton(
                        modifier = modifier,
                        icon = DesignRes.drawable.ic_add,
                        label = stringResource(Res.string.add),
                        isTonal = true,
                        onImagePick = onImagePick,
                    )
                }
            } else {
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .aspectRatio(ASPECT_RATIO_WIDTH / ASPECT_RATIO_HEIGHT),
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        when {
                            isUploading -> {
                                LoadingIndicator()
                            }

                            floorPlanUrl != null -> {
                                AsyncImage(
                                    modifier = Modifier
                                        .fillMaxSize(),
                                    model = floorPlanUrl,
                                    contentDescription = null,
                                    contentScale = ContentScale.Fit,
                                )
                            }
                        }
                    }
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    ChangePlanImageButton(
                        onImagePick = onImagePick,
                    )
                    OutlinedIconTextButton(
                        onClick = onRemoveImage,
                        icon = DesignRes.drawable.ic_close,
                        label = stringResource(Res.string.remove),
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun FloorPlanEditSectionNoImagePreview() {
    SnagTheme {
        FloorPlanEditSection(
            modifier = Modifier
                .padding(16.dp),
            floorPlanUrl = null,
            isUploading = false,
            onImagePick = { _, _ -> },
            onRemoveImage = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun FloorPlanEditSectionWithImagePreview() {
    SnagTheme {
        FloorPlanEditSection(
            modifier = Modifier
                .padding(16.dp),
            floorPlanUrl = "https://saterdesign.com/cdn/shop/products/6842.M_1200x.jpeg?v=1547874083",
            isUploading = false,
            onImagePick = { _, _ -> },
            onRemoveImage = {},
        )
    }
}

@Preview(
    showBackground = true,
)
@Composable
private fun FloorPlanEditSectionUploadingPreview() {
    SnagTheme {
        FloorPlanEditSection(
            modifier = Modifier
                .padding(16.dp),
            floorPlanUrl = null,
            isUploading = true,
            onImagePick = { _, _ -> },
            onRemoveImage = {},
        )
    }
}
