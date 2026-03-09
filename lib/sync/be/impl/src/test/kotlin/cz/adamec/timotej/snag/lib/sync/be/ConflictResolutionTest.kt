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

package cz.adamec.timotej.snag.lib.sync.be

import cz.adamec.timotej.snag.lib.core.common.Timestamp
import cz.adamec.timotej.snag.lib.sync.be.model.Syncable
import cz.adamec.timotej.snag.lib.sync.be.internal.ResolveConflictForDeleteUseCaseImpl
import cz.adamec.timotej.snag.lib.sync.be.internal.ResolveConflictForSaveUseCaseImpl
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class ConflictResolutionTest {
    private val resolveConflictForSave: ResolveConflictForSaveUseCase =
        ResolveConflictForSaveUseCaseImpl()

    private val resolveConflictForDelete: ResolveConflictForDeleteUseCase =
        ResolveConflictForDeleteUseCaseImpl()

    private fun entity(
        updatedAt: Long,
        deletedAt: Long? = null,
    ) = TestSyncable(
        updatedAt = Timestamp(updatedAt),
        deletedAt = deletedAt?.let { Timestamp(it) },
    )

    // region resolveConflictForSave

    @Test
    fun `save proceeds when no existing entity`() {
        val result = resolveConflictForSave(existing = null, incoming = entity(100L))

        assertIs<SaveConflictResult.Proceed>(result)
    }

    @Test
    fun `save proceeds when incoming is newer than existing`() {
        val result = resolveConflictForSave(
            existing = entity(100L),
            incoming = entity(200L),
        )

        assertIs<SaveConflictResult.Proceed>(result)
    }

    @Test
    fun `save rejected when existing is newer than incoming`() {
        val existing = entity(200L)

        val result = resolveConflictForSave(existing = existing, incoming = entity(100L))

        assertIs<SaveConflictResult.Rejected<TestSyncable>>(result)
        assertEquals(existing, result.serverVersion)
    }

    @Test
    fun `save rejected when timestamps are equal`() {
        val existing = entity(100L)

        val result = resolveConflictForSave(existing = existing, incoming = entity(100L))

        assertIs<SaveConflictResult.Rejected<TestSyncable>>(result)
        assertEquals(existing, result.serverVersion)
    }

    @Test
    fun `save rejected when existing is soft-deleted with newer deletedAt`() {
        val existing = entity(updatedAt = 100L, deletedAt = 300L)

        val result = resolveConflictForSave(existing = existing, incoming = entity(200L))

        assertIs<SaveConflictResult.Rejected<TestSyncable>>(result)
        assertEquals(existing, result.serverVersion)
    }

    @Test
    fun `save proceeds when incoming is newer than both updatedAt and deletedAt`() {
        val result = resolveConflictForSave(
            existing = entity(updatedAt = 100L, deletedAt = 200L),
            incoming = entity(300L),
        )

        assertIs<SaveConflictResult.Proceed>(result)
    }

    // endregion

    // region resolveConflictForDelete

    @Test
    fun `delete returns not found when no existing entity`() {
        val result = resolveConflictForDelete<TestSyncable>(
            existing = null,
            deletedAt = Timestamp(100L),
        )

        assertIs<DeleteConflictResult.NotFound>(result)
    }

    @Test
    fun `delete returns already deleted when existing is soft-deleted`() {
        val result = resolveConflictForDelete(
            existing = entity(updatedAt = 100L, deletedAt = 200L),
            deletedAt = Timestamp(300L),
        )

        assertIs<DeleteConflictResult.AlreadyDeleted>(result)
    }

    @Test
    fun `delete proceeds when deletedAt is newer than existing updatedAt`() {
        val result = resolveConflictForDelete(
            existing = entity(100L),
            deletedAt = Timestamp(200L),
        )

        assertIs<DeleteConflictResult.Proceed>(result)
    }

    @Test
    fun `delete rejected when existing updatedAt is newer than deletedAt`() {
        val existing = entity(200L)

        val result = resolveConflictForDelete(existing = existing, deletedAt = Timestamp(100L))

        assertIs<DeleteConflictResult.Rejected<TestSyncable>>(result)
        assertEquals(existing, result.serverVersion)
    }

    @Test
    fun `delete rejected when existing updatedAt equals deletedAt`() {
        val existing = entity(100L)

        val result = resolveConflictForDelete(existing = existing, deletedAt = Timestamp(100L))

        assertIs<DeleteConflictResult.Rejected<TestSyncable>>(result)
        assertEquals(existing, result.serverVersion)
    }

    // endregion

}

private data class TestSyncable(
    override val updatedAt: Timestamp,
    override val deletedAt: Timestamp?,
) : Syncable
