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
import cz.adamec.timotej.snag.lib.core.Initializer
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

internal class DatabaseInitializer(
    private val databaseDriver: SqlDriver,
    private val defaultDispatcher: CoroutineDispatcher,
) : Initializer {
    override suspend fun init() = withContext(defaultDispatcher) {
        SnagDatabase.Schema.awaitCreate(databaseDriver)
    }
}
