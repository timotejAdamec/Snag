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

package cz.adamec.timotej.snag.findings.be.driving.impl.internal

import cz.adamec.timotej.snag.findings.be.app.api.DeleteFindingUseCase
import cz.adamec.timotej.snag.findings.be.app.api.GetFindingsUseCase
import cz.adamec.timotej.snag.findings.be.app.api.SaveFindingUseCase
import cz.adamec.timotej.snag.findings.be.driving.contract.PutFindingApiDto
import cz.adamec.timotej.snag.routing.be.AppRoute
import cz.adamec.timotej.snag.routing.be.getIdFromParameters
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.put
import io.ktor.server.routing.route

@Suppress("LabeledExpression")
internal class FindingsRoute(
    private val deleteFindingUseCase: DeleteFindingUseCase,
    private val getFindingsUseCase: GetFindingsUseCase,
    private val saveFindingUseCase: SaveFindingUseCase,
) : AppRoute {
    override fun Route.setup() {
        route("/findings") {
            delete("/{id}") {
                val id = getIdFromParameters()

                deleteFindingUseCase(id)

                call.respond(HttpStatusCode.NoContent)
            }
        }

        route("/structures/{structureId}/findings") {
            get {
                val structureId = getIdFromParameters("structureId")
                val dtoFindings =
                    getFindingsUseCase(structureId).map {
                        it.toDto()
                    }
                call.respond(dtoFindings)
            }

            put("/{id}") {
                val id = getIdFromParameters()

                val putFindingDto =
                    runCatching { call.receive<PutFindingApiDto>() }.getOrNull()
                        ?: return@put call.respond(
                            status = HttpStatusCode.BadRequest,
                            message = "Invalid request body.",
                        )

                val updatedFinding = saveFindingUseCase(putFindingDto.toBusiness(id))

                updatedFinding?.let {
                    call.respond(it.toDto())
                } ?: call.respond(HttpStatusCode.NoContent)
            }
        }
    }
}
