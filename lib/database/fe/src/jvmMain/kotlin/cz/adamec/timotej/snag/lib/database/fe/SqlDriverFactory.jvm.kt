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

import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import cz.adamec.timotej.snag.lib.storage.fe.api.JvmAppDataDirResolver
import org.koin.core.scope.Scope
import java.io.File

actual fun Scope.createPlatformSqlDriver(
    schema: SqlSchema<QueryResult.AsyncValue<Unit>>,
    name: String,
    appId: String,
): SqlDriver {
    val absolutePath =
        resolveJvmAppDatabasePath(
            appDataDirResolver = get<JvmAppDataDirResolver>(),
            osName = System.getProperty("os.name").orEmpty(),
            userHome = System.getProperty("user.home").orEmpty(),
            localAppData = System.getenv("LOCALAPPDATA"),
            xdgDataHome = System.getenv("XDG_DATA_HOME"),
            appId = appId,
            dbName = name,
        )
    File(absolutePath).parentFile?.mkdirs()
    return JdbcSqliteDriver(
        url = "jdbc:sqlite:$absolutePath",
        schema = schema.synchronous(),
    )
}
