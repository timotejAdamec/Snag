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
import cz.adamec.timotej.snag.findings.be.ports.FindingsDb
import cz.adamec.timotej.snag.lib.core.common.Timestamp
import kotlin.uuid.Uuid

class FakeFindingsDb : FindingsDb {
    private val findings = mutableMapOf<Uuid, BackendFinding>()

    override suspend fun getFindings(structureId: Uuid): List<BackendFinding> =
        findings.values.filter { it.finding.structureId == structureId }

    override suspend fun updateFinding(finding: BackendFinding): BackendFinding? {
        val foundFinding = findings[finding.finding.id]
        if (foundFinding != null) {
            val serverTimestamp =
                maxOf(
                    foundFinding.finding.updatedAt,
                    foundFinding.deletedAt ?: Timestamp(0),
                )
            if (serverTimestamp >= finding.finding.updatedAt) {
                return foundFinding
            }
        }

        findings[finding.finding.id] = finding
        return null
    }

    @Suppress("ReturnCount")
    override suspend fun deleteFinding(
        id: Uuid,
        deletedAt: Timestamp,
    ): BackendFinding? {
        val foundFinding =
            findings[id]
                ?: return null
        if (foundFinding.deletedAt != null) return null
        if (foundFinding.finding.updatedAt >= deletedAt) return foundFinding

        findings[id] = foundFinding.copy(deletedAt = deletedAt)
        return null
    }

    override suspend fun getFindingsModifiedSince(
        structureId: Uuid,
        since: Timestamp,
    ): List<BackendFinding> =
        findings.values.filter {
            it.finding.structureId == structureId &&
                (it.finding.updatedAt > since || it.deletedAt?.let { d -> d > since } == true)
        }

    fun setFinding(finding: BackendFinding) {
        findings[finding.finding.id] = finding
    }

    fun setFindings(findings: List<BackendFinding>) {
        findings.forEach { this.findings[it.finding.id] = it }
    }
}
