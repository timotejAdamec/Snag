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

package cz.adamec.timotej.snag.featShared.database.be.driven.test.internal

import cz.adamec.timotej.snag.featShared.database.be.driven.api.allTables
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.deleteAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

internal object TestDatabaseCleaner {
    fun cleanAll(database: Database) {
        transaction(database) {
            allTables.reversed().forEach { it.deleteAll() }
        }
    }
}
