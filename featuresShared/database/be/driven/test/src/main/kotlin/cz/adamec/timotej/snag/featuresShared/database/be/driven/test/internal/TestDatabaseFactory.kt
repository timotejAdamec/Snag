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

package cz.adamec.timotej.snag.featuresShared.database.be.driven.test.internal

import org.jetbrains.exposed.v1.jdbc.Database

internal object TestDatabaseFactory {
    private val database: Database by lazy {
        Database.connect(
            url = "jdbc:h2:mem:test-shared;DB_CLOSE_DELAY=-1;MODE=MySQL",
            driver = "org.h2.Driver",
        )
    }

    fun create(): Database = database
}
