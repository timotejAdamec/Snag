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

package cz.adamec.timotej.snag.lib.storage.fe.api

import kotlin.test.Test
import kotlin.test.assertEquals

class JvmAppDataDirTest {
    @Test
    fun macOsResolvesUnderApplicationSupport() {
        val dir =
            resolveJvmAppDataDir(
                osName = "Mac OS X",
                userHome = "/Users/tim",
                appData = null,
                xdgDataHome = null,
                appId = "cz.adamec.timotej.snag",
            )

        assertEquals(
            expected = "/Users/tim/Library/Application Support/cz.adamec.timotej.snag",
            actual = dir,
        )
    }

    @Test
    fun darwinAliasResolvesUnderApplicationSupport() {
        val dir =
            resolveJvmAppDataDir(
                osName = "Darwin",
                userHome = "/Users/tim",
                appData = null,
                xdgDataHome = null,
                appId = "cz.adamec.timotej.snag",
            )

        assertEquals(
            expected = "/Users/tim/Library/Application Support/cz.adamec.timotej.snag",
            actual = dir,
        )
    }

    @Test
    fun windowsUsesAppDataEnvWhenSet() {
        val dir =
            resolveJvmAppDataDir(
                osName = "Windows 11",
                userHome = """C:\Users\Tim""",
                appData = """C:\Users\Tim\AppData\Roaming""",
                xdgDataHome = null,
                appId = "cz.adamec.timotej.snag",
            )

        assertEquals(
            expected = """C:\Users\Tim\AppData\Roaming/cz.adamec.timotej.snag""",
            actual = dir,
        )
    }

    @Test
    fun windowsFallsBackToHomeAppDataRoamingWhenEnvMissing() {
        val dir =
            resolveJvmAppDataDir(
                osName = "Windows 11",
                userHome = """C:\Users\Tim""",
                appData = null,
                xdgDataHome = null,
                appId = "cz.adamec.timotej.snag",
            )

        assertEquals(
            expected = """C:\Users\Tim/AppData/Roaming/cz.adamec.timotej.snag""",
            actual = dir,
        )
    }

    @Test
    fun windowsBlankAppDataFallsBackToHomeAppDataRoaming() {
        val dir =
            resolveJvmAppDataDir(
                osName = "Windows 11",
                userHome = """C:\Users\Tim""",
                appData = "",
                xdgDataHome = null,
                appId = "cz.adamec.timotej.snag",
            )

        assertEquals(
            expected = """C:\Users\Tim/AppData/Roaming/cz.adamec.timotej.snag""",
            actual = dir,
        )
    }

    @Test
    fun linuxUsesXdgDataHomeWhenSet() {
        val dir =
            resolveJvmAppDataDir(
                osName = "Linux",
                userHome = "/home/tim",
                appData = null,
                xdgDataHome = "/home/tim/.custom-data",
                appId = "cz.adamec.timotej.snag",
            )

        assertEquals(
            expected = "/home/tim/.custom-data/cz.adamec.timotej.snag",
            actual = dir,
        )
    }

    @Test
    fun linuxFallsBackToLocalShareWhenXdgMissing() {
        val dir =
            resolveJvmAppDataDir(
                osName = "Linux",
                userHome = "/home/tim",
                appData = null,
                xdgDataHome = null,
                appId = "cz.adamec.timotej.snag",
            )

        assertEquals(
            expected = "/home/tim/.local/share/cz.adamec.timotej.snag",
            actual = dir,
        )
    }

    @Test
    fun blankXdgDataHomeFallsBackToLocalShare() {
        val dir =
            resolveJvmAppDataDir(
                osName = "Linux",
                userHome = "/home/tim",
                appData = null,
                xdgDataHome = "",
                appId = "cz.adamec.timotej.snag",
            )

        assertEquals(
            expected = "/home/tim/.local/share/cz.adamec.timotej.snag",
            actual = dir,
        )
    }

    @Test
    fun unknownOsTreatedAsLinuxLike() {
        val dir =
            resolveJvmAppDataDir(
                osName = "FreeBSD",
                userHome = "/home/tim",
                appData = null,
                xdgDataHome = null,
                appId = "cz.adamec.timotej.snag",
            )

        assertEquals(
            expected = "/home/tim/.local/share/cz.adamec.timotej.snag",
            actual = dir,
        )
    }
}
