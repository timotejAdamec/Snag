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

package cz.adamec.timotej.snag.structures.be.driven.test

import cz.adamec.timotej.snag.feat.structures.business.Structure
import cz.adamec.timotej.snag.structures.be.ports.StructuresLocalDataSource
import kotlin.uuid.Uuid

class FakeStructuresLocalDataSource : StructuresLocalDataSource {
    private val structures = mutableListOf<Structure>()

    @Suppress("MaxLineLength")
    override suspend fun getStructures(projectId: Uuid): List<Structure> = structures.filter { it.projectId == projectId }

    override suspend fun updateStructure(structure: Structure): Structure? {
        structures.removeIf { it.id == structure.id }
        structures.add(structure)
        return null
    }

    fun setStructures(vararg items: Structure) {
        structures.addAll(items)
    }
}
