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

import FrontendStructure
import cz.adamec.timotej.snag.feat.structures.business.Structure
import cz.adamec.timotej.snag.lib.core.fe.OnlineDataResult
import cz.adamec.timotej.snag.structures.fe.ports.StructuresApi
import kotlin.uuid.Uuid

class FakeStructuresApi : StructuresApi {
    private val structures = mutableMapOf<Uuid, FrontendStructure>()
    var forcedFailure: OnlineDataResult.Failure? = null
    var saveStructureResponseOverride: ((FrontendStructure) -> OnlineDataResult<FrontendStructure?>)? = null

    override suspend fun getStructures(projectId: Uuid): OnlineDataResult<List<FrontendStructure>> {
        val failure = forcedFailure
        if (failure != null) return failure
        return OnlineDataResult.Success(structures.values.filter { it.structure.projectId == projectId })
    }

    override suspend fun deleteStructure(id: Uuid): OnlineDataResult<Unit> {
        val failure = forcedFailure
        if (failure != null) return failure
        structures.remove(id)
        return OnlineDataResult.Success(Unit)
    }

    override suspend fun saveStructure(frontendStructure: FrontendStructure): OnlineDataResult<FrontendStructure?> {
        val failure = forcedFailure
        if (failure != null) return failure
        val override = saveStructureResponseOverride
        if (override != null) return override(frontendStructure)
        structures[frontendStructure.structure.id] = frontendStructure
        return OnlineDataResult.Success(frontendStructure)
    }

    fun setStructure(structure: FrontendStructure) {
        structures[structure.structure.id] = structure
    }

    fun setStructures(structures: List<FrontendStructure>) {
        structures.forEach { this.structures[it.structure.id] = it }
    }
}
