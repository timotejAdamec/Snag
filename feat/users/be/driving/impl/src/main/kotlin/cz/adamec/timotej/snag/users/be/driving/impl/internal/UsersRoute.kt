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

import cz.adamec.timotej.snag.routing.be.AppRoute
import cz.adamec.timotej.snag.routing.be.getDtoFromBody
import cz.adamec.timotej.snag.routing.be.getIdFromParameters
import cz.adamec.timotej.snag.users.be.app.api.AssignUserToProjectUseCase
import cz.adamec.timotej.snag.users.be.app.api.GetProjectAssignmentsUseCase
import cz.adamec.timotej.snag.users.be.app.api.GetUserUseCase
import cz.adamec.timotej.snag.users.be.app.api.GetUsersUseCase
import cz.adamec.timotej.snag.users.be.app.api.RemoveUserFromProjectUseCase
import cz.adamec.timotej.snag.users.be.app.api.SaveUserUseCase
import cz.adamec.timotej.snag.users.be.app.api.UpdateUserRoleUseCase
import cz.adamec.timotej.snag.users.be.driving.contract.PutUserApiDto
import cz.adamec.timotej.snag.users.be.driving.contract.UpdateUserRoleApiDto
import cz.adamec.timotej.snag.users.business.UserRole
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.put
import io.ktor.server.routing.route

@Suppress("LabeledExpression")
internal class UsersRoute(
    private val getUsersUseCase: GetUsersUseCase,
    private val getUserUseCase: GetUserUseCase,
    private val saveUserUseCase: SaveUserUseCase,
    private val updateUserRoleUseCase: UpdateUserRoleUseCase,
    private val getProjectAssignmentsUseCase: GetProjectAssignmentsUseCase,
    private val assignUserToProjectUseCase: AssignUserToProjectUseCase,
    private val removeUserFromProjectUseCase: RemoveUserFromProjectUseCase,
) : AppRoute {
    override fun Route.setup() {
        route("/users") {
            get {
                val dtoUsers = getUsersUseCase().map { it.toDto() }
                call.respond(dtoUsers)
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

            put("/{id}/role") {
                val id = getIdFromParameters()
                val updateRoleDto = getDtoFromBody<UpdateUserRoleApiDto>()
                val role = updateRoleDto.role?.let { UserRole.valueOf(it) }

                val updatedUser =
                    updateUserRoleUseCase(id, role)
                        ?: return@put call.respond(
                            status = HttpStatusCode.NotFound,
                            message = "User not found.",
                        )

                call.respond(updatedUser.toDto())
            }
        }

        route("/projects/{projectId}/assignments") {
            get {
                val projectId = getIdFromParameters("projectId")
                val dtoUsers = getProjectAssignmentsUseCase(projectId).map { it.toDto() }
                call.respond(dtoUsers)
            }

            put("/{userId}") {
                val projectId = getIdFromParameters("projectId")
                val userId = getIdFromParameters("userId")
                assignUserToProjectUseCase(userId, projectId)
                call.respond(HttpStatusCode.NoContent)
            }

            delete("/{userId}") {
                val projectId = getIdFromParameters("projectId")
                val userId = getIdFromParameters("userId")
                removeUserFromProjectUseCase(userId, projectId)
                call.respond(HttpStatusCode.NoContent)
            }
        }
    }
}
