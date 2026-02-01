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

package cz.adamec.timotej.snag.structures.fe.driving.impl.internal.floorPlan.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import com.github.panpf.zoomimage.CoilZoomAsyncImage
import com.github.panpf.zoomimage.CoilZoomState
import com.github.panpf.zoomimage.rememberCoilZoomState
import cz.adamec.timotej.snag.feat.findings.business.Finding
import kotlinx.collections.immutable.ImmutableList
import kotlin.uuid.Uuid

@Composable
internal fun FloorPlanWithPins(
    floorPlanUrl: String,
    contentDescription: String,
    findings: ImmutableList<Finding>,
    selectedFindingId: Uuid?,
    modifier: Modifier = Modifier,
) {
    val zoomState = rememberCoilZoomState()

    Box(modifier = modifier) {
        CoilZoomAsyncImage(
            modifier = Modifier.fillMaxSize(),
            zoomState = zoomState,
            model = floorPlanUrl,
            contentDescription = contentDescription,
            contentScale = ContentScale.Fit,
        )

        FindingsPinsOverlay(
            zoomableState = zoomState,
            findings = findings,
            selectedFindingId = selectedFindingId,
        )
    }
}


@Composable
private fun FindingsPinsOverlay(
    zoomableState: CoilZoomState,
    findings: List<Finding>,
    selectedFindingId: Uuid?,
    modifier: Modifier = Modifier,
) {
    val displayRect = zoomableState.zoomable.contentDisplayRectF
    if (displayRect.isEmpty) return

    val displayedFindings =
        if (selectedFindingId != null) {
            findings.filter { it.id == selectedFindingId }
        } else {
            findings
        }

    val pinColor = MaterialTheme.colorScheme.error
    val selectedPinColor = MaterialTheme.colorScheme.tertiary
    val pinOutlineColor = MaterialTheme.colorScheme.onError

    Canvas(modifier = modifier.fillMaxSize()) {
        displayedFindings.forEach { finding ->
            finding.coordinates.forEach { coord ->
                val drawPoint =
                    Offset(
                        x = displayRect.left + coord.x * displayRect.width,
                        y = displayRect.top + coord.y * displayRect.height,
                    )
                val isSelected = finding.id == selectedFindingId

                drawFindingPin(
                    center = drawPoint,
                    fillColor = if (isSelected) selectedPinColor else pinColor,
                    outlineColor = pinOutlineColor,
                    radius = if (isSelected) 24f else 16f,
                )
            }
        }
    }
}

private fun DrawScope.drawFindingPin(
    center: Offset,
    fillColor: Color,
    outlineColor: Color,
    radius: Float,
) {
    drawCircle(
        color = fillColor,
        radius = radius,
        center = center,
        style = Fill,
    )
    drawCircle(
        color = outlineColor,
        radius = radius,
        center = center,
        style = Stroke(width = 2f),
    )
}
