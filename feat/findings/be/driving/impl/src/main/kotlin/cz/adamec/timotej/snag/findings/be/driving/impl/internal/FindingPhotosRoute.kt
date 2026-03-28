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

package cz.adamec.timotej.snag.findings.be.driving.impl.internal

import cz.adamec.timotej.snag.authentication.be.driving.api.currentUser
import cz.adamec.timotej.snag.authorization.be.driving.api.ForbiddenException
import cz.adamec.timotej.snag.core.foundation.common.Timestamp
import cz.adamec.timotej.snag.findings.be.app.api.DeleteFindingPhotoUseCase
import cz.adamec.timotej.snag.findings.be.app.api.GetFindingPhotosModifiedSinceUseCase
import cz.adamec.timotej.snag.findings.be.app.api.GetFindingUseCase
import cz.adamec.timotej.snag.findings.be.app.api.SaveFindingPhotoUseCase
import cz.adamec.timotej.snag.findings.be.app.api.model.DeleteFindingPhotoRequest
import cz.adamec.timotej.snag.findings.be.app.api.model.GetFindingPhotosModifiedSinceRequest
import cz.adamec.timotej.snag.findings.be.driving.contract.DeleteFindingPhotoApiDto
import cz.adamec.timotej.snag.findings.be.driving.contract.PutFindingPhotoApiDto
import cz.adamec.timotej.snag.projects.be.app.api.CanAccessProjectUseCase
import cz.adamec.timotej.snag.routing.be.AppRoute
import cz.adamec.timotej.snag.routing.be.getDtoFromBody
import cz.adamec.timotej.snag.routing.be.getIdFromParameters
import cz.adamec.timotej.snag.structures.be.app.api.GetStructureUseCase
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import kotlin.uuid.Uuid

internal class FindingPhotosRoute(
    private val saveFindingPhotoUseCase: SaveFindingPhotoUseCase,
    private val deleteFindingPhotoUseCase: DeleteFindingPhotoUseCase,
    private val getFindingPhotosModifiedSinceUseCase: GetFindingPhotosModifiedSinceUseCase,
    private val canAccessProjectUseCase: CanAccessProjectUseCase,
    private val getFindingUseCase: GetFindingUseCase,
    private val getStructureUseCase: GetStructureUseCase,
) : AppRoute {
    override fun Route.setup() {
        route("/findings/{findingId}/photos") {
            put("/{id}") {
                val userId = currentUser().userId
                val findingId = getIdFromParameters(FINDING_ID_PARAM)
                requireFindingProjectAccess(userId = userId, findingId = findingId)
                val id = getIdFromParameters()

                val putPhotoDto = getDtoFromBody<PutFindingPhotoApiDto>()

                val updatedPhoto =
                    saveFindingPhotoUseCase(
                        putPhotoDto.toModel(id = id, findingId = findingId),
                    )

                updatedPhoto?.let {
                    call.respond(it.toDto())
                } ?: call.respond(HttpStatusCode.NoContent)
            }

            patch("/{id}") {
                val userId = currentUser().userId
                val findingId = getIdFromParameters(FINDING_ID_PARAM)
                requireFindingProjectAccess(userId = userId, findingId = findingId)
                val id = getIdFromParameters()
                val deleteDto = getDtoFromBody<DeleteFindingPhotoApiDto>()

                val newerPhoto =
                    deleteFindingPhotoUseCase(
                        DeleteFindingPhotoRequest(
                            photoId = id,
                            deletedAt = deleteDto.deletedAt,
                        ),
                    )

                newerPhoto?.let {
                    call.respond(it.toDto())
                } ?: call.respond(HttpStatusCode.NoContent)
            }

            get {
                val userId = currentUser().userId
                val findingId = getIdFromParameters(FINDING_ID_PARAM)
                requireFindingProjectAccess(userId = userId, findingId = findingId)
                val sinceParam = call.request.queryParameters["since"]
                if (sinceParam != null) {
                    val since = Timestamp(sinceParam.toLong())
                    val modified =
                        getFindingPhotosModifiedSinceUseCase(
                            GetFindingPhotosModifiedSinceRequest(
                                findingId = findingId,
                                since = since,
                            ),
                        ).map { it.toDto() }
                    call.respond(modified)
                } else {
                    call.respond(HttpStatusCode.BadRequest)
                }
            }
        }
    }

    private suspend fun requireFindingProjectAccess(
        userId: Uuid,
        findingId: Uuid,
    ) {
        val projectId = resolveProjectIdFromFinding(findingId)
        if (!canAccessProjectUseCase(userId = userId, projectId = projectId)) {
            throw ForbiddenException()
        }
    }

    private suspend fun resolveProjectIdFromFinding(findingId: Uuid): Uuid {
        val finding = getFindingUseCase(findingId) ?: throw ForbiddenException()
        return getStructureUseCase(finding.structureId)?.projectId
            ?: throw ForbiddenException()
    }

    private companion object {
        const val FINDING_ID_PARAM = "findingId"
    }
}
