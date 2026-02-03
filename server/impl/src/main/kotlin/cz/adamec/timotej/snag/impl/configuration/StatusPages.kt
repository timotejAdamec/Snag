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

package cz.adamec.timotej.snag.impl.configuration

import cz.adamec.timotej.snag.routing.be.InvalidBodyException
import cz.adamec.timotej.snag.routing.be.InvalidIdException
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond

internal fun Application.configureStatusPages() {
    install(StatusPages) {
        exception<InvalidIdException> { call, _ ->
            call.respond(
                status = HttpStatusCode.BadRequest,
                message = "Invalid ID format.",
            )
        }
        exception<InvalidBodyException> { call, _ ->
            call.respond(
                status = HttpStatusCode.BadRequest,
                message = "Invalid request body.",
            )
        }
    }
}
