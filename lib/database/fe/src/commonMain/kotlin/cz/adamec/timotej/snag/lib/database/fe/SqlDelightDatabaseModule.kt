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

import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import cz.adamec.timotej.snag.lib.core.common.di.getDefaultDispatcher
import cz.adamec.timotej.snag.lib.core.fe.Initializer
import org.koin.core.qualifier.Qualifier
import org.koin.dsl.bind
import org.koin.dsl.module

fun sqlDelightDatabaseModule(
    schema: SqlSchema<QueryResult.AsyncValue<Unit>>,
    name: String,
    qualifier: Qualifier,
) = module {
    single(qualifier) {
        createPlatformSqlDriver(schema, name)
    } bind SqlDriver::class

    single<Initializer> {
        SqlDelightDatabaseInitializer(
            databaseDriver = get(qualifier),
            schema = schema,
            defaultDispatcher = getDefaultDispatcher(),
        )
    }
}
