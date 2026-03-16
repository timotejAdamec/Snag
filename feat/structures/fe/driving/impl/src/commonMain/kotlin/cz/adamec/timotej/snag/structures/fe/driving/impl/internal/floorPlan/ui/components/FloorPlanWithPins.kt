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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import com.github.panpf.zoomimage.CoilZoomAsyncImage
import com.github.panpf.zoomimage.CoilZoomState
import com.github.panpf.zoomimage.rememberCoilZoomState
import com.github.panpf.zoomimage.zoom.ContainerWhitespace
import cz.adamec.timotej.snag.feat.findings.business.model.FindingType
import cz.adamec.timotej.snag.feat.findings.business.model.RelativeCoordinate
import cz.adamec.timotej.snag.feat.findings.fe.driving.api.visuals
import cz.adamec.timotej.snag.feat.findings.app.model.AppFinding
import kotlinx.collections.immutable.ImmutableList
import org.jetbrains.compose.resources.painterResource
import kotlin.math.sqrt
import kotlin.uuid.Uuid

private const val BACKGROUND_CIRCLE_SCALE = 1.3f

@Composable
internal fun FloorPlanWithPins(
    floorPlanUrl: String,
    contentDescription: String,
    findings: ImmutableList<AppFinding>,
    selectedFindingId: Uuid?,
    onFindingClick: (Uuid) -> Unit,
    onEmptySpaceTap: (RelativeCoordinate) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
) {
    val zoomState = rememberCoilZoomState()
    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current
    val touchTargetRadiusPx = with(density) { 16.dp.toPx() }

    LaunchedEffect(contentPadding) {
        with(density) {
            zoomState.zoomable.setContainerWhitespace(
                ContainerWhitespace(
                    left = contentPadding.calculateStartPadding(layoutDirection).toPx(),
                    top = contentPadding.calculateTopPadding().toPx(),
                    right = contentPadding.calculateEndPadding(layoutDirection).toPx(),
                    bottom = contentPadding.calculateBottomPadding().toPx(),
                ),
            )
        }
    }

    Box(modifier = modifier) {
        CoilZoomAsyncImage(
            modifier = Modifier.fillMaxSize(),
            zoomState = zoomState,
            model = floorPlanUrl,
            contentDescription = contentDescription,
            contentScale = ContentScale.Fit,
            onTap = { tapOffset ->
                val tappedId =
                    findTappedFinding(
                        tapOffset = tapOffset,
                        displayRect = zoomState.zoomable.contentDisplayRectF,
                        findings = findings,
                        touchTargetRadiusPx = touchTargetRadiusPx,
                    )
                if (tappedId != null) {
                    onFindingClick(tappedId)
                } else {
                    val displayRect = zoomState.zoomable.contentDisplayRectF
                    if (!displayRect.isEmpty) {
                        val x = (tapOffset.x - displayRect.left) / displayRect.width
                        val y = (tapOffset.y - displayRect.top) / displayRect.height
                        if (x in 0f..1f && y in 0f..1f) {
                            onEmptySpaceTap(RelativeCoordinate(x, y))
                        }
                    }
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

@Suppress("CognitiveComplexMethod", "CyclomaticComplexMethod", "LabeledExpression", "MagicNumber")
@Composable
private fun FindingsPinsOverlay(
    zoomableState: CoilZoomState,
    findings: List<AppFinding>,
    selectedFindingId: Uuid?,
    modifier: Modifier = Modifier,
) {
    val displayRect = zoomableState.zoomable.contentDisplayRectF
    if (displayRect.isEmpty) return

    val classicVisuals = FindingType.Classic().visuals()
    val unvisitedVisuals = FindingType.Unvisited.visuals()
    val noteVisuals = FindingType.Note.visuals()

    val classicPainter = painterResource(classicVisuals.icon)
    val unvisitedPainter = painterResource(unvisitedVisuals.icon)
    val notePainter = painterResource(noteVisuals.icon)

    val selectedPinColor = MaterialTheme.colorScheme.primary

    val normalIconSize = with(LocalDensity.current) { 22.dp.toPx() }
    val selectedScale = 30f / 22f

    Canvas(modifier = modifier.fillMaxSize()) {
        // Draw non-selected pins first, then selected pin on top
        findings.forEach { finding ->
            if (finding.id == selectedFindingId) return@forEach
            finding.coordinates.forEach { coord ->
                val drawPoint =
                    Offset(
                        x = displayRect.left + coord.x * displayRect.width,
                        y = displayRect.top + coord.y * displayRect.height,
                    )

                val (painter, color) =
                    when (finding.type) {
                        is FindingType.Classic -> classicPainter to classicVisuals.pinColor
                        is FindingType.Unvisited -> unvisitedPainter to unvisitedVisuals.pinColor
                        is FindingType.Note -> notePainter to noteVisuals.pinColor
                    }

                drawFindingPin(
                    center = drawPoint,
                    painter = painter,
                    iconDrawSize = normalIconSize,
                    tint = color,
                )
            }
        }

        // Draw selected pins last so it appears on top
        if (selectedFindingId != null) {
            findings
                .find { it.id == selectedFindingId }
                ?.let { finding ->
                    finding.coordinates.forEach { coord ->
                        val drawPoint =
                            Offset(
                                x = displayRect.left + coord.x * displayRect.width,
                                y = displayRect.top + coord.y * displayRect.height,
                            )

                        val (painter, _) =
                            when (finding.type) {
                                is FindingType.Classic ->
                                    classicPainter to classicVisuals.pinColor

                                is FindingType.Unvisited ->
                                    unvisitedPainter to unvisitedVisuals.pinColor

                                is FindingType.Note ->
                                    notePainter to noteVisuals.pinColor
                            }

                        drawFindingPin(
                            center = drawPoint,
                            painter = painter,
                            iconDrawSize = normalIconSize,
                            pinScale = selectedScale,
                            tint = selectedPinColor,
                        )
                    }
                }
        }
    }
}

private fun DrawScope.drawFindingPin(
    center: Offset,
    painter: Painter,
    iconDrawSize: Float,
    tint: Color,
    pinScale: Float = 1f,
) {
    val scaledIconSize = iconDrawSize * pinScale
    val backgroundCircleRadius = scaledIconSize * BACKGROUND_CIRCLE_SCALE / 2
    val haloRadius = backgroundCircleRadius + 1.5.dp.toPx()

    // Draw white halo (outermost layer)
    drawCircle(
        color = Color.White,
        radius = haloRadius,
        center = center,
    )

    // Draw colored ring
    drawCircle(
        color = tint,
        radius = backgroundCircleRadius,
        center = center,
    )

    // Draw white icon on top — always pass the same iconDrawSize to avoid Painter
    // internal state mutation; use canvas scale for size differences instead
    withTransform({
        translate(left = center.x, top = center.y)
        scale(pinScale, pinScale, Offset.Zero)
        translate(left = -iconDrawSize / 2, top = -iconDrawSize / 2)
    }) {
        with(painter) {
            draw(
                size = Size(iconDrawSize, iconDrawSize),
                colorFilter = ColorFilter.tint(Color.White),
            )
        }
    }
}

private fun findTappedFinding(
    tapOffset: Offset,
    displayRect: Rect,
    findings: List<AppFinding>,
    touchTargetRadiusPx: Float,
): Uuid? {
    if (displayRect.isEmpty) return null

    var closestId: Uuid? = null
    var closestDistance = Float.MAX_VALUE

    for (finding in findings) {
        for (coord in finding.coordinates) {
            val pinCenter =
                Offset(
                    x = displayRect.left + coord.x * displayRect.width,
                    y = displayRect.top + coord.y * displayRect.height,
                )
            val dx = tapOffset.x - pinCenter.x
            val dy = tapOffset.y - pinCenter.y
            val distance = sqrt(dx * dx + dy * dy)
            if (distance <= touchTargetRadiusPx && distance < closestDistance) {
                closestDistance = distance
                closestId = finding.id
            }
        }
    }

    return closestId
}
