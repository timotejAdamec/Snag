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

import cz.adamec.timotej.snag.feat.structures.fe.model.FrontendStructure
import cz.adamec.timotej.snag.lib.core.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.structures.fe.ports.StructuresDb
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlin.uuid.Uuid

class FakeStructuresDb : StructuresDb {
    private val structures = MutableStateFlow<Map<Uuid, FrontendStructure>>(emptyMap())
    var forcedFailure: OfflineFirstDataResult.ProgrammerError? = null

    override fun getStructuresFlow(projectId: Uuid): Flow<OfflineFirstDataResult<List<FrontendStructure>>> =
        structures.map { map ->
            val failure = forcedFailure
            failure ?: OfflineFirstDataResult.Success(map.values.filter { it.structure.projectId == projectId })
        }

    override suspend fun saveStructures(structures: List<FrontendStructure>): OfflineFirstDataResult<Unit> {
        val failure = forcedFailure
        if (failure != null) return failure

        this.structures.update { current ->
            current + structures.associateBy { it.structure.id }
        }
        return OfflineFirstDataResult.Success(Unit)
    }

    override suspend fun saveStructure(structure: FrontendStructure): OfflineFirstDataResult<Unit> {
        val failure = forcedFailure
        if (failure != null) return failure

        structures.update { it + (structure.structure.id to structure) }
        return OfflineFirstDataResult.Success(Unit)
    }

    override suspend fun deleteStructure(id: Uuid): OfflineFirstDataResult<Unit> {
        val failure = forcedFailure
        if (failure != null) return failure

        structures.update { it - id }
        return OfflineFirstDataResult.Success(Unit)
    }

    override fun getStructureFlow(id: Uuid): Flow<OfflineFirstDataResult<FrontendStructure?>> =
        structures.map { map ->
            val failure = forcedFailure
            if (failure != null) {
                failure
            } else {
                OfflineFirstDataResult.Success(map[id])
            }
        }

    override suspend fun getStructureIdsByProjectId(projectId: Uuid): List<Uuid> =
        structures.value.values.filter { it.structure.projectId == projectId }.map { it.structure.id }

    override suspend fun deleteStructuresByProjectId(projectId: Uuid): OfflineFirstDataResult<Unit> {
        val failure = forcedFailure
        if (failure != null) return failure
        structures.update { current -> current.filterValues { it.structure.projectId != projectId } }
        return OfflineFirstDataResult.Success(Unit)
    }

    fun setStructure(structure: FrontendStructure) {
        structures.update { it + (structure.structure.id to structure) }
    }

    fun setStructures(structures: List<FrontendStructure>) {
        this.structures.update { current ->
            current + structures.associateBy { it.structure.id }
        }
    }
}
