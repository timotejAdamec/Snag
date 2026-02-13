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

import cz.adamec.timotej.snag.lib.core.common.Timestamp
import cz.adamec.timotej.snag.projects.be.app.api.DeleteProjectUseCase
import cz.adamec.timotej.snag.projects.be.app.api.GetProjectUseCase
import cz.adamec.timotej.snag.projects.be.app.api.GetProjectsModifiedSinceUseCase
import cz.adamec.timotej.snag.projects.be.app.api.GetProjectsUseCase
import cz.adamec.timotej.snag.projects.be.app.api.SaveProjectUseCase
import cz.adamec.timotej.snag.projects.be.app.api.model.DeleteProjectRequest
import cz.adamec.timotej.snag.projects.be.driving.contract.DeleteProjectApiDto
import cz.adamec.timotej.snag.projects.be.driving.contract.PutProjectApiDto
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
internal class ProjectsRoute(
    private val getProjectsUseCase: GetProjectsUseCase,
    private val getProjectsModifiedSinceUseCase: GetProjectsModifiedSinceUseCase,
    private val getProjectUseCase: GetProjectUseCase,
    private val saveProjectUseCase: SaveProjectUseCase,
    private val deleteProjectUseCase: DeleteProjectUseCase,
) : AppRoute {
    override fun Route.setup() {
        route("/projects") {
            get {
                val sinceParam = call.request.queryParameters["since"]
                if (sinceParam != null) {
                    val since = Timestamp(sinceParam.toLong())
                    val modified = getProjectsModifiedSinceUseCase(since).map { it.toDto() }
                    call.respond(modified)
                } else {
                    val dtoProjects = getProjectsUseCase().map { it.toDto() }
                    call.respond(dtoProjects)
                }
            }

            get("/{id}") {
                val id = getIdFromParameters()

                val dtoProject =
                    getProjectUseCase(id)
                        ?: return@get call.respond(
                            status = HttpStatusCode.NotFound,
                            message = "Project not found.",
                        )

                call.respond(dtoProject.toDto())
            }

            put("/{id}") {
                val id = getIdFromParameters()

                val putProjectDto = getDtoFromBody<PutProjectApiDto>()

                val updatedProject = saveProjectUseCase(putProjectDto.toModel(id))

                updatedProject?.let {
                    call.respond(it.toDto())
                } ?: call.respond(HttpStatusCode.NoContent)
            }

            delete("/{id}") {
                val id = getIdFromParameters()
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
    }
}
