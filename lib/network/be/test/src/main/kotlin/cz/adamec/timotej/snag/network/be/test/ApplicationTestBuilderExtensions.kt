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

package cz.adamec.timotej.snag.network.be.test

import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation as ClientContentNegotiation

fun ApplicationTestBuilder.jsonClient() =
    createClient {
        install(ClientContentNegotiation) { json() }
    }
