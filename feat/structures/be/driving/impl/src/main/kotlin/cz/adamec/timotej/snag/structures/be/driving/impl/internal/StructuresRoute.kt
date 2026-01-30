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
import cz.adamec.timotej.snag.structures.be.app.api.SaveStructureUseCase
import cz.adamec.timotej.snag.structures.be.driving.contract.PutStructureApiDto
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.put
import io.ktor.server.routing.route

@Suppress("LabeledExpression")
internal class StructuresRoute(
    private val getStructuresUseCase: GetStructuresUseCase,
    private val saveStructureUseCase: SaveStructureUseCase,
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

            put("/{id}") {
                val id = getIdFromParameters()

                val putStructureDto =
                    runCatching { call.receive<PutStructureApiDto>() }.getOrNull()
                        ?: return@put call.respond(
                            status = HttpStatusCode.BadRequest,
                            message = "Invalid request body.",
                        )

                val updatedStructure = saveStructureUseCase(putStructureDto.toBusiness(id))

                updatedStructure?.let {
                    call.respond(it.toDto())
                } ?: call.respond(HttpStatusCode.NoContent)
            }
        }
    }
}
