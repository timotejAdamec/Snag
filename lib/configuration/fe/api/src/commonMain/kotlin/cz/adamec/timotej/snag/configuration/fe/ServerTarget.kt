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

package cz.adamec.timotej.snag.configuration.fe

enum class ServerTarget {
    LOCALHOST,
    DEV,
    DEMO,
    ;

    val serverUrl: String
        get() =
            when (this) {
                LOCALHOST -> FrontendBuildConfig.SERVER_LOCALHOST_URL
                DEV -> FrontendBuildConfig.SERVER_DEV_URL
                DEMO -> FrontendBuildConfig.SERVER_DEMO_URL
            }

    internal companion object {
        fun fromBuildConfig(): ServerTarget =
            when (FrontendBuildConfig.SERVER_TARGET) {
                "dev" -> DEV
                "demo" -> DEMO
                else -> LOCALHOST
            }
    }
}
