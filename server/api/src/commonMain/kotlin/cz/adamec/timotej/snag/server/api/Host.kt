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

package cz.adamec.timotej.snag.server.api

sealed interface Host {
    data object Localhost : Host {
        const val PORT: Int = 8081
    }

    data object DevRemote : Host {
        const val URL: String = "https://snag-server-dev-ioifzecczq-ey.a.run.app"
    }

    @Suppress("NotImplementedDeclaration")
    data object DemoRemote : Host {
        val URL: String = TODO("Demo environment not yet provisioned")
    }
}
