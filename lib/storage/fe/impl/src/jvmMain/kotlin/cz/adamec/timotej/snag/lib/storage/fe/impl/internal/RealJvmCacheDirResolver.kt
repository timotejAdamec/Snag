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

import cz.adamec.timotej.snag.lib.storage.fe.api.JvmCacheDirResolver

internal class RealJvmCacheDirResolver : JvmCacheDirResolver {
    override operator fun invoke(
        osName: String,
        userHome: String,
        localAppData: String?,
        xdgCacheHome: String?,
        appId: String,
    ): String =
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
