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

package cz.adamec.timotej.snag.reports.be.app.impl.internal

import cz.adamec.timotej.snag.clients.be.app.api.GetClientUseCase
import cz.adamec.timotej.snag.feat.inspections.be.app.api.GetInspectionsUseCase
import cz.adamec.timotej.snag.findings.be.app.api.GetFindingsUseCase
import cz.adamec.timotej.snag.projects.be.app.api.GetProjectUseCase
import cz.adamec.timotej.snag.reports.be.app.api.GenerateProjectReportUseCase
import cz.adamec.timotej.snag.reports.be.app.impl.internal.LH.logger
import cz.adamec.timotej.snag.reports.be.model.BackendReport
import cz.adamec.timotej.snag.reports.be.ports.PdfReportGenerator
import cz.adamec.timotej.snag.reports.be.ports.ProjectReportData
import cz.adamec.timotej.snag.reports.business.Report
import cz.adamec.timotej.snag.structures.be.app.api.GetStructuresUseCase
import kotlin.uuid.Uuid

internal class GenerateProjectReportUseCaseImpl(
    private val getProjectUseCase: GetProjectUseCase,
    private val getClientUseCase: GetClientUseCase,
    private val getStructuresUseCase: GetStructuresUseCase,
    private val getFindingsUseCase: GetFindingsUseCase,
    private val getInspectionsUseCase: GetInspectionsUseCase,
    private val pdfReportGenerator: PdfReportGenerator,
) : GenerateProjectReportUseCase {
    override suspend operator fun invoke(projectId: Uuid): BackendReport? {
        logger.debug("Generating report for project {}.", projectId)

        val backendProject = getProjectUseCase(projectId) ?: return null
        val backendClient = backendProject.project.clientId?.let { getClientUseCase(it) }
        val structures =
            getStructuresUseCase(projectId).filter { it.deletedAt == null }
        val findingsByStructure =
            structures.associateWith { structure ->
                getFindingsUseCase(structure.structure.id).filter { it.deletedAt == null }
            }
        val inspections =
            getInspectionsUseCase(projectId).filter { it.deletedAt == null }
        val reportData =
            ProjectReportData(
                project = backendProject,
                client = backendClient,
                structures = structures,
                findingsByStructure = findingsByStructure,
                inspections = inspections,
            )

        val bytes = pdfReportGenerator.generate(reportData)
        logger.debug("Generated report for project {} ({} bytes).", projectId, bytes.size)
        val fileName = "${backendProject.project.name} - Report.pdf"
        return BackendReport(
            report =
                Report(
                    projectId = projectId,
                    fileName = fileName,
                    bytes = bytes,
                ),
        )
    }
}
