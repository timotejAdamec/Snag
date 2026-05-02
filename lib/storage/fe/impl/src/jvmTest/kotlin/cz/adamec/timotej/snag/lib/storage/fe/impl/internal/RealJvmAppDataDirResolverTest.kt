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

class RealJvmAppDataDirResolverTest {
    @Test
    fun macOsResolvesUnderApplicationSupport() {
        val resolver =
            RealJvmAppDataDirResolver(
                osName = "Mac OS X",
                userHome = "/Users/tim",
                localAppData = null,
                xdgDataHome = null,
                appId = "cz.adamec.timotej.snag",
            )

        assertEquals(
            expected = "/Users/tim/Library/Application Support/cz.adamec.timotej.snag",
            actual = resolver(),
        )
    }

    @Test
    fun darwinAliasResolvesUnderApplicationSupport() {
        val resolver =
            RealJvmAppDataDirResolver(
                osName = "Darwin",
                userHome = "/Users/tim",
                localAppData = null,
                xdgDataHome = null,
                appId = "cz.adamec.timotej.snag",
            )

        assertEquals(
            expected = "/Users/tim/Library/Application Support/cz.adamec.timotej.snag",
            actual = resolver(),
        )
    }

    @Test
    fun windowsUsesLocalAppDataEnvWhenSet() {
        val resolver =
            RealJvmAppDataDirResolver(
                osName = "Windows 11",
                userHome = """C:\Users\Tim""",
                localAppData = """C:\Users\Tim\AppData\Local""",
                xdgDataHome = null,
                appId = "cz.adamec.timotej.snag",
            )

        assertEquals(
            expected = """C:\Users\Tim\AppData\Local/cz.adamec.timotej.snag""",
            actual = resolver(),
        )
    }

    @Test
    fun windowsFallsBackToHomeAppDataLocalWhenEnvMissing() {
        val resolver =
            RealJvmAppDataDirResolver(
                osName = "Windows 11",
                userHome = """C:\Users\Tim""",
                localAppData = null,
                xdgDataHome = null,
                appId = "cz.adamec.timotej.snag",
            )

        assertEquals(
            expected = """C:\Users\Tim/AppData/Local/cz.adamec.timotej.snag""",
            actual = resolver(),
        )
    }

    @Test
    fun windowsBlankLocalAppDataFallsBackToHomeAppDataLocal() {
        val resolver =
            RealJvmAppDataDirResolver(
                osName = "Windows 11",
                userHome = """C:\Users\Tim""",
                localAppData = "",
                xdgDataHome = null,
                appId = "cz.adamec.timotej.snag",
            )

        assertEquals(
            expected = """C:\Users\Tim/AppData/Local/cz.adamec.timotej.snag""",
            actual = resolver(),
        )
    }

    @Test
    fun linuxUsesXdgDataHomeWhenSet() {
        val resolver =
            RealJvmAppDataDirResolver(
                osName = "Linux",
                userHome = "/home/tim",
                localAppData = null,
                xdgDataHome = "/home/tim/.custom-data",
                appId = "cz.adamec.timotej.snag",
            )

        assertEquals(
            expected = "/home/tim/.custom-data/cz.adamec.timotej.snag",
            actual = resolver(),
        )
    }

    @Test
    fun linuxFallsBackToLocalShareWhenXdgMissing() {
        val resolver =
            RealJvmAppDataDirResolver(
                osName = "Linux",
                userHome = "/home/tim",
                localAppData = null,
                xdgDataHome = null,
                appId = "cz.adamec.timotej.snag",
            )

        assertEquals(
            expected = "/home/tim/.local/share/cz.adamec.timotej.snag",
            actual = resolver(),
        )
    }

    @Test
    fun blankXdgDataHomeFallsBackToLocalShare() {
        val resolver =
            RealJvmAppDataDirResolver(
                osName = "Linux",
                userHome = "/home/tim",
                localAppData = null,
                xdgDataHome = "",
                appId = "cz.adamec.timotej.snag",
            )

        assertEquals(
            expected = "/home/tim/.local/share/cz.adamec.timotej.snag",
            actual = resolver(),
        )
    }

    @Test
    fun unknownOsTreatedAsLinuxLike() {
        val resolver =
            RealJvmAppDataDirResolver(
                osName = "FreeBSD",
                userHome = "/home/tim",
                localAppData = null,
                xdgDataHome = null,
                appId = "cz.adamec.timotej.snag",
            )

        assertEquals(
            expected = "/home/tim/.local/share/cz.adamec.timotej.snag",
            actual = resolver(),
        )
    }
}
