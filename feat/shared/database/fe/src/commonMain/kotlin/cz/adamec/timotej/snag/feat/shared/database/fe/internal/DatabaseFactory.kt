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

package cz.adamec.timotej.snag.feat.shared.database.fe.internal

import app.cash.sqldelight.async.coroutines.awaitCreate
import app.cash.sqldelight.db.SqlDriver
import cz.adamec.timotej.snag.feat.shared.database.fe.db.SnagDatabase
import cz.adamec.timotej.snag.lib.core.ApplicationScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal class DatabaseFactory(
    private val databaseDriver: SqlDriver,
    private val applicationScope: ApplicationScope,
) {
    var isInitialized: Boolean = false

    fun createDatabase(): SnagDatabase {
        check(!isInitialized) { "Database is already initialized!" }
        isInitialized = true

        TODO("Move web init into separate method and wait for it to complete before querying the database.")

        return SnagDatabase(databaseDriver).also {
            applicationScope.launch {
                withContext(Dispatchers.Default) {
                    SnagDatabase.Schema.awaitCreate(databaseDriver)
                }
            }
        }
    }
}
