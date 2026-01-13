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

package cz.adamec.timotej.snag.impl

import cz.adamec.timotej.snag.routing.be.AppRoute
import io.ktor.server.application.Application
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import org.koin.ktor.ext.getKoin

internal fun Application.configureRouting() {
    val routes = getKoin().getAll<AppRoute>()
    routing {
        routes.forEach { route ->
            with(route) { setup() }
        }
    }
    routing {
        get("/") {
            call.respondText("Ktor: Hello server")
        }
    }
}
