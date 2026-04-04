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

package cz.adamec.timotej.snag.findings.fe.driving.impl.internal.findingDetail.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import cz.adamec.timotej.snag.feat.findings.app.model.AppFindingPhoto
import cz.adamec.timotej.snag.lib.design.fe.dialog.DialogBackHandler
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.name
import io.github.vinceglb.filekit.readBytes
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import snag.feat.findings.fe.driving.impl.generated.resources.Res
import snag.feat.findings.fe.driving.impl.generated.resources.add_photo
import snag.feat.findings.fe.driving.impl.generated.resources.delete_photo
import snag.feat.findings.fe.driving.impl.generated.resources.photos_section_title
import snag.lib.design.fe.generated.resources.ic_add
import snag.lib.design.fe.generated.resources.ic_delete
import kotlin.uuid.Uuid
import snag.lib.design.fe.generated.resources.Res as DesignRes

private val ThumbnailSize = 120.dp

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun FindingPhotoSection(
    photos: List<AppFindingPhoto>,
    canEdit: Boolean,
    canModifyPhotos: Boolean,
    isAddingPhoto: Boolean,
    onAddPhoto: (bytes: ByteArray, fileName: String) -> Unit,
    onDeletePhoto: (Uuid) -> Unit,
    modifier: Modifier = Modifier,
) {
    var fullscreenPhotoIndex by remember { mutableStateOf<Int?>(null) }
    DialogBackHandler(
        enabled = fullscreenPhotoIndex != null,
        onBack = { fullscreenPhotoIndex = null },
    )

    fullscreenPhotoIndex?.let { index ->
        FullscreenPhotoViewer(
            photos = photos,
            initialPhotoIndex = index,
            onDismiss = { fullscreenPhotoIndex = null },
        )
    }

    val scope = rememberCoroutineScope()
    val pickerLauncher =
        rememberFilePickerLauncher(
            type = FileKitType.Image,
        ) { file ->
            if (file != null) {
                scope.launch {
                    onAddPhoto(file.readBytes(), file.name)
                }
            }
        }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = stringResource(Res.string.photos_section_title),
                style = MaterialTheme.typography.titleSmall,
            )
            if (canEdit && canModifyPhotos) {
                IconButton(
                    onClick = { pickerLauncher.launch() },
                    enabled = !isAddingPhoto,
                ) {
                    Icon(
                        painter = painterResource(DesignRes.drawable.ic_add),
                        contentDescription = stringResource(Res.string.add_photo),
                    )
                }
            }
        }
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            photos.forEachIndexed { index, photo ->
                FindingPhotoThumbnail(
                    photo = photo,
                    canEdit = canEdit && canModifyPhotos,
                    onDeletePhoto = onDeletePhoto,
                    onClick = { fullscreenPhotoIndex = index },
                )
            }
            if (isAddingPhoto) {
                Surface(
                    modifier = Modifier.size(ThumbnailSize),
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        LoadingIndicator()
                    }
                }
            }
        }
    }
}

@Composable
private fun FindingPhotoThumbnail(
    photo: AppFindingPhoto,
    canEdit: Boolean,
    onDeletePhoto: (Uuid) -> Unit,
    onClick: () -> Unit,
) {
    var isShowingDeleteConfirmation by remember { mutableStateOf(false) }

    if (isShowingDeleteConfirmation) {
        FindingPhotoDeletionDialog(
            onDelete = {
                onDeletePhoto(photo.id)
                isShowingDeleteConfirmation = false
            },
            onDismiss = {
                isShowingDeleteConfirmation = false
            },
        )
    }

    Box(
        modifier = Modifier.size(ThumbnailSize),
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            onClick = onClick,
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surfaceContainerLow,
        ) {
            AsyncImage(
                model = photo.url,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        }
        if (canEdit) {
            IconButton(
                modifier =
                    Modifier
                        .align(Alignment.TopEnd)
                        .size(24.dp),
                onClick = {
                    isShowingDeleteConfirmation = true
                },
            ) {
                Icon(
                    painter = painterResource(DesignRes.drawable.ic_delete),
                    contentDescription = stringResource(Res.string.delete_photo),
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}
