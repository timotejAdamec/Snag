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

package cz.adamec.timotej.snag.lib.database.fe.test

import cz.adamec.timotej.snag.lib.core.fe.OfflineFirstDataResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlin.uuid.Uuid

class FakeDbOps<T>(private val getId: (T) -> Uuid) {
    val items = MutableStateFlow<Map<Uuid, T>>(emptyMap())
    var forcedFailure: OfflineFirstDataResult.ProgrammerError? = null

    fun allItemsFlow(): Flow<OfflineFirstDataResult<List<T>>> =
        items.map { OfflineFirstDataResult.Success(it.values.toList()) }

    fun allItemsFlow(filter: (T) -> Boolean): Flow<OfflineFirstDataResult<List<T>>> =
        items.map { map ->
            val failure = forcedFailure
            failure ?: OfflineFirstDataResult.Success(map.values.filter(filter))
        }

    fun itemByIdFlow(id: Uuid): Flow<OfflineFirstDataResult<T?>> =
        items.map { map ->
            val failure = forcedFailure
            if (failure != null) {
                failure
            } else {
                OfflineFirstDataResult.Success(map[id])
            }
        }

    suspend fun saveOneItem(item: T): OfflineFirstDataResult<Unit> {
        val failure = forcedFailure
        if (failure != null) return failure

        items.update { it + (getId(item) to item) }
        return OfflineFirstDataResult.Success(Unit)
    }

    suspend fun saveManyItems(newItems: List<T>): OfflineFirstDataResult<Unit> {
        val failure = forcedFailure
        if (failure != null) return failure

        items.update { current ->
            current + newItems.associateBy { getId(it) }
        }
        return OfflineFirstDataResult.Success(Unit)
    }

    suspend fun deleteItem(id: Uuid): OfflineFirstDataResult<Unit> {
        val failure = forcedFailure
        if (failure != null) return failure

        items.update { it - id }
        return OfflineFirstDataResult.Success(Unit)
    }

    suspend fun deleteItemsWhere(keep: (T) -> Boolean): OfflineFirstDataResult<Unit> {
        val failure = forcedFailure
        if (failure != null) return failure

        items.update { current -> current.filterValues(keep) }
        return OfflineFirstDataResult.Success(Unit)
    }

    fun setItem(item: T) {
        items.update { it + (getId(item) to item) }
    }

    fun setItems(newItems: List<T>) {
        items.update { current ->
            current + newItems.associateBy { getId(it) }
        }
    }
}
