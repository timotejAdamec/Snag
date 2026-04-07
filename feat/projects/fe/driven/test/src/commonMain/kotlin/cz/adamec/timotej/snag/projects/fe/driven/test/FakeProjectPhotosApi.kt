package cz.adamec.timotej.snag.projects.fe.driven.test

import cz.adamec.timotej.snag.core.foundation.common.Timestamp
import cz.adamec.timotej.snag.core.network.fe.OnlineDataResult
import cz.adamec.timotej.snag.network.fe.test.FakeApiOps
import cz.adamec.timotej.snag.projects.app.model.AppProjectPhoto
import cz.adamec.timotej.snag.projects.fe.ports.ProjectPhotoSyncResult
import cz.adamec.timotej.snag.projects.fe.ports.ProjectPhotosApi
import kotlin.uuid.Uuid

class FakeProjectPhotosApi : ProjectPhotosApi {
    private val ops =
        FakeApiOps<AppProjectPhoto, ProjectPhotoSyncResult>(getId = { it.id })

    var forcedFailure
        get() = ops.forcedFailure
        set(value) {
            ops.forcedFailure = value
        }

    var savePhotoResponseOverride
        get() = ops.saveResponseOverride
        set(value) {
            ops.saveResponseOverride = value
        }

    var modifiedSinceResults
        get() = ops.modifiedSinceResults
        set(value) {
            ops.modifiedSinceResults = value
        }

    override suspend fun savePhoto(photo: AppProjectPhoto): OnlineDataResult<AppProjectPhoto?> = ops.saveItem(photo)

    override suspend fun deletePhoto(
        id: Uuid,
        projectId: Uuid,
        deletedAt: Timestamp,
    ): OnlineDataResult<AppProjectPhoto?> = ops.deleteItemById(id)

    override suspend fun getPhotosModifiedSince(
        projectId: Uuid,
        since: Timestamp,
    ): OnlineDataResult<List<ProjectPhotoSyncResult>> = ops.getModifiedSinceItems()

    fun setPhoto(photo: AppProjectPhoto) = ops.setItem(photo)

    fun setPhotos(photos: List<AppProjectPhoto>) = ops.setItems(photos)
}
