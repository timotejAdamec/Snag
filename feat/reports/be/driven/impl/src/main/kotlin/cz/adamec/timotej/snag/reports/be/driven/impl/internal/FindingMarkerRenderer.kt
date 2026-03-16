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

package cz.adamec.timotej.snag.reports.be.driven.impl.internal

import cz.adamec.timotej.snag.feat.findings.be.model.BackendFinding
import cz.adamec.timotej.snag.feat.findings.business.model.FindingType
import cz.adamec.timotej.snag.feat.findings.business.model.RelativeCoordinate
import java.awt.BasicStroke
import java.awt.Color
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO
import java.awt.Font as AwtFont

internal object FindingMarkerRenderer {
    private const val MARKER_RADIUS_DIVISOR = 60
    private const val MARKER_FONT_SCALE = 1.2
    private const val MARKER_HALO_PX = 2
    private const val MARKER_BORDER_WIDTH = 2f
    private const val PIN_COLOR_CLASSIC_R = 186
    private const val PIN_COLOR_CLASSIC_G = 26
    private const val PIN_COLOR_CLASSIC_B = 26
    private const val PIN_COLOR_NOTE_R = 98
    private const val PIN_COLOR_NOTE_G = 91
    private const val PIN_COLOR_NOTE_B = 113
    private const val PIN_COLOR_UNVISITED_R = 121
    private const val PIN_COLOR_UNVISITED_G = 116
    private const val PIN_COLOR_UNVISITED_B = 126
    private val PIN_COLOR_CLASSIC = Color(PIN_COLOR_CLASSIC_R, PIN_COLOR_CLASSIC_G, PIN_COLOR_CLASSIC_B)
    private val PIN_COLOR_NOTE = Color(PIN_COLOR_NOTE_R, PIN_COLOR_NOTE_G, PIN_COLOR_NOTE_B)
    private val PIN_COLOR_UNVISITED = Color(PIN_COLOR_UNVISITED_R, PIN_COLOR_UNVISITED_G, PIN_COLOR_UNVISITED_B)

    fun drawMarkersOnImage(
        imageBytes: ByteArray,
        findings: List<BackendFinding>,
    ): ByteArray {
        val bufferedImage =
            ImageIO.read(ByteArrayInputStream(imageBytes))
                ?: return imageBytes
        val canvas =
            BufferedImage(
                bufferedImage.width,
                bufferedImage.height,
                BufferedImage.TYPE_INT_ARGB,
            )
        val g = canvas.createGraphics()
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
        g.drawImage(bufferedImage, 0, 0, null)

        val markerRadius = maxOf(canvas.width, canvas.height) / MARKER_RADIUS_DIVISOR
        val fontSize = (markerRadius * MARKER_FONT_SCALE).toFloat()
        g.font = g.font.deriveFont(AwtFont.BOLD, fontSize)

        findings.forEachIndexed { index, backendFinding ->
            val label = "${index + 1}"
            val color = findingTypeColor(backendFinding.type)
            backendFinding.coordinates.forEach { coord ->
                drawMarker(g, coord, canvas.width, canvas.height, markerRadius, label, color)
            }
        }

        g.dispose()

        val output = ByteArrayOutputStream()
        ImageIO.write(canvas, "png", output)
        return output.toByteArray()
    }

    private fun drawMarker(
        g: Graphics2D,
        coord: RelativeCoordinate,
        imageWidth: Int,
        imageHeight: Int,
        radius: Int,
        label: String,
        color: Color,
    ) {
        val cx = (coord.x * imageWidth).toInt()
        val cy = (coord.y * imageHeight).toInt()

        // White halo
        g.color = Color.WHITE
        g.fillOval(cx - radius - MARKER_HALO_PX, cy - radius - MARKER_HALO_PX, (radius + MARKER_HALO_PX) * 2, (radius + MARKER_HALO_PX) * 2)

        // Colored circle
        g.color = color
        g.fillOval(cx - radius, cy - radius, radius * 2, radius * 2)

        // White border
        g.color = Color.WHITE
        g.stroke = BasicStroke(MARKER_BORDER_WIDTH)
        g.drawOval(cx - radius, cy - radius, radius * 2, radius * 2)

        // Number label
        g.color = Color.WHITE
        val fm = g.fontMetrics
        val textWidth = fm.stringWidth(label)
        val textHeight = fm.ascent
        g.drawString(label, cx - textWidth / 2, cy + textHeight / 2 - fm.descent / 2)
    }

    private fun findingTypeColor(type: FindingType): Color =
        when (type) {
            is FindingType.Classic -> PIN_COLOR_CLASSIC
            is FindingType.Note -> PIN_COLOR_NOTE
            is FindingType.Unvisited -> PIN_COLOR_UNVISITED
        }
}
