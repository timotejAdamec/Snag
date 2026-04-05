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

import cz.adamec.timotej.snag.authentication.be.driving.api.currentUser
import cz.adamec.timotej.snag.authorization.be.driving.api.ForbiddenException
import cz.adamec.timotej.snag.projects.be.app.api.CanAccessProjectUseCase
import cz.adamec.timotej.snag.projects.be.app.api.GetProjectUseCase
import cz.adamec.timotej.snag.reports.be.app.api.CanGenerateReportUseCase
import cz.adamec.timotej.snag.reports.be.app.api.GenerateProjectReportUseCase
import cz.adamec.timotej.snag.reports.business.ReportType
import cz.adamec.timotej.snag.routing.be.AppRoute
import cz.adamec.timotej.snag.routing.be.getIdFromParameters
import io.ktor.http.ContentDisposition
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.header
import io.ktor.server.response.respond
import io.ktor.server.response.respondBytes
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import kotlin.uuid.Uuid

@Suppress("LabeledExpression")
internal class ReportRoute(
    private val generateProjectReportUseCase: GenerateProjectReportUseCase,
    private val canAccessProjectUseCase: CanAccessProjectUseCase,
    private val canGenerateReportUseCase: CanGenerateReportUseCase,
    private val getProjectUseCase: GetProjectUseCase,
) : AppRoute {
    override fun Route.setup() {
        route("/projects/{projectId}/report") {
            get {
                val userId = currentUser().userId
                val projectId = getIdFromParameters("projectId")
                val reportType = parseReportType(call.parameters["type"])
                if (reportType == null) {
                    call.respond(HttpStatusCode.BadRequest)
                    return@get
                }
                if (getProjectUseCase(projectId) == null) {
                    call.respond(HttpStatusCode.NotFound)
                    return@get
                }
                requireProjectAccess(userId = userId, projectId = projectId)
                requireReportTypeAccess(userId = userId, type = reportType)
                val report = generateProjectReportUseCase(projectId, reportType)

                if (report != null) {
                    val disposition =
                        ContentDisposition.Attachment.withParameter(
                            ContentDisposition.Parameters.FileName,
                            report.fileName,
                        )
                    call.response.header(
                        HttpHeaders.ContentDisposition,
                        disposition.toString(),
                    )
                    call.respondBytes(report.bytes, ContentType.Application.Pdf)
                } else {
                    call.respond(HttpStatusCode.NotFound)
                }
            }
        }
    }

    private fun parseReportType(value: String?): ReportType? =
        when (value) {
            null, "passport" -> ReportType.PASSPORT
            "service_protocol" -> ReportType.SERVICE_PROTOCOL
            else -> null
        }

    private suspend fun requireProjectAccess(
        userId: Uuid,
        projectId: Uuid,
    ) {
        if (!canAccessProjectUseCase(userId = userId, projectId = projectId)) {
            throw ForbiddenException()
        }
    }

    private suspend fun requireReportTypeAccess(
        userId: Uuid,
        type: ReportType,
    ) {
        if (!canGenerateReportUseCase(userId = userId, type = type)) {
            throw ForbiddenException()
        }
    }
}
