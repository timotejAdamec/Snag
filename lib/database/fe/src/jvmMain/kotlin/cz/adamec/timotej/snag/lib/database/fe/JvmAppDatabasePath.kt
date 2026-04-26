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

import cz.adamec.timotej.snag.core.foundation.fe.resolveJvmAppDataDir

internal fun resolveJvmAppDatabasePath(
    osName: String,
    userHome: String,
    appData: String?,
    xdgDataHome: String?,
    appId: String,
    dbName: String,
): String {
    val baseDir =
        resolveJvmAppDataDir(
            osName = osName,
            userHome = userHome,
            appData = appData,
            xdgDataHome = xdgDataHome,
            appId = appId,
        )
    return "$baseDir/$dbName"
}
