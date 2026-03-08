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

package cz.adamec.timotej.snag.feat.shared.database.be.internal

import cz.adamec.timotej.snag.feat.shared.database.be.allTables
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

internal object TestSchemaInitializer {
    private var initialized = false

    fun ensureCreated(database: Database) {
        if (!initialized) {
            transaction(database) {
                SchemaUtils.create(*allTables)
            }
            initialized = true
        }
    }
}
