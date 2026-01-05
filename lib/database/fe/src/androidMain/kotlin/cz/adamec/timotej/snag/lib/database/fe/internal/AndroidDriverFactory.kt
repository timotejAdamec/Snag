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

import android.content.Context
import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver

internal class AndroidDriverFactory(
    private val context: Context,
) : DriverFactory {
    override fun createDriver(): SqlDriver =
        AndroidSqliteDriver(
            schema = DriverFactory.schema.synchronous(),
            context = context,
            name = DriverFactory.NAME,
        )
}
