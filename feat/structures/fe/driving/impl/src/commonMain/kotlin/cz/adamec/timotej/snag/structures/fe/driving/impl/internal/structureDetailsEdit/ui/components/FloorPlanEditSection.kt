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

package cz.adamec.timotej.snag.structures.fe.driving.impl.internal.structureDetailsEdit.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import cz.adamec.timotej.snag.lib.design.fe.button.OutlinedIconTextButton
import cz.adamec.timotej.snag.lib.design.fe.theme.SnagTheme
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import snag.feat.structures.fe.driving.impl.generated.resources.Res
import snag.feat.structures.fe.driving.impl.generated.resources.error_loading_image
import snag.feat.structures.fe.driving.impl.generated.resources.floor_plan
import snag.feat.structures.fe.driving.impl.generated.resources.remove
import snag.feat.structures.fe.driving.impl.generated.resources.upload
import snag.lib.design.fe.generated.resources.ic_close
import snag.lib.design.fe.generated.resources.ic_error
import snag.lib.design.fe.generated.resources.ic_upload
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
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = stringResource(Res.string.floor_plan),
            style = MaterialTheme.typography.labelLarge,
        )
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            shape = MaterialTheme.shapes.medium,
        ) {
            Column(
                modifier = Modifier
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (floorPlanUrl == null && !isUploading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(ASPECT_RATIO_WIDTH / ASPECT_RATIO_HEIGHT),
                        contentAlignment = Alignment.Center,
                    ) {
                        PlanImagePickButton(
                            modifier = modifier,
                            icon = DesignRes.drawable.ic_upload,
                            label = stringResource(Res.string.upload),
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
                            var isImageLoading by remember { mutableStateOf(false) }
                            var hasImageError by remember { mutableStateOf(false) }

                            if (floorPlanUrl != null) {
                                AsyncImage(
                                    modifier = Modifier
                                        .fillMaxSize(),
                                    model = floorPlanUrl,
                                    contentDescription = null,
                                    contentScale = ContentScale.Fit,
                                    onLoading = {
                                        isImageLoading = true
                                        hasImageError = false
                                    },
                                    onError = {
                                        isImageLoading = false
                                        hasImageError = true
                                    },
                                    onSuccess = {
                                        isImageLoading = false
                                        hasImageError = false
                                    },
                                )
                            }

                            if ((isUploading || isImageLoading) && !hasImageError) {
                                LoadingIndicator()
                            }

                            if (hasImageError) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    Icon(
                                        painter = painterResource(DesignRes.drawable.ic_error),
                                        contentDescription = null,
                                    )
                                    Text(
                                        text = stringResource(Res.string.error_loading_image),
                                        style = MaterialTheme.typography.bodyMedium,
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

@Preview(showBackground = true)
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

@Preview(showBackground = true)
@Composable
private fun FloorPlanEditSectionWithImageErrorPreview() {
    SnagTheme {
        FloorPlanEditSection(
            modifier = Modifier
                .padding(16.dp),
            floorPlanUrl = "nonexistent URL",
            isUploading = false,
            onImagePick = { _, _ -> },
            onRemoveImage = {},
        )
    }
}
