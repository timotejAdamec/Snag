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

package cz.adamec.timotej.snag.findings.be.driven.test

import cz.adamec.timotej.snag.feat.findings.business.Finding
import cz.adamec.timotej.snag.findings.be.ports.FindingsLocalDataSource
import kotlin.uuid.Uuid

class FakeFindingsLocalDataSource : FindingsLocalDataSource {
    private val findings = mutableMapOf<Uuid, Finding>()

    override suspend fun getFindings(structureId: Uuid): List<Finding> = findings.values.filter { it.structureId == structureId }

    override suspend fun updateFinding(finding: Finding): Finding? {
        findings[finding.id] = finding
        return null
    }

    override suspend fun deleteFinding(id: Uuid) {
        findings.remove(id)
    }

    fun setFinding(finding: Finding) {
        findings[finding.id] = finding
    }

    fun setFindings(findings: List<Finding>) {
        findings.forEach { this.findings[it.id] = it }
    }
}
