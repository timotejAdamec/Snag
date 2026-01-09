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

package cz.adamec.timotej.snag.projects.be.driving.internal

import cz.adamec.timotej.snag.routing.be.AppRoute
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route

internal class ProjectsRoute : AppRoute {
    override fun Route.setup() {
        route("/projects") {
            get {
                call.respondText("Hello from projects!")
            }
        }
    }
}