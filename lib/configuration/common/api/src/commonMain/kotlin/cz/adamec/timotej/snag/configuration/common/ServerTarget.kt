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

package cz.adamec.timotej.snag.configuration.common

enum class ServerTarget {
    LOCALHOST,
    DEV,
    DEMO,
    ;

    internal companion object {
        fun fromBuildConfig(): ServerTarget =
            when (SnagBuildConfig.SERVER_TARGET) {
                "dev" -> DEV
                "demo" -> DEMO
                else -> LOCALHOST
            }
    }
}
