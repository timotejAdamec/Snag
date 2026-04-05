package cz.adamec.timotej.snag.projects.be.ports

import cz.adamec.timotej.snag.core.foundation.common.Timestamp
import cz.adamec.timotej.snag.projects.be.model.BackendProjectPhoto
import kotlin.uuid.Uuid

interface ProjectPhotosDb {
    suspend fun savePhoto(photo: BackendProjectPhoto): BackendProjectPhoto?

    suspend fun deletePhoto(
        id: Uuid,
        deletedAt: Timestamp,
    ): BackendProjectPhoto?

    suspend fun getPhotosModifiedSince(
        projectId: Uuid,
        since: Timestamp,
    ): List<BackendProjectPhoto>
}
