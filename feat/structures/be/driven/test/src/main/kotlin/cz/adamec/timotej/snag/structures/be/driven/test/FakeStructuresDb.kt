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

import cz.adamec.timotej.snag.feat.structures.be.model.BackendStructure
import cz.adamec.timotej.snag.lib.core.common.Timestamp
import cz.adamec.timotej.snag.structures.be.ports.StructuresDb
import kotlin.uuid.Uuid

class FakeStructuresDb : StructuresDb {
    private val structures = mutableListOf<BackendStructure>()

    override suspend fun getStructures(projectId: Uuid): List<BackendStructure> = structures.filter { it.structure.projectId == projectId }

    @Suppress("ReturnCount")
    override suspend fun deleteStructure(
        id: Uuid,
        deletedAt: Timestamp,
    ): BackendStructure? {
        val foundStructure =
            structures.find { it.structure.id == id }
                ?: return null
        if (foundStructure.deletedAt != null) return null
        if (foundStructure.structure.updatedAt >= deletedAt) return foundStructure

        val index = structures.indexOfFirst { it.structure.id == id }
        structures[index] = foundStructure.copy(deletedAt = deletedAt)
        return null
    }

    override suspend fun saveStructure(backendStructure: BackendStructure): BackendStructure? {
        val foundStructure = structures.find { it.structure.id == backendStructure.structure.id }
        if (foundStructure != null) {
            val serverTimestamp =
                maxOf(
                    foundStructure.structure.updatedAt,
                    foundStructure.deletedAt ?: Timestamp(0),
                )
            if (serverTimestamp >= backendStructure.structure.updatedAt) {
                return foundStructure
            }
        }

        structures.removeIf { it.structure.id == backendStructure.structure.id }
        structures.add(backendStructure)
        return null
    }

    override suspend fun getStructuresModifiedSince(
        projectId: Uuid,
        since: Timestamp,
    ): List<BackendStructure> =
        structures.filter {
            it.structure.projectId == projectId &&
                (it.structure.updatedAt > since || it.deletedAt?.let { d -> d > since } == true)
        }

    fun getStructure(id: Uuid): BackendStructure? = structures.find { it.structure.id == id }

    fun setStructures(vararg items: BackendStructure) {
        structures.addAll(items)
    }
}
