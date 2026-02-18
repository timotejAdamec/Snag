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
import cz.adamec.timotej.snag.feat.findings.business.FindingType
import cz.adamec.timotej.snag.feat.structures.be.model.BackendStructure
import cz.adamec.timotej.snag.reports.be.ports.PdfReportGenerator
import cz.adamec.timotej.snag.reports.be.ports.ProjectReportData
import org.openpdf.text.Document
import org.openpdf.text.Element
import org.openpdf.text.Font
import org.openpdf.text.Image
import org.openpdf.text.PageSize
import org.openpdf.text.Paragraph
import org.openpdf.text.Phrase
import org.openpdf.text.pdf.PdfPCell
import org.openpdf.text.pdf.PdfPTable
import org.openpdf.text.pdf.PdfWriter
import java.awt.Color
import java.io.ByteArrayOutputStream
import java.net.URI
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

internal class OpenPdfReportGenerator : PdfReportGenerator {
    override suspend fun generate(data: ProjectReportData): ByteArray {
        val outputStream = ByteArrayOutputStream()
        val document = Document(PageSize.A4)
        PdfWriter.getInstance(document, outputStream)
        document.open()

        addCoverPage(document, data)
        addInspectionsSection(document, data)
        addStructureSections(document, data)
        addSummaryPage(document, data)

        document.close()
        return outputStream.toByteArray()
    }

    private fun addCoverPage(
        document: Document,
        data: ProjectReportData,
    ) {
        @Suppress("StringShouldBeRawString")
        document.add(Paragraph("\n\n\n"))
        document.add(Paragraph("Project Report", FONT_TITLE))
        document.add(Paragraph("\n"))
        document.add(Paragraph(data.project.project.name, FONT_HEADING))
        document.add(Paragraph("\n"))

        val address = data.project.project.address
        if (address.isNotBlank()) {
            document.add(Paragraph("Address: $address", FONT_NORMAL))
        }

        data.client?.let { client ->
            document.add(Paragraph("\n"))
            document.add(Paragraph("Client Information", FONT_SUBHEADING))
            document.add(Paragraph("Name: ${client.client.name}", FONT_NORMAL))
            client.client.address?.let {
                document.add(Paragraph("Address: $it", FONT_NORMAL))
            }
            client.client.phoneNumber?.let {
                document.add(Paragraph("Phone: $it", FONT_NORMAL))
            }
            client.client.email?.let {
                document.add(Paragraph("Email: $it", FONT_NORMAL))
            }
        }

        document.add(Paragraph("\n"))
        document.add(
            Paragraph(
                "Generated: ${DATE_FORMATTER.format(Instant.now())}",
                FONT_SMALL,
            ),
        )
        document.add(
            Paragraph(
                "Structures: ${data.structures.size} | " +
                    "Findings: ${data.findingsByStructure.values.sumOf { it.size }} | " +
                    "Inspections: ${data.inspections.size}",
                FONT_SMALL,
            ),
        )
    }

    private fun addInspectionsSection(
        document: Document,
        data: ProjectReportData,
    ) {
        if (data.inspections.isEmpty()) return

        document.newPage()
        document.add(Paragraph("Inspections", FONT_HEADING))
        document.add(Paragraph("\n"))

        val table = PdfPTable(TABLE_COLUMNS_INSPECTIONS)
        table.widthPercentage = TABLE_WIDTH_PERCENT
        table.setWidths(INSPECTIONS_TABLE_WIDTHS)

        addHeaderCell(table, "Started")
        addHeaderCell(table, "Ended")
        addHeaderCell(table, "Participants")
        addHeaderCell(table, "Climate")
        addHeaderCell(table, "Note")

        data.inspections.forEach { inspection ->
            val i = inspection.inspection
            table.addCell(createCell(i.startedAt?.let { formatTimestamp(it.value) } ?: "-"))
            table.addCell(createCell(i.endedAt?.let { formatTimestamp(it.value) } ?: "-"))
            table.addCell(createCell(i.participants ?: "-"))
            table.addCell(createCell(i.climate ?: "-"))
            table.addCell(createCell(i.note ?: "-"))
        }

        document.add(table)
    }

    private fun addStructureSections(
        document: Document,
        data: ProjectReportData,
    ) {
        data.structures.forEach { structure ->
            document.newPage()
            addStructureSection(
                document,
                structure,
                data.findingsByStructure[structure] ?: emptyList(),
            )
        }
    }

    private fun addStructureSection(
        document: Document,
        structure: BackendStructure,
        findings: List<BackendFinding>,
    ) {
        document.add(Paragraph(structure.structure.name, FONT_HEADING))
        document.add(Paragraph("\n"))

        addFloorPlan(document, structure)

        if (findings.isEmpty()) {
            document.add(Paragraph("No findings for this structure.", FONT_NORMAL))
            return
        }

        document.add(Paragraph("\n"))
        document.add(Paragraph("Findings (${findings.size})", FONT_SUBHEADING))
        document.add(Paragraph("\n"))

        val table = PdfPTable(TABLE_COLUMNS_FINDINGS)
        table.widthPercentage = TABLE_WIDTH_PERCENT
        table.setWidths(FINDINGS_TABLE_WIDTHS)

        addHeaderCell(table, "#")
        addHeaderCell(table, "Name")
        addHeaderCell(table, "Description")
        addHeaderCell(table, "Type")

        findings.forEachIndexed { index, backendFinding ->
            val f = backendFinding.finding
            table.addCell(createCell("${index + 1}"))
            table.addCell(createCell(f.name))
            table.addCell(createCell(f.description ?: "-"))
            table.addCell(createCell(formatFindingType(f.type)))
        }

        document.add(table)
    }

