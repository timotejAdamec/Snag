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

package cz.adamec.timotej.snag.reports.be.driving.impl.internal

import cz.adamec.timotej.snag.reports.be.app.api.GenerateProjectReportUseCase
import cz.adamec.timotej.snag.routing.be.AppRoute
import cz.adamec.timotej.snag.routing.be.getIdFromParameters
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.response.respondBytes
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route

@Suppress("LabeledExpression")
internal class ReportRoute(
    private val generateProjectReportUseCase: GenerateProjectReportUseCase,
) : AppRoute {
    override fun Route.setup() {
        route("/projects/{projectId}/report") {
            get {
                val projectId = getIdFromParameters("projectId")
                val report = generateProjectReportUseCase(projectId)

                if (report != null) {
                    call.respondBytes(report.report.bytes, ContentType.Application.Pdf)
                } else {
                    call.respond(HttpStatusCode.NotFound)
                }
            }
        }
    }
}
