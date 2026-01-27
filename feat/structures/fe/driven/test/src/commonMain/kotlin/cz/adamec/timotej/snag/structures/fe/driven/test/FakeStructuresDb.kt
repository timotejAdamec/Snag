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
import cz.adamec.timotej.snag.lib.core.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.structures.fe.ports.StructuresDb
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlin.uuid.Uuid

class FakeStructuresDb : StructuresDb {
    private val structures = MutableStateFlow<Map<Uuid, Structure>>(emptyMap())
    var forcedFailure: OfflineFirstDataResult.ProgrammerError? = null

    override fun getStructuresFlow(projectId: Uuid): Flow<OfflineFirstDataResult<List<Structure>>> =
        structures.map { map ->
            val failure = forcedFailure
            failure ?: OfflineFirstDataResult.Success(map.values.filter { it.projectId == projectId })
        }

    override suspend fun saveStructures(structures: List<Structure>): OfflineFirstDataResult<Unit> {
        val failure = forcedFailure
        if (failure != null) return failure

        this.structures.update { current ->
            current + structures.associateBy { it.id }
        }
        return OfflineFirstDataResult.Success(Unit)
    }

    fun setStructure(structure: Structure) {
        structures.update { it + (structure.id to structure) }
    }

    fun setStructures(structures: List<Structure>) {
        this.structures.update { current ->
            current + structures.associateBy { it.id }
        }
    }
}
