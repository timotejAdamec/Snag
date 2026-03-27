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

package cz.adamec.timotej.snag.feat.inspections.be.driving.impl.internal

import cz.adamec.timotej.snag.authentication.be.driving.api.currentUser
import cz.adamec.timotej.snag.authorization.be.driving.api.ForbiddenException
import cz.adamec.timotej.snag.core.foundation.common.Timestamp
import cz.adamec.timotej.snag.feat.inspections.be.app.api.DeleteInspectionUseCase
import cz.adamec.timotej.snag.feat.inspections.be.app.api.GetInspectionsModifiedSinceUseCase
import cz.adamec.timotej.snag.feat.inspections.be.app.api.GetInspectionsUseCase
import cz.adamec.timotej.snag.feat.inspections.be.app.api.SaveInspectionUseCase
import cz.adamec.timotej.snag.feat.inspections.be.app.api.model.DeleteInspectionRequest
import cz.adamec.timotej.snag.feat.inspections.be.app.api.model.GetInspectionsModifiedSinceRequest
import cz.adamec.timotej.snag.feat.inspections.be.driving.contract.DeleteInspectionApiDto
import cz.adamec.timotej.snag.feat.inspections.be.driving.contract.PutInspectionApiDto
import cz.adamec.timotej.snag.feat.inspections.be.ports.InspectionsDb
import cz.adamec.timotej.snag.projects.be.app.api.CanAccessProjectUseCase
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
import kotlin.uuid.Uuid

@Suppress("LabeledExpression")
internal class InspectionsRoute(
    private val deleteInspectionUseCase: DeleteInspectionUseCase,
    private val getInspectionsUseCase: GetInspectionsUseCase,
    private val getInspectionsModifiedSinceUseCase: GetInspectionsModifiedSinceUseCase,
    private val saveInspectionUseCase: SaveInspectionUseCase,
    private val canAccessProjectUseCase: CanAccessProjectUseCase,
    private val inspectionsDb: InspectionsDb,
) : AppRoute {
    override fun Route.setup() {
        route("/inspections") {
            delete("/{id}") {
                val userId = currentUser().userId
                val id = getIdFromParameters()
                val projectId = inspectionsDb.getInspection(id)?.projectId
                    ?: throw ForbiddenException()
                requireProjectAccess(userId = userId, projectId = projectId)
                val deleteInspectionDto = getDtoFromBody<DeleteInspectionApiDto>()

                val newerInspection =
                    deleteInspectionUseCase(
                        DeleteInspectionRequest(
                            inspectionId = id,
                            deletedAt = deleteInspectionDto.deletedAt,
                        ),
                    )

                newerInspection?.let {
                    call.respond(it.toDto())
                } ?: call.respond(HttpStatusCode.NoContent)
            }
        }

        route("/projects/{projectId}/inspections") {
            get {
                val userId = currentUser().userId
                val projectId = getIdFromParameters("projectId")
                requireProjectAccess(userId = userId, projectId = projectId)
                val sinceParam = call.request.queryParameters["since"]
                if (sinceParam != null) {
                    val since = Timestamp(sinceParam.toLong())
                    val modified =
                        getInspectionsModifiedSinceUseCase(
                            GetInspectionsModifiedSinceRequest(
                                projectId = projectId,
                                since = since,
                            ),
                        ).map { it.toDto() }
                    call.respond(modified)
                } else {
                    val dtoInspections = getInspectionsUseCase(projectId).map { it.toDto() }
                    call.respond(dtoInspections)
                }
            }

            put("/{id}") {
                val userId = currentUser().userId
                val projectId = getIdFromParameters("projectId")
                requireProjectAccess(userId = userId, projectId = projectId)
                val id = getIdFromParameters()
                val putInspectionDto = getDtoFromBody<PutInspectionApiDto>()

                val updatedInspection = saveInspectionUseCase(putInspectionDto.toModel(id))

                updatedInspection?.let {
                    call.respond(it.toDto())
                } ?: call.respond(HttpStatusCode.NoContent)
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
}
