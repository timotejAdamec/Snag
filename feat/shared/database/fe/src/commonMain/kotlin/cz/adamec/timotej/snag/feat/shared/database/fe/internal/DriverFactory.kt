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

import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import cz.adamec.timotej.snag.feat.shared.database.fe.db.SnagDatabase

internal interface DriverFactory {
    fun createDriver(): SqlDriver

    companion object {
        val schema: SqlSchema<QueryResult.AsyncValue<Unit>> = SnagDatabase.Schema
        const val NAME: String = "snag.db"
    }
}
