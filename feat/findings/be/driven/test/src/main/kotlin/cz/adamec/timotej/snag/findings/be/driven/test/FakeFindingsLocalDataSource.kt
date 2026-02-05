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

import cz.adamec.timotej.snag.feat.findings.be.model.BackendFinding
import cz.adamec.timotej.snag.findings.be.ports.FindingsLocalDataSource
import kotlin.uuid.Uuid

class FakeFindingsLocalDataSource : FindingsLocalDataSource {
    private val findings = mutableMapOf<Uuid, BackendFinding>()

    override suspend fun getFindings(structureId: Uuid): List<BackendFinding> =
        findings.values.filter { it.finding.structureId == structureId }

    override suspend fun updateFinding(finding: BackendFinding): BackendFinding? {
        findings[finding.finding.id] = finding
        return null
    }

    override suspend fun deleteFinding(id: Uuid) {
        findings.remove(id)
    }

    fun setFinding(finding: BackendFinding) {
        findings[finding.finding.id] = finding
    }

    fun setFindings(findings: List<BackendFinding>) {
        findings.forEach { this.findings[it.finding.id] = it }
    }
}
