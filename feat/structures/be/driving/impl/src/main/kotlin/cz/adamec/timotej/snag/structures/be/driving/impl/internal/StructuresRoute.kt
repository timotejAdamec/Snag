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

package cz.adamec.timotej.snag.structures.be.driving.impl.internal

import cz.adamec.timotej.snag.routing.be.AppRoute
import cz.adamec.timotej.snag.routing.be.getIdFromParameters
import cz.adamec.timotej.snag.structures.be.app.api.GetStructuresUseCase
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route

internal class StructuresRoute(
    private val getStructuresUseCase: GetStructuresUseCase,
) : AppRoute {
    override fun Route.setup() {
        route("/projects/{projectId}/structures") {
            get {
                val projectId = getIdFromParameters("projectId")
                val dtoStructures =
                    getStructuresUseCase(projectId).map {
                        it.toDto()
                    }
                call.respond(dtoStructures)
            }
        }
    }
}
