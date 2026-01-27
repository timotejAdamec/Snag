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

import cz.adamec.timotej.snag.projects.be.app.DeleteProjectUseCase
import cz.adamec.timotej.snag.projects.be.app.GetProjectUseCase
import cz.adamec.timotej.snag.projects.be.app.GetProjectsUseCase
import cz.adamec.timotej.snag.projects.be.app.SaveProjectUseCase
import cz.adamec.timotej.snag.projects.be.driving.contract.PutProjectApiDto
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
import kotlin.uuid.Uuid

@Suppress("LabeledExpression")
internal class ProjectsRoute(
    private val getProjectsUseCase: GetProjectsUseCase,
    private val getProjectUseCase: GetProjectUseCase,
    private val saveProjectUseCase: SaveProjectUseCase,
    private val deleteProjectUseCase: DeleteProjectUseCase,
) : AppRoute {
    override fun Route.setup() {
        route("/projects") {
            get {
                val dtoProjects =
                    getProjectsUseCase().map {
                        it.toDto()
                    }
                call.respond(dtoProjects)
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

                val putProjectDto =
                    runCatching { call.receive<PutProjectApiDto>() }.getOrNull()
                        ?: return@put call.respond(
                            status = HttpStatusCode.BadRequest,
                            message = "Invalid request body.",
                        )

                val updatedProject = saveProjectUseCase(putProjectDto.toBusiness(id))

                updatedProject?.let {
                    call.respond(it.toDto())
                } ?: call.respond(HttpStatusCode.NoContent)
            }

            delete("/{id}") {
                val id = getIdFromParameters()

                deleteProjectUseCase(id)

                call.respond(HttpStatusCode.NoContent)
            }
        }
    }
}
