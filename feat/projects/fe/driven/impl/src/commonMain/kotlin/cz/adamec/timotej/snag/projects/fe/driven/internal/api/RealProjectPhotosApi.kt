package cz.adamec.timotej.snag.projects.fe.driven.internal.api

import cz.adamec.timotej.snag.core.foundation.common.Timestamp
import cz.adamec.timotej.snag.core.network.fe.OnlineDataResult
import cz.adamec.timotej.snag.core.network.fe.safeApiCall
import cz.adamec.timotej.snag.network.fe.SnagNetworkHttpClient
import cz.adamec.timotej.snag.projects.app.model.AppProjectPhoto
import cz.adamec.timotej.snag.projects.contract.DeleteProjectPhotoApiDto
import cz.adamec.timotej.snag.projects.contract.ProjectPhotoApiDto
import cz.adamec.timotej.snag.projects.fe.driven.internal.LH
import cz.adamec.timotej.snag.projects.fe.ports.ProjectPhotoSyncResult
import cz.adamec.timotej.snag.projects.fe.ports.ProjectPhotosApi
import io.ktor.client.call.body
import io.ktor.client.request.setBody
import io.ktor.http.HttpStatusCode
import kotlin.uuid.Uuid

internal class RealProjectPhotosApi(
    private val httpClient: SnagNetworkHttpClient,
) : ProjectPhotosApi {
    override suspend fun savePhoto(photo: AppProjectPhoto): OnlineDataResult<AppProjectPhoto?> {
        LH.logger.d { "Saving project photo ${photo.id} to API..." }
        return safeApiCall(
            logger = LH.logger,
            errorContext = "Error saving project photo ${photo.id} to API.",
        ) {
            val response =
                httpClient.put("/projects/${photo.projectId}/photos/${photo.id}") {
                    setBody(photo.toPutApiDto())
                }
            if (response.status != HttpStatusCode.NoContent) {
                response.body<ProjectPhotoApiDto>().toModel()
            } else {
                null
            }
        }.also {
            if (it is OnlineDataResult.Success) LH.logger.d { "Saved project photo ${photo.id} to API." }
        }
    }

    override suspend fun deletePhoto(
        id: Uuid,
        projectId: Uuid,
        deletedAt: Timestamp,
    ): OnlineDataResult<AppProjectPhoto?> {
        LH.logger.d { "Deleting project photo $id from API..." }
        return safeApiCall(
            logger = LH.logger,
            errorContext = "Error deleting project photo $id from API.",
        ) {
            val response =
                httpClient.patch("/projects/$projectId/photos/$id") {
                    setBody(DeleteProjectPhotoApiDto(deletedAt = deletedAt))
                }
            if (response.status != HttpStatusCode.NoContent) {
                response.body<ProjectPhotoApiDto>().toModel()
            } else {
                null
            }
        }.also {
            if (it is OnlineDataResult.Success) LH.logger.d { "Deleted project photo $id from API." }
        }
    }

    override suspend fun getPhotosModifiedSince(
        projectId: Uuid,
        since: Timestamp,
    ): OnlineDataResult<List<ProjectPhotoSyncResult>> {
        LH.logger.d { "Fetching project photos modified since $since for project $projectId..." }
        return safeApiCall(
            logger = LH.logger,
            errorContext = "Error fetching project photos modified since $since for project $projectId.",
        ) {
            httpClient
                .get("/projects/$projectId/photos?since=${since.value}")
                .body<List<ProjectPhotoApiDto>>()
                .map { dto -> dto.toSyncResult() }
        }.also {
            if (it is OnlineDataResult.Success) {
                LH.logger.d { "Fetched ${it.data.size} modified project photos for project $projectId." }
            }
        }
    }
}
