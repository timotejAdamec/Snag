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

package cz.adamec.timotej.snag.lib.storage.fe.impl.internal

import cz.adamec.timotej.snag.lib.storage.fe.api.JVM_APP_NAME
import cz.adamec.timotej.snag.lib.storage.fe.api.JvmAppDataDirResolver

internal class RealJvmAppDataDirResolver(
    private val osName: String = System.getProperty("os.name").orEmpty(),
    private val userHome: String = System.getProperty("user.home").orEmpty(),
    private val localAppData: String? = System.getenv("LOCALAPPDATA"),
    private val xdgDataHome: String? = System.getenv("XDG_DATA_HOME"),
    private val appId: String = JVM_APP_NAME,
) : JvmAppDataDirResolver {
    override operator fun invoke(): String {
        val baseDir =
            when {
                osName.contains(other = "mac", ignoreCase = true) ||
                    osName.contains(other = "darwin", ignoreCase = true) ->
                    "$userHome/Library/Application Support"
                osName.contains(other = "win", ignoreCase = true) ->
                    localAppData?.takeIf { it.isNotBlank() } ?: "$userHome/AppData/Local"
                else ->
                    xdgDataHome?.takeIf { it.isNotBlank() } ?: "$userHome/.local/share"
            }
        return "$baseDir/$appId"
    }
}
