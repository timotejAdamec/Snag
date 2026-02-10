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
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.github.panpf.zoomimage.CoilZoomAsyncImage
import com.github.panpf.zoomimage.CoilZoomState
import com.github.panpf.zoomimage.rememberCoilZoomState
import cz.adamec.timotej.snag.feat.findings.business.FindingType
import cz.adamec.timotej.snag.feat.findings.fe.driving.api.findingTypeVisuals
import cz.adamec.timotej.snag.feat.findings.fe.model.FrontendFinding
import kotlinx.collections.immutable.ImmutableList
import org.jetbrains.compose.resources.painterResource
import kotlin.math.sqrt
import kotlin.uuid.Uuid

@Composable
internal fun FloorPlanWithPins(
    floorPlanUrl: String,
    contentDescription: String,
    findings: ImmutableList<FrontendFinding>,
    selectedFindingId: Uuid?,
    onFindingClick: (Uuid) -> Unit,
    modifier: Modifier = Modifier,
) {
    val zoomState = rememberCoilZoomState()
    val touchTargetRadiusPx = with(LocalDensity.current) { 16.dp.toPx() }

    Box(modifier = modifier) {
        CoilZoomAsyncImage(
            modifier = Modifier.fillMaxSize(),
            zoomState = zoomState,
            model = floorPlanUrl,
            contentDescription = contentDescription,
            contentScale = ContentScale.Fit,
            onTap = { tapOffset ->
                val tappedId = findTappedFinding(
                    tapOffset = tapOffset,
                    displayRect = zoomState.zoomable.contentDisplayRectF,
                    findings = findings,
                    touchTargetRadiusPx = touchTargetRadiusPx,
                )
                if (tappedId != null) {
                    onFindingClick(tappedId)
                }
            },
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
    findings: List<FrontendFinding>,
    selectedFindingId: Uuid?,
    modifier: Modifier = Modifier,
) {
    val displayRect = zoomableState.zoomable.contentDisplayRectF
    if (displayRect.isEmpty) return

    val classicVisuals = findingTypeVisuals(FindingType.Classic())
    val unvisitedVisuals = findingTypeVisuals(FindingType.Unvisited)
    val noteVisuals = findingTypeVisuals(FindingType.Note)

    val classicPainter = painterResource(classicVisuals.icon)
    val unvisitedPainter = painterResource(unvisitedVisuals.icon)
    val notePainter = painterResource(noteVisuals.icon)

    val selectedPinColor = MaterialTheme.colorScheme.primary

    Canvas(modifier = modifier.fillMaxSize()) {
        findings.forEach { finding ->
            finding.finding.coordinates.forEach { coord ->
                val drawPoint =
                    Offset(
                        x = displayRect.left + coord.x * displayRect.width,
                        y = displayRect.top + coord.y * displayRect.height,
                    )
                val isSelected = finding.finding.id == selectedFindingId
                val iconSize = if (isSelected) 36.dp.toPx() else 28.dp.toPx()

                val (painter, color) =
                    when (finding.finding.type) {
                        is FindingType.Classic -> classicPainter to classicVisuals.pinColor
                        is FindingType.Unvisited -> unvisitedPainter to unvisitedVisuals.pinColor
                        is FindingType.Note -> notePainter to noteVisuals.pinColor
                    }

                drawFindingPin(
                    center = drawPoint,
                    painter = painter,
                    size = iconSize,
                    tint = if (isSelected) selectedPinColor else color,
                )
            }
        }
    }
}

private fun DrawScope.drawFindingPin(
    center: Offset,
    painter: Painter,
    size: Float,
    tint: androidx.compose.ui.graphics.Color,
) {
    // Use diagonal of icon square to calculate circle sizes (ensures full icon visibility)
    val iconDiagonal = size * 1.41f / 2
    val haloRadius = iconDiagonal + 3.dp.toPx()
    val ringRadius = iconDiagonal + 1.dp.toPx()

    // Draw white halo (outermost layer)
    drawCircle(
        color = Color.White,
        radius = haloRadius,
        center = center,
    )

    // Draw colored ring
    drawCircle(
        color = tint,
        radius = ringRadius,
        center = center,
    )

    // Draw white icon on top
    translate(
        left = center.x - size / 2,
        top = center.y - size / 2,
    ) {
        with(painter) {
            draw(
                size = Size(size, size),
                colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(Color.White),
            )
        }
    }
}

private fun findTappedFinding(
    tapOffset: Offset,
    displayRect: Rect,
    findings: List<FrontendFinding>,
    touchTargetRadiusPx: Float,
): Uuid? {
    if (displayRect.isEmpty) return null

    var closestId: Uuid? = null
    var closestDistance = Float.MAX_VALUE

    for (finding in findings) {
        for (coord in finding.finding.coordinates) {
            val pinCenter = Offset(
                x = displayRect.left + coord.x * displayRect.width,
                y = displayRect.top + coord.y * displayRect.height,
            )
            val dx = tapOffset.x - pinCenter.x
            val dy = tapOffset.y - pinCenter.y
            val distance = sqrt(dx * dx + dy * dy)
            if (distance <= touchTargetRadiusPx && distance < closestDistance) {
                closestDistance = distance
                closestId = finding.finding.id
            }
        }
    }

    return closestId
}
