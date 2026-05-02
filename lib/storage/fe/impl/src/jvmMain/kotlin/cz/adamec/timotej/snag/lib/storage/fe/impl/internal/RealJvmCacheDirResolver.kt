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
import cz.adamec.timotej.snag.lib.storage.fe.api.JvmCacheDirResolver

internal class RealJvmCacheDirResolver(
    private val osName: String = System.getProperty("os.name").orEmpty(),
    private val userHome: String = System.getProperty("user.home").orEmpty(),
    private val localAppData: String? = System.getenv("LOCALAPPDATA"),
    private val xdgCacheHome: String? = System.getenv("XDG_CACHE_HOME"),
    private val appId: String = JVM_APP_NAME,
) : JvmCacheDirResolver {
    override operator fun invoke(): String =
        when {
            osName.contains(other = "mac", ignoreCase = true) ||
                osName.contains(other = "darwin", ignoreCase = true) ->
                "$userHome/Library/Caches/$appId"
            osName.contains(other = "win", ignoreCase = true) ->
                "${localAppData?.takeIf { it.isNotBlank() } ?: "$userHome/AppData/Local"}/$appId/Cache"
            else ->
                "${xdgCacheHome?.takeIf { it.isNotBlank() } ?: "$userHome/.cache"}/$appId"
        }
}
