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

package cz.adamec.timotej.snag.feat.shared.database.fe.impl.di

import app.cash.sqldelight.db.SqlDriver
import cz.adamec.timotej.snag.feat.shared.database.fe.db.ClassicFindingEntityQueries
import cz.adamec.timotej.snag.feat.shared.database.fe.db.ClientEntityQueries
import cz.adamec.timotej.snag.feat.shared.database.fe.db.FindingEntityQueries
import cz.adamec.timotej.snag.feat.shared.database.fe.db.ProjectEntityQueries
import cz.adamec.timotej.snag.feat.shared.database.fe.db.PullSyncTimestampEntityQueries
import cz.adamec.timotej.snag.feat.shared.database.fe.db.SnagDatabase
import cz.adamec.timotej.snag.feat.shared.database.fe.db.StructureEntityQueries
import cz.adamec.timotej.snag.feat.shared.database.fe.db.SyncOperationEntityQueries
import cz.adamec.timotej.snag.feat.shared.database.fe.impl.internal.DatabaseInitializer
import cz.adamec.timotej.snag.feat.shared.database.fe.impl.internal.DriverFactory
import cz.adamec.timotej.snag.lib.core.common.di.getDefaultDispatcher
import cz.adamec.timotej.snag.lib.core.fe.Initializer
import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module

val databaseModule =
    module {
        includes(platformModule)

        single {
            get<DriverFactory>().createDriver()
        } bind SqlDriver::class

        single {
            SnagDatabase(driver = get())
        } bind SnagDatabase::class

        factory {
            val snagDatabase = get<SnagDatabase>()
            snagDatabase.clientEntityQueries
        } bind ClientEntityQueries::class

        factory {
            val snagDatabase = get<SnagDatabase>()
            snagDatabase.projectEntityQueries
        } bind ProjectEntityQueries::class

        factory {
            val snagDatabase = get<SnagDatabase>()
            snagDatabase.structureEntityQueries
        } bind StructureEntityQueries::class

        factory {
            val snagDatabase = get<SnagDatabase>()
            snagDatabase.findingEntityQueries
        } bind FindingEntityQueries::class

        factory {
            val snagDatabase = get<SnagDatabase>()
            snagDatabase.classicFindingEntityQueries
        } bind ClassicFindingEntityQueries::class

        factory {
            val snagDatabase = get<SnagDatabase>()
            snagDatabase.syncOperationEntityQueries
        } bind SyncOperationEntityQueries::class

        factory {
            val snagDatabase = get<SnagDatabase>()
            snagDatabase.pullSyncTimestampEntityQueries
        } bind PullSyncTimestampEntityQueries::class

        single {
            DatabaseInitializer(
                databaseDriver = get(),
                defaultDispatcher = getDefaultDispatcher(),
            )
        } bind Initializer::class
    }

internal expect val platformModule: Module
