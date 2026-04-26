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

import kotlin.test.Test
import kotlin.test.assertEquals

class JvmAppDatabasePathTest {
    @Test
    fun macOsResolvesUnderApplicationSupport() {
        val path =
            resolveJvmAppDatabasePath(
                osName = "Mac OS X",
                userHome = "/Users/tim",
                appData = null,
                xdgDataHome = null,
                appId = "cz.adamec.timotej.snag",
                dbName = "snag.db",
            )

        assertEquals(
            expected = "/Users/tim/Library/Application Support/cz.adamec.timotej.snag/snag.db",
            actual = path,
        )
    }

    @Test
    fun windowsUsesAppDataEnvWhenSet() {
        val path =
            resolveJvmAppDatabasePath(
                osName = "Windows 11",
                userHome = """C:\Users\Tim""",
                appData = """C:\Users\Tim\AppData\Roaming""",
                xdgDataHome = null,
                appId = "cz.adamec.timotej.snag",
                dbName = "snag.db",
            )

        assertEquals(
            expected = """C:\Users\Tim\AppData\Roaming/cz.adamec.timotej.snag/snag.db""",
            actual = path,
        )
    }

    @Test
    fun windowsFallsBackToHomeAppDataRoamingWhenEnvMissing() {
        val path =
            resolveJvmAppDatabasePath(
                osName = "Windows 11",
                userHome = """C:\Users\Tim""",
                appData = null,
                xdgDataHome = null,
                appId = "cz.adamec.timotej.snag",
                dbName = "snag.db",
            )

        assertEquals(
            expected = """C:\Users\Tim/AppData/Roaming/cz.adamec.timotej.snag/snag.db""",
            actual = path,
        )
    }

    @Test
    fun linuxUsesXdgDataHomeWhenSet() {
        val path =
            resolveJvmAppDatabasePath(
                osName = "Linux",
                userHome = "/home/tim",
                appData = null,
                xdgDataHome = "/home/tim/.custom-data",
                appId = "cz.adamec.timotej.snag",
                dbName = "snag.db",
            )

        assertEquals(
            expected = "/home/tim/.custom-data/cz.adamec.timotej.snag/snag.db",
            actual = path,
        )
    }

    @Test
    fun linuxFallsBackToLocalShareWhenXdgMissing() {
        val path =
            resolveJvmAppDatabasePath(
                osName = "Linux",
                userHome = "/home/tim",
                appData = null,
                xdgDataHome = null,
                appId = "cz.adamec.timotej.snag",
                dbName = "snag.db",
            )

        assertEquals(
            expected = "/home/tim/.local/share/cz.adamec.timotej.snag/snag.db",
            actual = path,
        )
    }

    @Test
    fun unknownOsTreatedAsLinuxLike() {
        val path =
            resolveJvmAppDatabasePath(
                osName = "FreeBSD",
                userHome = "/home/tim",
                appData = null,
                xdgDataHome = null,
                appId = "cz.adamec.timotej.snag",
                dbName = "snag.db",
            )

        assertEquals(
            expected = "/home/tim/.local/share/cz.adamec.timotej.snag/snag.db",
            actual = path,
        )
    }

    @Test
    fun blankXdgDataHomeFallsBackToLocalShare() {
        val path =
            resolveJvmAppDatabasePath(
                osName = "Linux",
                userHome = "/home/tim",
                appData = null,
                xdgDataHome = "",
                appId = "cz.adamec.timotej.snag",
                dbName = "snag.db",
            )

        assertEquals(
            expected = "/home/tim/.local/share/cz.adamec.timotej.snag/snag.db",
            actual = path,
        )
    }
}
