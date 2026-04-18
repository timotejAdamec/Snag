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

package cz.adamec.timotej.snag.lib.database.fe

import app.cash.sqldelight.async.coroutines.awaitCreate
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import cz.adamec.timotej.snag.core.foundation.fe.Initializer
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

internal class SqlDelightDatabaseInitializer(
    private val databaseDriver: SqlDriver,
    private val schema: SqlSchema<QueryResult.AsyncValue<Unit>>,
    private val defaultDispatcher: CoroutineDispatcher,
) : Initializer {
    override val priority: Int get() = DATABASE_INITIALIZER_PRIORITY

    override suspend fun init() =
        withContext(defaultDispatcher) {
            schema.awaitCreate(databaseDriver)
        }

    private companion object {
        const val DATABASE_INITIALIZER_PRIORITY = -100
    }
}
