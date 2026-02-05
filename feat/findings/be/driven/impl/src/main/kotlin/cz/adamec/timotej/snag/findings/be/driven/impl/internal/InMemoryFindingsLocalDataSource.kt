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

package cz.adamec.timotej.snag.findings.be.driven.impl.internal

import cz.adamec.timotej.snag.feat.findings.be.model.BackendFinding
import cz.adamec.timotej.snag.feat.findings.business.Finding
import cz.adamec.timotej.snag.feat.findings.business.RelativeCoordinate
import cz.adamec.timotej.snag.findings.be.ports.FindingsLocalDataSource
import kotlin.uuid.Uuid

internal class InMemoryFindingsLocalDataSource : FindingsLocalDataSource {
    private val findings =
        mutableListOf(
            BackendFinding(
                finding = Finding(
                    id = Uuid.parse(FINDING_1),
                    structureId = Uuid.parse(STRUCTURE_1),
                    name = "Cracked wall tile",
                    description = "Visible crack on wall tile near entrance.",
                    coordinates = listOf(RelativeCoordinate(x = 0.25f, y = 0.40f)),
                ),
            ),
            BackendFinding(
                finding = Finding(
                    id = Uuid.parse(FINDING_2),
                    structureId = Uuid.parse(STRUCTURE_1),
                    name = "Missing paint patch",
                    description = "Unpainted area on the ceiling in hallway.",
                    coordinates = listOf(RelativeCoordinate(x = 0.60f, y = 0.15f)),
                ),
            ),
            BackendFinding(
                finding = Finding(
                    id = Uuid.parse(FINDING_3),
                    structureId = Uuid.parse(STRUCTURE_2),
                    name = "Loose handrail",
                    description = null,
                    coordinates =
                        listOf(
                            RelativeCoordinate(x = 0.80f, y = 0.55f),
                            RelativeCoordinate(x = 0.82f, y = 0.60f),
                        ),
                ),
            ),
        )

    override suspend fun getFindings(structureId: Uuid): List<BackendFinding> =
        findings.filter { it.finding.structureId == structureId }

    override suspend fun deleteFinding(id: Uuid) {
        findings.removeIf { it.finding.id == id }
    }

    override suspend fun updateFinding(finding: BackendFinding): BackendFinding? {
        findings.removeIf { it.finding.id == finding.finding.id }
        findings.add(finding)
        return null
    }

    private companion object {
        private const val STRUCTURE_1 = "00000000-0000-0000-0001-000000000001"
        private const val STRUCTURE_2 = "00000000-0000-0000-0001-000000000002"
        private const val FINDING_1 = "00000000-0000-0000-0002-000000000001"
        private const val FINDING_2 = "00000000-0000-0000-0002-000000000002"
        private const val FINDING_3 = "00000000-0000-0000-0002-000000000003"
    }
}
