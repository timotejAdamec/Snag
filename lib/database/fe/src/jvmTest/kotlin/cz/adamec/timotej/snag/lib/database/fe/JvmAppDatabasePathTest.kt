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

import cz.adamec.timotej.snag.lib.storage.fe.api.JvmAppDataDirResolver
import kotlin.test.Test
import kotlin.test.assertEquals

class JvmAppDatabasePathTest {
    @Test
    fun appendsDbNameToResolvedAppDataDir() {
        val resolver = FakeJvmAppDataDirResolver(returns = "/base/cz.adamec.timotej.snag")

        val path =
            resolveJvmAppDatabasePath(
                appDataDirResolver = resolver,
                osName = "Mac OS X",
                userHome = "/Users/tim",
                appData = null,
                xdgDataHome = null,
                appId = "cz.adamec.timotej.snag",
                dbName = "snag.db",
            )

        assertEquals(
            expected = "/base/cz.adamec.timotej.snag/snag.db",
            actual = path,
        )
    }

    @Test
    fun forwardsAllInputsToResolver() {
        val resolver = FakeJvmAppDataDirResolver(returns = "/ignored")

        resolveJvmAppDatabasePath(
            appDataDirResolver = resolver,
            osName = "Windows 11",
            userHome = """C:\Users\Tim""",
            appData = """C:\Users\Tim\AppData\Roaming""",
            xdgDataHome = "/home/tim/.custom-data",
            appId = "cz.adamec.timotej.snag",
            dbName = "snag.db",
        )

        assertEquals(expected = "Windows 11", actual = resolver.lastOsName)
        assertEquals(expected = """C:\Users\Tim""", actual = resolver.lastUserHome)
        assertEquals(expected = """C:\Users\Tim\AppData\Roaming""", actual = resolver.lastAppData)
        assertEquals(expected = "/home/tim/.custom-data", actual = resolver.lastXdgDataHome)
        assertEquals(expected = "cz.adamec.timotej.snag", actual = resolver.lastAppId)
    }

    private class FakeJvmAppDataDirResolver(
        private val returns: String,
    ) : JvmAppDataDirResolver {
        var lastOsName: String? = null
        var lastUserHome: String? = null
        var lastAppData: String? = null
        var lastXdgDataHome: String? = null
        var lastAppId: String? = null

        override fun invoke(
            osName: String,
            userHome: String,
            appData: String?,
            xdgDataHome: String?,
            appId: String,
        ): String {
            lastOsName = osName
            lastUserHome = userHome
            lastAppData = appData
            lastXdgDataHome = xdgDataHome
            lastAppId = appId
            return returns
        }
    }
}
