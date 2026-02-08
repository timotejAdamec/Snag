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

package cz.adamec.timotej.snag.feat.shared.database.be.di

import cz.adamec.timotej.snag.feat.shared.database.be.FindingCoordinatesTable
import cz.adamec.timotej.snag.feat.shared.database.be.FindingsTable
import cz.adamec.timotej.snag.feat.shared.database.be.ProjectsTable
import cz.adamec.timotej.snag.feat.shared.database.be.StructuresTable
import cz.adamec.timotej.snag.feat.shared.database.be.internal.DatabaseFactory
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.koin.dsl.module

val sharedDatabaseModule =
    module {
        single<Database> { DatabaseFactory.create() }
        single(createdAtStart = true) {
            transaction(get<Database>()) {
                SchemaUtils.create(
                    ProjectsTable,
                    StructuresTable,
                    FindingsTable,
                    FindingCoordinatesTable,
                )
            }
        }
    }
