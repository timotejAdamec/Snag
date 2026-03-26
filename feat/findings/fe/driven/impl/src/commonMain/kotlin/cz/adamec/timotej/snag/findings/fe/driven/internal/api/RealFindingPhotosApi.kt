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

package cz.adamec.timotej.snag.findings.fe.driven.internal.api

import cz.adamec.timotej.snag.core.foundation.common.Timestamp
import cz.adamec.timotej.snag.core.network.fe.OnlineDataResult
import cz.adamec.timotej.snag.core.network.fe.safeApiCall
import cz.adamec.timotej.snag.feat.findings.app.model.AppFindingPhoto
import cz.adamec.timotej.snag.findings.be.driving.contract.DeleteFindingPhotoApiDto
import cz.adamec.timotej.snag.findings.be.driving.contract.FindingPhotoApiDto
import cz.adamec.timotej.snag.findings.fe.driven.internal.LH
import cz.adamec.timotej.snag.findings.fe.ports.FindingPhotoSyncResult
import cz.adamec.timotej.snag.findings.fe.ports.FindingPhotosApi
import cz.adamec.timotej.snag.network.fe.SnagNetworkHttpClient
import io.ktor.client.call.body
import io.ktor.client.request.setBody
import io.ktor.http.HttpStatusCode
import kotlin.uuid.Uuid

internal class RealFindingPhotosApi(
    private val httpClient: SnagNetworkHttpClient,
) : FindingPhotosApi {
    override suspend fun savePhoto(photo: AppFindingPhoto): OnlineDataResult<AppFindingPhoto?> {
        LH.logger.d { "Saving photo ${photo.id} to API..." }
        return safeApiCall(logger = LH.logger, errorContext = "Error saving photo ${photo.id} to API.") {
            val response =
                httpClient.put("/findings/${photo.findingId}/photos/${photo.id}") {
                    setBody(photo.toPutApiDto())
                }
            if (response.status != HttpStatusCode.NoContent) {
                response.body<FindingPhotoApiDto>().toModel()
            } else {
                null
            }
        }.also { if (it is OnlineDataResult.Success) LH.logger.d { "Saved photo ${photo.id} to API." } }
    }

    override suspend fun deletePhoto(
        id: Uuid,
        findingId: Uuid,
        deletedAt: Timestamp,
    ): OnlineDataResult<AppFindingPhoto?> {
        LH.logger.d { "Deleting photo $id from API..." }
        return safeApiCall(logger = LH.logger, errorContext = "Error deleting photo $id from API.") {
            val response =
                httpClient.patch("/findings/$findingId/photos/$id") {
                    setBody(DeleteFindingPhotoApiDto(deletedAt = deletedAt))
                }
            if (response.status != HttpStatusCode.NoContent) {
                response.body<FindingPhotoApiDto>().toModel()
            } else {
                null
            }
        }.also { if (it is OnlineDataResult.Success) LH.logger.d { "Deleted photo $id from API." } }
    }

    override suspend fun getPhotosModifiedSince(
        findingId: Uuid,
        since: Timestamp,
    ): OnlineDataResult<List<FindingPhotoSyncResult>> {
        LH.logger.d { "Fetching photos modified since $since for finding $findingId..." }
        return safeApiCall(logger = LH.logger, errorContext = "Error fetching photos modified since $since for finding $findingId.") {
            httpClient.get("/findings/$findingId/photos?since=${since.value}").body<List<FindingPhotoApiDto>>().map { dto ->
                dto.toSyncResult()
            }
        }.also {
            if (it is OnlineDataResult.Success) LH.logger.d { "Fetched ${it.data.size} modified photos for finding $findingId." }
        }
    }
}
