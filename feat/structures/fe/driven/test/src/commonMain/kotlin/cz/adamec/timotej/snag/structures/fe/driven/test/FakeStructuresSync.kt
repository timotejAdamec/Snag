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

package cz.adamec.timotej.snag.structures.fe.driven.test

import cz.adamec.timotej.snag.structures.fe.ports.StructuresSync
import kotlin.uuid.Uuid

class FakeStructuresSync : StructuresSync {
    val savedStructureIds = mutableListOf<Uuid>()

    override suspend fun enqueueStructureSave(structureId: Uuid) {
        savedStructureIds.add(structureId)
    }
}
