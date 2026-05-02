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

import kotlin.test.Test
import kotlin.test.assertEquals

class RealJvmCacheDirResolverTest {
    @Test
    fun macOsResolvesUnderLibraryCaches() {
        val resolver =
            RealJvmCacheDirResolver(
                osName = "Mac OS X",
                userHome = "/Users/tim",
                localAppData = null,
                xdgCacheHome = null,
                appId = "cz.adamec.timotej.snag",
            )

        assertEquals(
            expected = "/Users/tim/Library/Caches/cz.adamec.timotej.snag",
            actual = resolver(),
        )
    }

    @Test
    fun darwinAliasResolvesUnderLibraryCaches() {
        val resolver =
            RealJvmCacheDirResolver(
                osName = "Darwin",
                userHome = "/Users/tim",
                localAppData = null,
                xdgCacheHome = null,
                appId = "cz.adamec.timotej.snag",
            )

        assertEquals(
            expected = "/Users/tim/Library/Caches/cz.adamec.timotej.snag",
            actual = resolver(),
        )
    }

    @Test
    fun windowsUsesLocalAppDataEnvWhenSet() {
        val resolver =
            RealJvmCacheDirResolver(
                osName = "Windows 11",
                userHome = """C:\Users\Tim""",
                localAppData = """C:\Users\Tim\AppData\Local""",
                xdgCacheHome = null,
                appId = "cz.adamec.timotej.snag",
            )

        assertEquals(
            expected = """C:\Users\Tim\AppData\Local/cz.adamec.timotej.snag/Cache""",
            actual = resolver(),
        )
    }

    @Test
    fun windowsFallsBackToHomeAppDataLocalWhenEnvMissing() {
        val resolver =
            RealJvmCacheDirResolver(
                osName = "Windows 11",
                userHome = """C:\Users\Tim""",
                localAppData = null,
                xdgCacheHome = null,
                appId = "cz.adamec.timotej.snag",
            )

        assertEquals(
            expected = """C:\Users\Tim/AppData/Local/cz.adamec.timotej.snag/Cache""",
            actual = resolver(),
        )
    }

    @Test
    fun windowsBlankLocalAppDataFallsBackToHomeAppDataLocal() {
        val resolver =
            RealJvmCacheDirResolver(
                osName = "Windows 11",
                userHome = """C:\Users\Tim""",
                localAppData = "",
                xdgCacheHome = null,
                appId = "cz.adamec.timotej.snag",
            )

        assertEquals(
            expected = """C:\Users\Tim/AppData/Local/cz.adamec.timotej.snag/Cache""",
            actual = resolver(),
        )
    }

    @Test
    fun linuxUsesXdgCacheHomeWhenSet() {
        val resolver =
            RealJvmCacheDirResolver(
                osName = "Linux",
                userHome = "/home/tim",
                localAppData = null,
                xdgCacheHome = "/home/tim/.custom-cache",
                appId = "cz.adamec.timotej.snag",
            )

        assertEquals(
            expected = "/home/tim/.custom-cache/cz.adamec.timotej.snag",
            actual = resolver(),
        )
    }

    @Test
    fun linuxFallsBackToCacheWhenXdgMissing() {
        val resolver =
            RealJvmCacheDirResolver(
                osName = "Linux",
                userHome = "/home/tim",
                localAppData = null,
                xdgCacheHome = null,
                appId = "cz.adamec.timotej.snag",
            )

        assertEquals(
            expected = "/home/tim/.cache/cz.adamec.timotej.snag",
            actual = resolver(),
        )
    }

    @Test
    fun blankXdgCacheHomeFallsBackToCache() {
        val resolver =
            RealJvmCacheDirResolver(
                osName = "Linux",
                userHome = "/home/tim",
                localAppData = null,
                xdgCacheHome = "",
                appId = "cz.adamec.timotej.snag",
            )

        assertEquals(
            expected = "/home/tim/.cache/cz.adamec.timotej.snag",
            actual = resolver(),
        )
    }

    @Test
    fun unknownOsTreatedAsLinuxLike() {
        val resolver =
            RealJvmCacheDirResolver(
                osName = "FreeBSD",
                userHome = "/home/tim",
                localAppData = null,
                xdgCacheHome = null,
                appId = "cz.adamec.timotej.snag",
            )

        assertEquals(
            expected = "/home/tim/.cache/cz.adamec.timotej.snag",
            actual = resolver(),
        )
    }
}
