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

import cz.adamec.timotej.snag.projects.be.app.GetProjectsUseCase
import cz.adamec.timotej.snag.routing.be.AppRoute
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route

internal class ProjectsRoute(
    private val getProjectsUseCase: GetProjectsUseCase,
) : AppRoute {
    override fun Route.setup() {
        route("/projects") {
            get {
                val dtoProjects = getProjectsUseCase().toDto()
                call.respond(dtoProjects)
            }
        }
    }
}
