package cz.adamec.timotej.snag.projects.fe.ports

import cz.adamec.timotej.snag.core.foundation.common.Timestamp
import cz.adamec.timotej.snag.core.network.fe.OnlineDataResult
import cz.adamec.timotej.snag.projects.app.model.AppProjectPhoto
import kotlin.uuid.Uuid

sealed interface ProjectPhotoSyncResult {
    data class Deleted(
        val id: Uuid,
    ) : ProjectPhotoSyncResult

    data class Updated(
        val photo: AppProjectPhoto,
    ) : ProjectPhotoSyncResult
}

interface ProjectPhotosApi {
    suspend fun savePhoto(photo: AppProjectPhoto): OnlineDataResult<AppProjectPhoto?>

    suspend fun deletePhoto(
        id: Uuid,
        projectId: Uuid,
        deletedAt: Timestamp,
    ): OnlineDataResult<AppProjectPhoto?>

    suspend fun getPhotosModifiedSince(
        projectId: Uuid,
        since: Timestamp,
    ): OnlineDataResult<List<ProjectPhotoSyncResult>>
}
