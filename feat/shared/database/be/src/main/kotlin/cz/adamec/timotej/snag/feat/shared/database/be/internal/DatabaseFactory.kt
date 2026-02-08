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

import org.jetbrains.exposed.v1.jdbc.Database

internal object DatabaseFactory {
    fun create(): Database =
        Database.connect(
            url = "jdbc:h2:mem:snag;DB_CLOSE_DELAY=-1",
            driver = "org.h2.Driver",
        )
}
