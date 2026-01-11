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

package cz.adamec.timotej.snag.feat.shared.database.fe.internal

import cz.adamec.timotej.snag.feat.shared.database.fe.DatabaseResult
import cz.adamec.timotej.snag.feat.shared.database.fe.catchAsDatabaseResult
import cz.adamec.timotej.snag.lib.core.runCatchingCancellable

internal class CallDatabaseWithResult {
    suspend operator fun <T> invoke(block: suspend () -> T): DatabaseResult<T> =
        runCatchingCancellable {
            block()
        }.catchAsDatabaseResult()
}
