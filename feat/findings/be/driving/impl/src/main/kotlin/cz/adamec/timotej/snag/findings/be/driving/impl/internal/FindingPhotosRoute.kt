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

import cz.adamec.timotej.snag.core.foundation.common.Timestamp
import cz.adamec.timotej.snag.findings.be.app.api.DeleteFindingPhotoUseCase
import cz.adamec.timotej.snag.findings.be.app.api.GetFindingPhotosModifiedSinceUseCase
import cz.adamec.timotej.snag.findings.be.app.api.SaveFindingPhotoUseCase
import cz.adamec.timotej.snag.findings.be.app.api.model.DeleteFindingPhotoRequest
import cz.adamec.timotej.snag.findings.be.app.api.model.GetFindingPhotosModifiedSinceRequest
import cz.adamec.timotej.snag.findings.be.driving.contract.DeleteFindingPhotoApiDto
import cz.adamec.timotej.snag.findings.be.driving.contract.PutFindingPhotoApiDto
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

@Suppress("LabeledExpression")
internal class FindingPhotosRoute(
    private val saveFindingPhotoUseCase: SaveFindingPhotoUseCase,
    private val deleteFindingPhotoUseCase: DeleteFindingPhotoUseCase,
    private val getFindingPhotosModifiedSinceUseCase: GetFindingPhotosModifiedSinceUseCase,
) : AppRoute {
    override fun Route.setup() {
        route("/findings/photos") {
            patch("/{id}") {
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
        }

        route("/findings/{findingId}/photos") {
            get {
                val findingId = getIdFromParameters("findingId")
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

            put("/{id}") {
                val id = getIdFromParameters()
                val findingId = getIdFromParameters("findingId")

                val putPhotoDto = getDtoFromBody<PutFindingPhotoApiDto>()

                val updatedPhoto =
                    saveFindingPhotoUseCase(
                        putPhotoDto.toModel(id = id, findingId = findingId),
                    )

                updatedPhoto?.let {
                    call.respond(it.toDto())
                } ?: call.respond(HttpStatusCode.NoContent)
            }
        }
    }
}
