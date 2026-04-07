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
import cz.adamec.timotej.snag.projects.be.app.api.CanAccessProjectUseCase
import cz.adamec.timotej.snag.projects.be.app.api.DeleteProjectPhotoUseCase
import cz.adamec.timotej.snag.projects.be.app.api.GetProjectPhotosModifiedSinceUseCase
import cz.adamec.timotej.snag.projects.be.app.api.SaveProjectPhotoUseCase
import cz.adamec.timotej.snag.projects.be.app.api.model.DeleteProjectPhotoRequest
import cz.adamec.timotej.snag.projects.be.app.api.model.GetProjectPhotosModifiedSinceRequest
import cz.adamec.timotej.snag.projects.contract.DeleteProjectPhotoApiDto
import cz.adamec.timotej.snag.projects.contract.PutProjectPhotoApiDto
import cz.adamec.timotej.snag.routing.be.AppRoute
import cz.adamec.timotej.snag.routing.be.getDtoFromBody
import cz.adamec.timotej.snag.routing.be.getIdFromParameters
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import kotlin.uuid.Uuid

internal class ProjectPhotosRoute(
    private val saveProjectPhotoUseCase: SaveProjectPhotoUseCase,
    private val deleteProjectPhotoUseCase: DeleteProjectPhotoUseCase,
    private val getProjectPhotosModifiedSinceUseCase: GetProjectPhotosModifiedSinceUseCase,
    private val canAccessProjectUseCase: CanAccessProjectUseCase,
) : AppRoute {
    override fun Route.setup() {
        route("/projects/{$PROJECT_ID_PARAM}/photos") {
            put("/{id}") {
                val userId = currentUser().userId
                val projectId = getIdFromParameters(PROJECT_ID_PARAM)
                requireProjectAccess(userId = userId, projectId = projectId)
                val id = getIdFromParameters()

                val putPhotoDto = getDtoFromBody<PutProjectPhotoApiDto>()

                val updatedPhoto =
                    saveProjectPhotoUseCase(
                        putPhotoDto.toModel(id = id, projectId = projectId),
                    )

                updatedPhoto?.let {
                    call.respond(it.toPhotoDto())
                } ?: call.respond(HttpStatusCode.NoContent)
            }

            patch("/{id}") {
                val userId = currentUser().userId
                val projectId = getIdFromParameters(PROJECT_ID_PARAM)
                requireProjectAccess(userId = userId, projectId = projectId)
                val id = getIdFromParameters()
                val deleteDto = getDtoFromBody<DeleteProjectPhotoApiDto>()

                val newerPhoto =
                    deleteProjectPhotoUseCase(
                        DeleteProjectPhotoRequest(
                            photoId = id,
                            deletedAt = deleteDto.deletedAt,
                        ),
                    )

                newerPhoto?.let {
                    call.respond(it.toPhotoDto())
                } ?: call.respond(HttpStatusCode.NoContent)
            }

            get {
                val userId = currentUser().userId
                val projectId = getIdFromParameters(PROJECT_ID_PARAM)
                requireProjectAccess(userId = userId, projectId = projectId)
                val sinceParam = call.request.queryParameters["since"]
                if (sinceParam != null) {
                    val since = Timestamp(sinceParam.toLong())
                    val modified =
                        getProjectPhotosModifiedSinceUseCase(
                            GetProjectPhotosModifiedSinceRequest(
                                projectId = projectId,
                                since = since,
                            ),
                        ).map { it.toPhotoDto() }
                    call.respond(modified)
                } else {
                    call.respond(HttpStatusCode.BadRequest)
                }
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

    private companion object {
        const val PROJECT_ID_PARAM = "projectId"
    }
}
