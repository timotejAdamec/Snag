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

package cz.adamec.timotej.snag.network.be

import io.ktor.server.application.Application

interface KtorServerConfiguration {
    /**
     * Installation priority. Lower values are installed first.
     * Use negative values for plugins that must precede authentication (e.g. CORS).
     */
    val priority: Int get() = 0

    fun Application.setup()
}
