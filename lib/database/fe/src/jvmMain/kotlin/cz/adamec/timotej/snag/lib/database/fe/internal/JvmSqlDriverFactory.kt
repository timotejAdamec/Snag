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

package cz.adamec.timotej.snag.lib.database.fe.internal

import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import cz.adamec.timotej.snag.lib.database.fe.SqlDriverFactory
import cz.adamec.timotej.snag.lib.storage.fe.api.JvmAppDataDirResolver
import java.io.File

internal class JvmSqlDriverFactory(
    private val appDataDirResolver: JvmAppDataDirResolver,
) : SqlDriverFactory {
    override fun create(
        schema: SqlSchema<QueryResult.AsyncValue<Unit>>,
        name: String,
    ): SqlDriver {
        val absolutePath = "${appDataDirResolver()}/$name"
        File(absolutePath).parentFile?.mkdirs()
        return JdbcSqliteDriver(
            url = "jdbc:sqlite:$absolutePath",
            schema = schema.synchronous(),
        )
    }
}
