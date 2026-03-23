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

package cz.adamec.timotej.snag.projects.be.driving.impl.internal

import cz.adamec.timotej.snag.authentication.be.driving.api.currentUser
import cz.adamec.timotej.snag.authorization.be.driving.api.ForbiddenException
import cz.adamec.timotej.snag.core.foundation.common.Timestamp
import cz.adamec.timotej.snag.projects.be.app.api.AssignUserToProjectUseCase
import cz.adamec.timotej.snag.projects.be.app.api.CanAccessProjectUseCase
import cz.adamec.timotej.snag.projects.be.app.api.CanAssignUserToProjectUseCase
import cz.adamec.timotej.snag.projects.be.app.api.CanCloseProjectUseCase
import cz.adamec.timotej.snag.projects.be.app.api.CanCreateProjectUseCase
import cz.adamec.timotej.snag.projects.be.app.api.DeleteProjectUseCase
import cz.adamec.timotej.snag.projects.be.app.api.GetProjectAssignmentsUseCase
import cz.adamec.timotej.snag.projects.be.app.api.GetProjectUseCase
import cz.adamec.timotej.snag.projects.be.app.api.GetProjectsModifiedSinceUseCase
import cz.adamec.timotej.snag.projects.be.app.api.GetProjectsUseCase
import cz.adamec.timotej.snag.projects.be.app.api.RemoveUserFromProjectUseCase
import cz.adamec.timotej.snag.projects.be.app.api.SaveProjectUseCase
import cz.adamec.timotej.snag.projects.be.app.api.model.AssignUserToProjectRequest
import cz.adamec.timotej.snag.projects.be.app.api.model.DeleteProjectRequest
import cz.adamec.timotej.snag.projects.be.app.api.model.RemoveUserFromProjectRequest
import cz.adamec.timotej.snag.projects.be.driving.contract.DeleteProjectApiDto
import cz.adamec.timotej.snag.projects.be.driving.contract.PutProjectApiDto
import cz.adamec.timotej.snag.routing.be.AppRoute
import cz.adamec.timotej.snag.routing.be.getDtoFromBody
import cz.adamec.timotej.snag.routing.be.getIdFromParameters
import cz.adamec.timotej.snag.users.be.driving.api.toDto
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import kotlin.uuid.Uuid

