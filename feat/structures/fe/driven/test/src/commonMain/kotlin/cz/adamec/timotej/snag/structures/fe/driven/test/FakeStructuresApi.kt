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

import cz.adamec.timotej.snag.feat.structures.business.Structure
import cz.adamec.timotej.snag.lib.core.fe.OnlineDataResult
import cz.adamec.timotej.snag.structures.fe.ports.StructuresApi
import kotlin.uuid.Uuid

class FakeStructuresApi : StructuresApi {
    private val structures = mutableMapOf<Uuid, Structure>()
    var forcedFailure: OnlineDataResult.Failure? = null

    override suspend fun getStructures(projectId: Uuid): OnlineDataResult<List<Structure>> {
        val failure = forcedFailure
        if (failure != null) return failure
        return OnlineDataResult.Success(structures.values.filter { it.projectId == projectId })
    }

    fun setStructure(structure: Structure) {
        structures[structure.id] = structure
    }

    fun setStructures(structures: List<Structure>) {
        structures.forEach { this.structures[it.id] = it }
    }
}
