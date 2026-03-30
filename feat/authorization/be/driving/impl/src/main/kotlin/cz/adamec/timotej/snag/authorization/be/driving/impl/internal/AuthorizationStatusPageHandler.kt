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

package cz.adamec.timotej.snag.authorization.be.driving.impl.internal

import cz.adamec.timotej.snag.authorization.be.driving.api.ForbiddenException
import cz.adamec.timotej.snag.authorization.be.driving.impl.internal.LH.logger
import cz.adamec.timotej.snag.configuration.be.AppStatusPageHandler
import io.ktor.http.HttpStatusCode
import io.ktor.server.plugins.statuspages.StatusPagesConfig
import io.ktor.server.response.respond

internal class AuthorizationStatusPageHandler : AppStatusPageHandler {
    override fun StatusPagesConfig.setup() {
        exception<ForbiddenException> { call, _ ->
            logger.warn("Forbidden request: {} {}.", call.request.local.method.value, call.request.local.uri)
            call.respond(
                status = HttpStatusCode.Forbidden,
                message = "Access denied.",
            )
        }
    }
}
