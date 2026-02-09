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

package cz.adamec.timotej.snag.clients.be.driving.impl.internal

import cz.adamec.timotej.snag.clients.be.app.api.DeleteClientUseCase
import cz.adamec.timotej.snag.clients.be.app.api.GetClientUseCase
import cz.adamec.timotej.snag.clients.be.app.api.GetClientsModifiedSinceUseCase
import cz.adamec.timotej.snag.clients.be.app.api.GetClientsUseCase
import cz.adamec.timotej.snag.clients.be.app.api.SaveClientUseCase
import cz.adamec.timotej.snag.clients.be.app.api.model.DeleteClientRequest
import cz.adamec.timotej.snag.clients.be.driving.contract.DeleteClientApiDto
import cz.adamec.timotej.snag.clients.be.driving.contract.PutClientApiDto
import cz.adamec.timotej.snag.lib.core.common.Timestamp
import cz.adamec.timotej.snag.routing.be.AppRoute
import cz.adamec.timotej.snag.routing.be.getDtoFromBody
import cz.adamec.timotej.snag.routing.be.getIdFromParameters
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.put
import io.ktor.server.routing.route

@Suppress("LabeledExpression", "StringLiteralDuplication")
internal class ClientsRoute(
    private val getClientsUseCase: GetClientsUseCase,
    private val getClientsModifiedSinceUseCase: GetClientsModifiedSinceUseCase,
    private val getClientUseCase: GetClientUseCase,
    private val saveClientUseCase: SaveClientUseCase,
    private val deleteClientUseCase: DeleteClientUseCase,
) : AppRoute {
    override fun Route.setup() {
        route("/clients") {
            get {
                val sinceParam = call.request.queryParameters["since"]
                if (sinceParam != null) {
                    val since = Timestamp(sinceParam.toLong())
                    val modified = getClientsModifiedSinceUseCase(since).map { it.toDto() }
                    call.respond(modified)
                } else {
                    val dtoClients = getClientsUseCase().map { it.toDto() }
                    call.respond(dtoClients)
                }
            }

            get("/{id}") {
                val id = getIdFromParameters()

                val dtoClient =
                    getClientUseCase(id)
                        ?: return@get call.respond(
                            status = HttpStatusCode.NotFound,
                            message = "Client not found.",
                        )

                call.respond(dtoClient.toDto())
            }

            put("/{id}") {
                val id = getIdFromParameters()

                val putClientDto = getDtoFromBody<PutClientApiDto>()

                val updatedClient = saveClientUseCase(putClientDto.toModel(id))

                updatedClient?.let {
                    call.respond(it.toDto())
                } ?: call.respond(HttpStatusCode.NoContent)
            }

            delete("/{id}") {
                val id = getIdFromParameters()
                val deleteClientDto = getDtoFromBody<DeleteClientApiDto>()

                val newerClient =
                    deleteClientUseCase(
                        DeleteClientRequest(
                            clientId = id,
                            deletedAt = deleteClientDto.deletedAt,
                        ),
                    )

                newerClient?.let {
                    call.respond(it.toDto())
                } ?: call.respond(HttpStatusCode.NoContent)
            }
        }
    }
}
