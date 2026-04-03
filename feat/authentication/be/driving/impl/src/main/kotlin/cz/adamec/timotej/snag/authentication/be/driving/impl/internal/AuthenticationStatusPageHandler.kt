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
import cz.adamec.timotej.snag.authentication.be.driving.impl.internal.LH.logger
import cz.adamec.timotej.snag.network.be.KtorStatusPageHandler
import io.ktor.http.HttpStatusCode
import io.ktor.server.plugins.statuspages.StatusPagesConfig
import io.ktor.server.response.respond

internal class AuthenticationStatusPageHandler : KtorStatusPageHandler {
    override fun StatusPagesConfig.setup() {
        exception<UnauthenticatedException> { call, _ ->
            logger.warn("Unauthenticated request: {} {}.", call.request.local.method.value, call.request.local.uri)
            call.respond(
                status = HttpStatusCode.Unauthorized,
                message = "Authentication required.",
            )
        }
    }
}
