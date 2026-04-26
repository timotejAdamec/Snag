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

package cz.adamec.timotej.snag.core.foundation.fe

fun resolveJvmAppDataDir(
    osName: String,
    userHome: String,
    appData: String?,
    xdgDataHome: String?,
    appId: String,
): String {
    val baseDir =
        when {
            osName.contains(other = "mac", ignoreCase = true) ||
                osName.contains(other = "darwin", ignoreCase = true) ->
                "$userHome/Library/Application Support"
            osName.contains(other = "win", ignoreCase = true) ->
                appData?.takeIf { it.isNotBlank() } ?: "$userHome/AppData/Roaming"
            else ->
                xdgDataHome?.takeIf { it.isNotBlank() } ?: "$userHome/.local/share"
        }
    return "$baseDir/$appId"
}
