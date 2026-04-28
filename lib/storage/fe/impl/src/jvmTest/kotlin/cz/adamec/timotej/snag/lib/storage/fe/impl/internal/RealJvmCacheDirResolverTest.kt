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
    private val resolver = RealJvmCacheDirResolver()

    @Test
    fun macOsResolvesUnderLibraryCaches() {
        val dir =
            resolver(
                osName = "Mac OS X",
                userHome = "/Users/tim",
                localAppData = null,
                xdgCacheHome = null,
                appId = "cz.adamec.timotej.snag",
            )

        assertEquals(
            expected = "/Users/tim/Library/Caches/cz.adamec.timotej.snag",
            actual = dir,
        )
    }

    @Test
    fun darwinAliasResolvesUnderLibraryCaches() {
        val dir =
            resolver(
                osName = "Darwin",
                userHome = "/Users/tim",
                localAppData = null,
                xdgCacheHome = null,
                appId = "cz.adamec.timotej.snag",
            )

        assertEquals(
            expected = "/Users/tim/Library/Caches/cz.adamec.timotej.snag",
            actual = dir,
        )
    }

    @Test
    fun windowsUsesLocalAppDataEnvWhenSet() {
        val dir =
            resolver(
                osName = "Windows 11",
                userHome = """C:\Users\Tim""",
                localAppData = """C:\Users\Tim\AppData\Local""",
                xdgCacheHome = null,
                appId = "cz.adamec.timotej.snag",
            )

        assertEquals(
            expected = """C:\Users\Tim\AppData\Local/cz.adamec.timotej.snag/Cache""",
            actual = dir,
        )
    }

    @Test
    fun windowsFallsBackToHomeAppDataLocalWhenEnvMissing() {
        val dir =
            resolver(
                osName = "Windows 11",
                userHome = """C:\Users\Tim""",
                localAppData = null,
                xdgCacheHome = null,
                appId = "cz.adamec.timotej.snag",
            )

        assertEquals(
            expected = """C:\Users\Tim/AppData/Local/cz.adamec.timotej.snag/Cache""",
            actual = dir,
        )
    }

    @Test
    fun windowsBlankLocalAppDataFallsBackToHomeAppDataLocal() {
        val dir =
            resolver(
                osName = "Windows 11",
                userHome = """C:\Users\Tim""",
                localAppData = "",
                xdgCacheHome = null,
                appId = "cz.adamec.timotej.snag",
            )

        assertEquals(
            expected = """C:\Users\Tim/AppData/Local/cz.adamec.timotej.snag/Cache""",
            actual = dir,
        )
    }

    @Test
    fun linuxUsesXdgCacheHomeWhenSet() {
        val dir =
            resolver(
                osName = "Linux",
                userHome = "/home/tim",
                localAppData = null,
                xdgCacheHome = "/home/tim/.custom-cache",
                appId = "cz.adamec.timotej.snag",
            )

        assertEquals(
            expected = "/home/tim/.custom-cache/cz.adamec.timotej.snag",
            actual = dir,
        )
    }

    @Test
    fun linuxFallsBackToCacheWhenXdgMissing() {
        val dir =
            resolver(
                osName = "Linux",
                userHome = "/home/tim",
                localAppData = null,
                xdgCacheHome = null,
                appId = "cz.adamec.timotej.snag",
            )

        assertEquals(
            expected = "/home/tim/.cache/cz.adamec.timotej.snag",
            actual = dir,
        )
    }

    @Test
    fun blankXdgCacheHomeFallsBackToCache() {
        val dir =
            resolver(
                osName = "Linux",
                userHome = "/home/tim",
                localAppData = null,
                xdgCacheHome = "",
                appId = "cz.adamec.timotej.snag",
            )

        assertEquals(
            expected = "/home/tim/.cache/cz.adamec.timotej.snag",
            actual = dir,
        )
    }

    @Test
    fun unknownOsTreatedAsLinuxLike() {
        val dir =
            resolver(
                osName = "FreeBSD",
                userHome = "/home/tim",
                localAppData = null,
                xdgCacheHome = null,
                appId = "cz.adamec.timotej.snag",
            )

        assertEquals(
            expected = "/home/tim/.cache/cz.adamec.timotej.snag",
            actual = dir,
        )
    }
}
