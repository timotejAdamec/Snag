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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.Dialog
import com.github.panpf.zoomimage.CoilZoomAsyncImage
import com.github.panpf.zoomimage.rememberCoilZoomState
import cz.adamec.timotej.snag.core.foundation.common.Timestamp
import cz.adamec.timotej.snag.feat.findings.app.model.AppFindingPhoto
import cz.adamec.timotej.snag.feat.findings.app.model.AppFindingPhotoData
import cz.adamec.timotej.snag.lib.design.fe.api.dialog.fullscreenDialogProperties
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import snag.feat.findings.fe.driving.impl.generated.resources.Res
import snag.feat.findings.fe.driving.impl.generated.resources.photo_counter
import snag.lib.design.fe.api.generated.resources.close
import snag.lib.design.fe.api.generated.resources.ic_close
import kotlin.uuid.Uuid
import snag.lib.design.fe.api.generated.resources.Res as DesignRes

@Composable
internal fun FullscreenPhotoViewer(
    photos: List<AppFindingPhoto>,
    initialPhotoIndex: Int,
    onDismiss: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = fullscreenDialogProperties(),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.scrim),
        ) {
            val pagerState =
                rememberPagerState(
                    initialPage = initialPhotoIndex,
                    pageCount = { photos.size },
                )

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
            ) { page ->
                val zoomState = rememberCoilZoomState()
                CoilZoomAsyncImage(
                    modifier = Modifier.fillMaxSize(),
                    zoomState = zoomState,
                    model = photos[page].url,
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                )
            }

            TopAppBar(
                title = {
                    if (photos.size > 1) {
                        Text(
                            text =
                                stringResource(
                                    Res.string.photo_counter,
                                    pagerState.currentPage + 1,
                                    photos.size,
                                ),
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onDismiss,
                        colors =
                            IconButtonDefaults.iconButtonColors(
                                contentColor = MaterialTheme.colorScheme.onSurface,
                            ),
                    ) {
                        Icon(
                            painter = painterResource(DesignRes.drawable.ic_close),
                            contentDescription = stringResource(DesignRes.string.close),
                        )
                    }
                },
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.5f),
                    ),
            )
        }
    }
}

@Preview
@Composable
private fun FullscreenPhotoViewerSinglePreview() {
    FullscreenPhotoViewer(
        photos =
            listOf(
                AppFindingPhotoData(
                    id = Uuid.random(),
                    findingId = Uuid.random(),
                    url = "",
                    createdAt = Timestamp(0L),
                ),
            ),
        initialPhotoIndex = 0,
        onDismiss = {},
    )
}

@Preview
@Composable
@Suppress("MagicNumber")
private fun FullscreenPhotoViewerMultiplePreview() {
    FullscreenPhotoViewer(
        photos =
            List(5) {
                AppFindingPhotoData(
                    id = Uuid.random(),
                    findingId = Uuid.random(),
                    url = "",
                    createdAt = Timestamp(0L),
                )
            },
        initialPhotoIndex = 2,
        onDismiss = {},
    )
}
