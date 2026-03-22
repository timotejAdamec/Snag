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

package cz.adamec.timotej.snag.authentication.be.driving.impl.internal

import cz.adamec.timotej.snag.authentication.be.driving.api.UnauthenticatedException
import cz.adamec.timotej.snag.configuration.be.AppStatusPageHandler
import io.ktor.http.HttpStatusCode
import io.ktor.server.plugins.statuspages.StatusPagesConfig
import io.ktor.server.response.respond

internal class AuthenticationStatusPageHandler : AppStatusPageHandler {
    override fun StatusPagesConfig.setup() {
        exception<UnauthenticatedException> { call, _ ->
            call.respond(
                status = HttpStatusCode.Unauthorized,
                message = "Authentication required.",
            )
        }
    }
}
