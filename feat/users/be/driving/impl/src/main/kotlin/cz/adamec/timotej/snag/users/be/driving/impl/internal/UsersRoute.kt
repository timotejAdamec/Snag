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

package cz.adamec.timotej.snag.users.be.driving.impl.internal

import cz.adamec.timotej.snag.lib.core.common.Timestamp
import cz.adamec.timotej.snag.routing.be.AppRoute
import cz.adamec.timotej.snag.routing.be.getDtoFromBody
import cz.adamec.timotej.snag.routing.be.getIdFromParameters
import cz.adamec.timotej.snag.users.be.app.api.GetUserUseCase
import cz.adamec.timotej.snag.users.be.app.api.GetUsersModifiedSinceUseCase
import cz.adamec.timotej.snag.users.be.app.api.GetUsersUseCase
import cz.adamec.timotej.snag.users.be.app.api.SaveUserUseCase
import cz.adamec.timotej.snag.users.be.driving.contract.PutUserApiDto
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.put
import io.ktor.server.routing.route

@Suppress("LabeledExpression")
internal class UsersRoute(
    private val getUsersUseCase: GetUsersUseCase,
    private val getUsersModifiedSinceUseCase: GetUsersModifiedSinceUseCase,
    private val getUserUseCase: GetUserUseCase,
    private val saveUserUseCase: SaveUserUseCase,
) : AppRoute {
    override fun Route.setup() {
        route("/users") {
            get {
                val sinceParam = call.request.queryParameters["since"]
                if (sinceParam != null) {
                    val since = Timestamp(sinceParam.toLong())
                    val modified = getUsersModifiedSinceUseCase(since).map { it.toDto() }
                    call.respond(modified)
                } else {
                    val dtoUsers = getUsersUseCase().map { it.toDto() }
                    call.respond(dtoUsers)
                }
            }

            get("/{id}") {
                val id = getIdFromParameters()

                val dtoUser =
                    getUserUseCase(id)
                        ?: return@get call.respond(
                            status = HttpStatusCode.NotFound,
                            message = "User not found.",
                        )

                call.respond(dtoUser.toDto())
            }

            put("/{id}") {
                val id = getIdFromParameters()
                val putUserDto = getDtoFromBody<PutUserApiDto>()
                val savedUser = saveUserUseCase(putUserDto.toModel(id))
                call.respond(savedUser.toDto())
            }
        }
    }
}
