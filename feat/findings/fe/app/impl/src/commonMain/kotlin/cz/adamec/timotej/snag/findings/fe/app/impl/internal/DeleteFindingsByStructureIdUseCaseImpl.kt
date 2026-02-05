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

package cz.adamec.timotej.snag.findings.fe.app.impl.internal

import cz.adamec.timotej.snag.findings.fe.app.api.DeleteFindingsByStructureIdUseCase
import cz.adamec.timotej.snag.findings.fe.ports.FindingsDb
import kotlin.uuid.Uuid

internal class DeleteFindingsByStructureIdUseCaseImpl(
    private val findingsDb: FindingsDb,
) : DeleteFindingsByStructureIdUseCase {
    override suspend operator fun invoke(structureId: Uuid) {
        findingsDb.deleteFindingsByStructureId(structureId)
    }
}