    private fun addFloorPlan(
        document: Document,
        structure: BackendStructure,
    ) {
        val floorPlanUrl = structure.structure.floorPlanUrl ?: return

        val image =
            try {
                Image.getInstance(URI(floorPlanUrl).toURL())
            } catch (_: Exception) {
                document.add(Paragraph("Floor plan image could not be loaded.", FONT_SMALL))
                return
            }

        val maxWidth = document.pageSize.width - document.leftMargin() - document.rightMargin()
        image.scaleToFit(maxWidth, FLOOR_PLAN_MAX_HEIGHT)
        image.alignment = Element.ALIGN_CENTER
        document.add(image)
    }

    private fun addSummaryPage(
        document: Document,
        data: ProjectReportData,
    ) {
        document.newPage()
        document.add(Paragraph("Summary", FONT_HEADING))
        document.add(Paragraph("\n"))

        val totalFindings = data.findingsByStructure.values.sumOf { it.size }
        val allFindings = data.findingsByStructure.values.flatten()

        document.add(Paragraph("Total Structures: ${data.structures.size}", FONT_NORMAL))
        document.add(Paragraph("Total Findings: $totalFindings", FONT_NORMAL))
        document.add(Paragraph("Total Inspections: ${data.inspections.size}", FONT_NORMAL))
        document.add(Paragraph("\n"))

        document.add(Paragraph("Findings by Type", FONT_SUBHEADING))
        val classicCount = allFindings.count { it.finding.type is FindingType.Classic }
        val noteCount = allFindings.count { it.finding.type is FindingType.Note }
        val unvisitedCount = allFindings.count { it.finding.type is FindingType.Unvisited }
        document.add(Paragraph("Classic: $classicCount", FONT_NORMAL))
        document.add(Paragraph("Note: $noteCount", FONT_NORMAL))
        document.add(Paragraph("Unvisited: $unvisitedCount", FONT_NORMAL))

        if (classicCount > 0) {
            document.add(Paragraph("\n"))
            document.add(Paragraph("Classic Findings by Importance", FONT_SUBHEADING))
            val classicFindings =
                allFindings
                    .map { it.finding.type }
                    .filterIsInstance<FindingType.Classic>()
            val byImportance = classicFindings.groupBy { it.importance }
            byImportance.forEach { (importance, list) ->
                document.add(Paragraph("$importance: ${list.size}", FONT_NORMAL))
            }
        }
    }

    private fun addHeaderCell(
        table: PdfPTable,
        text: String,
    ) {
        val cell = PdfPCell(Phrase(text, FONT_TABLE_HEADER))
        cell.backgroundColor = HEADER_BG_COLOR
        cell.horizontalAlignment = Element.ALIGN_CENTER
        cell.setPadding(CELL_PADDING)
        table.addCell(cell)
    }

    private fun createCell(text: String): PdfPCell {
        val cell = PdfPCell(Phrase(text, FONT_TABLE_BODY))
        cell.setPadding(CELL_PADDING)
        return cell
    }

    private fun formatFindingType(type: FindingType): String =
        when (type) {
            is FindingType.Classic -> "Classic (${type.importance}, ${type.term})"
            is FindingType.Note -> "Note"
            is FindingType.Unvisited -> "Unvisited"
        }

    private fun formatTimestamp(millis: Long): String = DATE_FORMATTER.format(Instant.ofEpochMilli(millis))

    companion object {
        private val FONT_TITLE = Font(Font.HELVETICA, 24f, Font.BOLD)
        private val FONT_HEADING = Font(Font.HELVETICA, 18f, Font.BOLD)
        private val FONT_SUBHEADING = Font(Font.HELVETICA, 14f, Font.BOLD)
        private val FONT_NORMAL = Font(Font.HELVETICA, 11f, Font.NORMAL)
        private val FONT_SMALL = Font(Font.HELVETICA, 9f, Font.NORMAL, Color.GRAY)
        private val FONT_TABLE_HEADER = Font(Font.HELVETICA, 10f, Font.BOLD, Color.WHITE)
        private val FONT_TABLE_BODY = Font(Font.HELVETICA, 10f, Font.NORMAL)
        private val HEADER_BG_COLOR = Color(60, 60, 60)
        private val DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault())

        private val INSPECTIONS_TABLE_WIDTHS = floatArrayOf(2f, 2f, 2f, 2f, 3f)
        private val FINDINGS_TABLE_WIDTHS = floatArrayOf(0.5f, 2f, 3f, 1.5f)
        private const val TABLE_WIDTH_PERCENT = 100f
        private const val TABLE_COLUMNS_INSPECTIONS = 5
        private const val TABLE_COLUMNS_FINDINGS = 4
        private const val CELL_PADDING = 5f
        private const val FLOOR_PLAN_MAX_HEIGHT = 400f
    }
}