@Suppress("LabeledExpression", "StringLiteralDuplication")
internal class ProjectsRoute(
    private val getProjectsUseCase: GetProjectsUseCase,
    private val getProjectsModifiedSinceUseCase: GetProjectsModifiedSinceUseCase,
    private val getProjectUseCase: GetProjectUseCase,
    private val saveProjectUseCase: SaveProjectUseCase,
    private val deleteProjectUseCase: DeleteProjectUseCase,
    private val getProjectAssignmentsUseCase: GetProjectAssignmentsUseCase,
    private val assignUserToProjectUseCase: AssignUserToProjectUseCase,
    private val removeUserFromProjectUseCase: RemoveUserFromProjectUseCase,
    private val canCreateProjectUseCase: CanCreateProjectUseCase,
    private val canCloseProjectUseCase: CanCloseProjectUseCase,
    private val canAccessProjectUseCase: CanAccessProjectUseCase,
    private val canAssignUserToProjectUseCase: CanAssignUserToProjectUseCase,
) : AppRoute {
    override fun Route.setup() {
        route("/projects") {
            get {
                val userId = currentUser().userId
                val sinceParam = call.request.queryParameters["since"]
                if (sinceParam != null) {
                    val since = Timestamp(sinceParam.toLong())
                    val modified =
                        getProjectsModifiedSinceUseCase(
                            userId = userId,
                            since = since,
                        ).map { it.toDto() }
                    call.respond(modified)
                } else {
                    val dtoProjects = getProjectsUseCase(userId).map { it.toDto() }
                    call.respond(dtoProjects)
                }
            }

            get("/{id}") {
                val id = getIdFromParameters()
                val userId = currentUser().userId
                requireProjectAccess(userId = userId, projectId = id)

                val dtoProject =
                    getProjectUseCase(id)
                        ?: return@get call.respond(
                            status = HttpStatusCode.NotFound,
                            message = "Project not found.",
                        )

                call.respond(dtoProject.toDto())
            }

            setupPutRoute()
            setupPatchRoute()
        }

        setupAssignmentsRoute()
    }

    private fun Route.setupPutRoute() {
        put("/{id}") {
            val id = getIdFromParameters()
            val user = currentUser()
            val putProjectDto = getDtoFromBody<PutProjectApiDto>()

            authorizeProjectSave(
                userId = user.userId,
                projectId = id,
                incomingIsClosed = putProjectDto.isClosed,
            )

            val updatedProject = saveProjectUseCase(putProjectDto.toModel(id))

            updatedProject?.let {
                call.respond(it.toDto())
            } ?: call.respond(HttpStatusCode.NoContent)
        }
    }

    private fun Route.setupPatchRoute() {
        patch("/{id}") {
            val id = getIdFromParameters()
            val userId = currentUser().userId
            requireCloseAccess(userId = userId, projectId = id)

            val deleteProjectDto = getDtoFromBody<DeleteProjectApiDto>()

            val newerProject =
                deleteProjectUseCase(
                    DeleteProjectRequest(
                        projectId = id,
                        deletedAt = deleteProjectDto.deletedAt,
                    ),
                )

            newerProject?.let {
                call.respond(it.toDto())
            } ?: call.respond(HttpStatusCode.NoContent)
        }
    }

    private fun Route.setupAssignmentsRoute() {
        route("/projects/{projectId}/assignments") {
            get {
                val projectId = getIdFromParameters("projectId")
                val userId = currentUser().userId
                requireProjectAccess(userId = userId, projectId = projectId)

                val dtoUsers = getProjectAssignmentsUseCase(projectId).map { it.toDto() }
                call.respond(dtoUsers)
            }

            put("/{userId}") {
                val projectId = getIdFromParameters("projectId")
                val targetUserId = getIdFromParameters("userId")
                val actingUserId = currentUser().userId
                requireAssignmentAccess(actingUserId)

                assignUserToProjectUseCase(
                    AssignUserToProjectRequest(
                        userId = targetUserId,
                        projectId = projectId,
                    ),
                )
                call.respond(HttpStatusCode.NoContent)
            }

            delete("/{userId}") {
                val projectId = getIdFromParameters("projectId")
                val targetUserId = getIdFromParameters("userId")
                val actingUserId = currentUser().userId
                requireAssignmentAccess(actingUserId)

                removeUserFromProjectUseCase(
                    RemoveUserFromProjectRequest(
                        userId = targetUserId,
                        projectId = projectId,
                    ),
                )
                call.respond(HttpStatusCode.NoContent)
            }
        }
    }

    private suspend fun requireProjectAccess(
        userId: Uuid,
        projectId: Uuid,
    ) {
        if (!canAccessProjectUseCase(userId = userId, projectId = projectId)) {
            throw ForbiddenException()
        }
    }

    private suspend fun requireCloseAccess(
        userId: Uuid,
        projectId: Uuid,
    ) {
        if (!canCloseProjectUseCase(userId = userId, projectId = projectId)) {
            throw ForbiddenException()
        }
    }

    private suspend fun requireAssignmentAccess(userId: Uuid) {
        if (!canAssignUserToProjectUseCase(userId)) {
            throw ForbiddenException()
        }
    }

    private suspend fun authorizeProjectSave(
        userId: Uuid,
        projectId: Uuid,
        incomingIsClosed: Boolean,
    ) {
        val existingProject = getProjectUseCase(projectId)

        if (existingProject == null) {
            if (!canCreateProjectUseCase(userId)) throw ForbiddenException()
            return
        }

        requireProjectAccess(userId = userId, projectId = projectId)

        if (incomingIsClosed != existingProject.isClosed) {
            requireCloseAccess(userId = userId, projectId = projectId)
        }
    }
}
