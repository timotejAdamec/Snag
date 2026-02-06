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

package cz.adamec.timotej.snag.findings.fe.app.test

import cz.adamec.timotej.snag.findings.fe.app.api.DeleteLocalFindingsByStructureIdUseCase
import kotlin.uuid.Uuid

class FakeDeleteLocalFindingsByStructureIdUseCase : DeleteLocalFindingsByStructureIdUseCase {
    val deletedStructureIds = mutableListOf<Uuid>()

    override suspend fun invoke(structureId: Uuid) {
        deletedStructureIds.add(structureId)
    }

    fun reset() {
        deletedStructureIds.clear()
    }
}
