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

package cz.adamec.timotej.snag.feat.shared.database.fe.di

import app.cash.sqldelight.db.SqlDriver
import cz.adamec.timotej.snag.feat.shared.database.fe.internal.CallDatabaseWithResult
import cz.adamec.timotej.snag.feat.shared.database.fe.SuspendCallDatabaseWithResult
import cz.adamec.timotej.snag.feat.shared.database.fe.db.ProjectBookkeepingQueries
import cz.adamec.timotej.snag.feat.shared.database.fe.db.ProjectEntityQueries
import cz.adamec.timotej.snag.feat.shared.database.fe.db.SnagDatabase
import cz.adamec.timotej.snag.feat.shared.database.fe.internal.DatabaseFactory
import cz.adamec.timotej.snag.feat.shared.database.fe.internal.DriverFactory
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module

val databaseModule =
    module {
        includes(platformModule)

        single {
            get<DriverFactory>().createDriver()
        } bind SqlDriver::class

        single {
            DatabaseFactory(
                databaseDriver = get(),
                applicationScope = get(),
            ).createDatabase()
        } bind SnagDatabase::class

        factoryOf(::SuspendCallDatabaseWithResult)

        factory {
            val snagDatabase = get<SnagDatabase>()
            snagDatabase.projectEntityQueries
        } bind ProjectEntityQueries::class

        factory {
            val snagDatabase = get<SnagDatabase>()
            snagDatabase.projectBookkeepingQueries
        } bind ProjectBookkeepingQueries::class
    }

internal expect val platformModule: Module
