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

package cz.adamec.timotej.snag.reports.be.ports

import cz.adamec.timotej.snag.clients.be.model.BackendClient
import cz.adamec.timotej.snag.feat.findings.be.model.BackendFinding
import cz.adamec.timotej.snag.feat.inspections.be.model.BackendInspection
import cz.adamec.timotej.snag.feat.structures.be.model.BackendStructure
import cz.adamec.timotej.snag.projects.be.model.BackendProject

data class ProjectReportData(
    val project: BackendProject,
    val client: BackendClient?,
    val structures: List<BackendStructure>,
    val findingsByStructure: Map<BackendStructure, List<BackendFinding>>,
    val inspections: List<BackendInspection>,
)
