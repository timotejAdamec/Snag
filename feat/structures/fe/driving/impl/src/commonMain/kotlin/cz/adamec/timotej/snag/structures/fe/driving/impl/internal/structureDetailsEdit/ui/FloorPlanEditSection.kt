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
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import cz.adamec.timotej.snag.structures.fe.driving.impl.internal.floorPlan.ui.components.FloorPlanPlaceholder
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.openFilePicker
import io.github.vinceglb.filekit.name
import io.github.vinceglb.filekit.readBytes
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import snag.feat.structures.fe.driving.impl.generated.resources.Res
import snag.feat.structures.fe.driving.impl.generated.resources.add_floor_plan
import snag.feat.structures.fe.driving.impl.generated.resources.change_floor_plan
import snag.feat.structures.fe.driving.impl.generated.resources.remove_floor_plan

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
    val scope = rememberCoroutineScope()

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Surface(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(ASPECT_RATIO_WIDTH / ASPECT_RATIO_HEIGHT)
                    .clip(MaterialTheme.shapes.medium),
            color = MaterialTheme.colorScheme.surfaceVariant,
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                when {
                    isUploading -> {
                        CircularProgressIndicator()
                    }
                    floorPlanUrl != null -> {
                        AsyncImage(
                            modifier = Modifier.fillMaxSize(),
                            model = floorPlanUrl,
                            contentDescription = null,
                            contentScale = ContentScale.Fit,
                        )
                    }
                    else -> {
                        FloorPlanPlaceholder(
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Button(
                onClick = {
                    scope.launch {
                        val file =
                            FileKit.openFilePicker(
                                type = FileKitType.Image,
                            )
                        if (file != null) {
                            val bytes = file.readBytes()
                            onImagePick(bytes, file.name)
                        }
                    }
                },
            ) {
                Text(
                    text =
                        if (floorPlanUrl != null) {
                            stringResource(Res.string.change_floor_plan)
                        } else {
                            stringResource(Res.string.add_floor_plan)
                        },
                )
            }
            if (floorPlanUrl != null) {
                OutlinedButton(
                    onClick = onRemoveImage,
                ) {
                    Text(text = stringResource(Res.string.remove_floor_plan))
                }
            }
        }
    }
}
