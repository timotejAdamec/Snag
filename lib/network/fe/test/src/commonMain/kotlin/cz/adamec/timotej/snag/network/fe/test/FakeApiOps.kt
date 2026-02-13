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

package cz.adamec.timotej.snag.network.fe.test

import cz.adamec.timotej.snag.lib.core.fe.OnlineDataResult
import kotlin.uuid.Uuid

class FakeApiOps<T, SyncResultType>(private val getId: (T) -> Uuid) {
    val apiItems = mutableMapOf<Uuid, T>()
    var forcedFailure: OnlineDataResult.Failure? = null
    var saveResponseOverride: ((T) -> OnlineDataResult<T?>)? = null
    var modifiedSinceResults: List<SyncResultType> = emptyList()

    fun getAllItems(): OnlineDataResult<List<T>> {
        val failure = forcedFailure
        if (failure != null) return failure
        return OnlineDataResult.Success(apiItems.values.toList())
    }

    fun getAllItems(filter: (T) -> Boolean): OnlineDataResult<List<T>> {
        val failure = forcedFailure
        if (failure != null) return failure
        return OnlineDataResult.Success(apiItems.values.filter(filter))
    }

    fun getItemById(id: Uuid): OnlineDataResult<T> {
        val failure = forcedFailure
        if (failure != null) return failure
        return apiItems[id]?.let { OnlineDataResult.Success(it) }
            ?: OnlineDataResult.Failure.ProgrammerError(Exception("Not found"))
    }

    fun saveItem(item: T): OnlineDataResult<T?> {
        val failure = forcedFailure
        if (failure != null) return failure
        val override = saveResponseOverride
        return if (override != null) {
            override(item)
        } else {
            apiItems[getId(item)] = item
            OnlineDataResult.Success(item)
        }
    }

    fun deleteItemById(id: Uuid): OnlineDataResult<Unit> {
        val failure = forcedFailure
        if (failure != null) return failure
        apiItems.remove(id)
        return OnlineDataResult.Success(Unit)
    }

    fun getModifiedSinceItems(): OnlineDataResult<List<SyncResultType>> {
        val failure = forcedFailure
        if (failure != null) return failure
        return OnlineDataResult.Success(modifiedSinceResults)
    }

    fun setItem(item: T) {
        apiItems[getId(item)] = item
    }

    fun setItems(newItems: List<T>) {
        newItems.forEach { apiItems[getId(it)] = it }
    }
}
